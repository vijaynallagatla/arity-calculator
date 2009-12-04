// Copyright (C) 2009 Mihai Preda

package calculator;

import android.opengl.GLSurfaceView;
import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.Scroller;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.opengl.Matrix;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import org.javia.arity.Function;

public class Graph3dView extends GLView implements Grapher {
    private float lastTouchX, lastTouchY;
    private VelocityTracker velocityTracker;
    private float angleX, angleY;
    private float[] matrix1 = new float[16], matrix2 = new float[16], matrix3 = new float[16];
    private int drawCnt;
    private long lastTime;
    private boolean isRotating;
    private boolean isFullScreen;
    private boolean isDirty;
    private Function function;

    private Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                myDraw();
            }
        };

    public Graph3dView(Context context, AttributeSet attrs) {
        super(context, attrs);
        isFullScreen = false;
        init();
    }

    public Graph3dView(Context context) {
        super(context);
        isFullScreen = true;
        init();
    }

    private void init() {
        Matrix.setIdentityM(matrix1, 0);
        Matrix.rotateM(matrix1, 0, -75, 1, 0, 0);
    }

    public void setFunction(Function f) {
        function = f;
        isDirty = true;
        // forceDraw();
    }

    protected void onSurfaceCreated(GL11 gl) {
        gl.glDisable(GL10.GL_DITHER);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);               
        gl.glClearColor(0, 0, 0, 1);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glDisable(GL10.GL_LIGHTING);
        Graph3d.instance.init(gl, function);
        isDirty = false;
        angleX = .5f;
        angleY = 0;
        isRotating = true;
    }

    private static final float NEAR = 11;
    protected void onSurfaceChanged(GL11 gl, int width, int height) {
        // Calculator.log("size " + width + ' ' + height);        
        gl.glViewport(0, 0, width+1, height+1);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        float h = NEAR/5f * height / (float) width;
        gl.glFrustumf(-NEAR/5f, NEAR/5f, -h, h, NEAR, NEAR+18);
        // gl.glOrthof(-NEAR, NEAR, -h, h, NEAR, NEAR+12);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
    }
    
    public void onDrawFrame(GL11 gl) {
        if (isDirty) {
            Graph3d.instance.update(gl, function);
            isDirty = false;
        }
        if (--drawCnt <= 0) {
            drawCnt = 10;
            long now = System.currentTimeMillis();
            // Calculator.log("time " + (now - lastTime));
            lastTime = now;
        }

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(0, 0, -(NEAR+9f));

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

        Graph3d.instance.draw(gl);
        if (isRotating) {
            requestDraw();
        }
    }    

    private void requestDraw() {
        if (!handler.hasMessages(1)) {
            handler.sendEmptyMessage(1);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!isFullScreen) {
            isRotating = false;
            return super.onTouchEvent(event);
        }

        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            isRotating = false;
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
            float vx = velocityTracker.getXVelocity();
            float vy = velocityTracker.getYVelocity();
            // Calculator.log("velocity " + vx + ' ' + vy);
            final float limit = 50;
            isRotating = vx < -limit || vx > limit || vy < -limit || vy > limit;
            angleX = vx / 100;
            angleY = vy / 100;
            if (isRotating) {
                requestDraw();
            }
            // scroller.fling(Math.round(x), Math.round(y), Math.round(vx), Math.round(vy), -5000, 5000, -5000, 5000);
            // requestDraw();
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
