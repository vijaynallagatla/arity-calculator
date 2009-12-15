// Copyright (C) 2009 Mihai Preda

package calculator;

import android.opengl.Matrix;
import android.opengl.GLSurfaceView.Renderer;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.egl.EGLConfig;
import org.javia.arity.Function;

class GraphRenderer implements Renderer {
    private float[] matrix1 = new float[16], matrix2 = new float[16], matrix3 = new float[16];
    private float angleX, angleY;
    private int drawCnt;
    private long lastTime;
    private boolean isDirty;
    private Function function;
    
    GraphRenderer() {
        Matrix.setIdentityM(matrix1, 0);
        Matrix.rotateM(matrix1, 0, -75, 1, 0, 0);
    }

    void setRotation(float x, float y) {
        angleX = x;
        angleY = y;
    }

    public void setFunction(Function f) {
        function = f;
        isDirty = true;
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig dummy) {
        gl.glDisable(GL10.GL_DITHER);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);               
        gl.glClearColor(0, 0, 0, 1);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glDisable(GL10.GL_LIGHTING);
        Graph3d.instance.init(gl, function);
        isDirty = false;
        angleX = .5f;
        angleY = 0;
    }

    private static final float NEAR = 11;
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        float h = NEAR/5f * height / (float) width;
        gl.glFrustumf(-NEAR/5f, NEAR/5f, -h, h, NEAR, NEAR+18);
        // gl.glOrthof(-NEAR, NEAR, -h, h, NEAR, NEAR+12);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
    }
    
    public void onDrawFrame(GL10 gl10) {
        GL11 gl = (GL11) gl10;
        if (isDirty) {
            Graph3d.instance.update(gl, function);
            isDirty = false;
        }
        if (--drawCnt <= 0) {
            drawCnt = 100;
            long now = System.currentTimeMillis();
            Calculator.log("f/s " + Math.round(100000f / (now - lastTime)));
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
    }

    private void printMatrix(float[] m, String name) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < 16; ++i) {
            b.append(m[i]).append(' ');
        }
        Calculator.log(name + ' ' + b.toString());
    }
}
