package calculator;

import android.opengl.GLSurfaceView;
import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.Scroller;
import android.os.Handler;
import android.os.Message;
import arity.calculator.R;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import android.opengl.Matrix;

public class Graph3dView extends GLView {
    // private GraphRenderer renderer;
    private float lastTouchX, lastTouchY;
    private VelocityTracker velocityTracker;
    private Scroller scroller;
    private float angleX=.5f, angleY;
    private float[] matrix1, matrix2 = new float[16], matrix3 = new float[16];
    private Graph3d graph;
    private int drawCnt;
    private long lastTime;

    private Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                myDraw();
            }
        };

    Graph3dView(Context context, Graph3d graph, float[] rotation) {
        super(context);
        this.graph = graph;
        matrix1 = rotation;
        scroller = new Scroller(context);
    }

    private static final float NEAR = 13;
    protected void onSurfaceChanged(GL10 gl, int width, int height) {
        Calculator.log("size " + width + ' ' + height);
        gl.glDisable(GL10.GL_DITHER);
        // gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);               
        gl.glClearColor(0, 0, 0, 1);
        gl.glShadeModel(GL10.GL_SMOOTH);
        
        gl.glViewport(0, 0, width+1, height+1);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        float h = NEAR/5f * height / (float) width;
        gl.glFrustumf(-NEAR/5f, NEAR/5f, -h, h, NEAR, NEAR+12);
        // gl.glOrthof(-NEAR, NEAR, -h, h, NEAR, NEAR+12);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
    }
    
    public void onDrawFrame(GL11 gl) {
        if (--drawCnt <= 0) {
            drawCnt = 10;
            long now = System.currentTimeMillis();
            Calculator.log("time " + (now - lastTime));
            lastTime = now;
        }

        // Calculator.log("draw frame");
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(0, 0, -(NEAR+6.5f));

        Matrix.setIdentityM(matrix2, 0);
        float ax = Math.abs(angleX);
        float ay = Math.abs(angleY);
        if (ay * 3 < ax) {
            Matrix.rotateM(matrix2, 0, angleX, 0, 1, 0);
        } else if (ax * 3 < ay) {
            Matrix.rotateM(matrix2, 0, angleY, 1, 0, 0);
        } else {
            if (ax > ay) {
                Matrix.rotateM(matrix2, 0, angleX, 0, 1, 0);
                Matrix.rotateM(matrix2, 0, angleY, 1, 0, 0);
            } else {
                Matrix.rotateM(matrix2, 0, angleY, 1, 0, 0);
                Matrix.rotateM(matrix2, 0, angleX, 0, 1, 0);
            }
        }
        Matrix.multiplyMM(matrix3, 0, matrix2, 0, matrix1, 0);
        gl.glMultMatrixf(matrix3, 0);        
        System.arraycopy(matrix3, 0, matrix1, 0, 16);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);        
        graph.draw(gl);
        if (!handler.hasMessages(1)) {
            handler.sendEmptyMessage(1);
        }
    }    

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            if (!scroller.isFinished()) {
                scroller.abortAnimation();
            }
            velocityTracker = VelocityTracker.obtain();
            velocityTracker.addMovement(event);
            lastTouchX = x;
            lastTouchY = y;
            break;

        case MotionEvent.ACTION_MOVE:
            velocityTracker.addMovement(event);
            float deltaX = x - lastTouchX;
            float deltaY = y - lastTouchY;
            if (deltaX > 1 || deltaX < -1 || deltaY > 1 || deltaY < -1) {
                angleX = deltaX;
                angleY = deltaY;
                myDraw();
                lastTouchX = x;
                lastTouchY = y;
            }
            break;
            
        case MotionEvent.ACTION_UP:
            velocityTracker.computeCurrentVelocity(1000);
            scroller.fling(Math.round(x), Math.round(y), 
                           Math.round(velocityTracker.getXVelocity()), 
                           Math.round(velocityTracker.getYVelocity()),
                           -1000, 1000, -1000, 1000);
            // draw();
            // no break

        default:
            if (velocityTracker != null) {
                velocityTracker.recycle();
                velocityTracker = null;
            }
        }
        return true;
    }
}
