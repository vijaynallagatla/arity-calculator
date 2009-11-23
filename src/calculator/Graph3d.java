// Copyright (C) 2009 Mihai Preda

package calculator;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import org.javia.arity.*;

class Graph3d {
    private static final int N = 25;
    private float minX = -4, maxX = 4, minY = -4, maxY = 4; 

    private FloatBuffer vertexBuf, colorBuf, axisVertexBuf, gridVertexBuf;
    private ShortBuffer verticeIdx[] = new ShortBuffer[N];
    
    public Graph3d() {
        short[] b = new short[N];
        for (int i = 0; i < N; ++i) {
            short v = (short) i;
            for (int j = 0; j < N; ++j, v+=N) {
                b[j] = v;
            }
            verticeIdx[i] = buildBuffer(b);
        }

        float grid[] = {
            -4, -4, 0, -4, 4, 0,
            -2, -4, 0, -2, 4, 0,
            0, -4, 0, 0, 4, 0,
            2, -4, 0, 2, 4, 0,
            4, -4, 0, 4, 4, 0,

            -4, -4, 0, 4, -4, 0,
            -4, -2, 0, 4, -2, 0,
            -4, 0, 0, 4, 0, 0,
            -4, 2, 0, 4, 2, 0,
            -4, 4, 0, 4, 4, 0,
        };
        gridVertexBuf = buildBuffer(grid);

        float unit = 5;
        float axis[] = {
            0, 0, 0,
            unit, 0, 0,
            0, 0, 0,
            0, unit, 0,
            0, 0, 0,
            0, 0, unit,
        };
        axisVertexBuf = buildBuffer(axis);        
    }

    public void setFunction(Function f) {
        float vertices[] = new float[N*N*3 + 6*3];
        float colors[] = new float[N*N*3 + 6*3];
        if (f != null) {
            float sizeX = maxX - minX;
            float sizeY = maxY - minY;
            float stepX = sizeX / (N-1);
            float stepY = sizeY / (N-1);
            int pos = 0;
            float maxAbs = 0;
            float y = minY;
            for (int i = 0; i < N; ++i, y+=stepY) {
                float x = minX;
                for (int j = 0; j < N; ++j, x+=stepX, pos+=3) {
                    float z = (float) f.eval(x, y);
                    if (z != z) {
                        z = 0;
                    }
                    vertices[pos] = x;
                    vertices[pos+1] = y;
                    vertices[pos+2] = z;
                    if (z > maxAbs || -z < -maxAbs) {
                        maxAbs = Math.abs(z);
                    }
                }
            }
            for (int i = (N*N*3-3); i >= 0; i-=3) {
                float z = vertices[i+2];
                float a = z / maxAbs;
                colors[i] = a + 1;
                colors[i+1] = a > 0 ? a : -a;
                colors[i+2] = -a + 1;
            }
        }
        vertexBuf = buildBuffer(vertices);
        colorBuf  = buildBuffer(colors);
    }

    private static FloatBuffer buildBuffer(float[] b) {
        ByteBuffer bb = ByteBuffer.allocateDirect(b.length << 2);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer sb = bb.asFloatBuffer();
        sb.put(b);
        sb.position(0);
        return sb;
    }

    private static ShortBuffer buildBuffer(short[] b) {
        ByteBuffer bb = ByteBuffer.allocateDirect(b.length << 1);
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer sb = bb.asShortBuffer();
        sb.put(b);
        sb.position(0);
        return sb;
    }

    public void draw(GL10 gl) {
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glColor4f(.7f, .7f, .7f, 1);

        gl.glVertexPointer(3, gl.GL_FLOAT, 0, gridVertexBuf);        
        gl.glDrawArrays(gl.GL_LINES, 0, 20);

        gl.glColor4f(1, 1, 1, 1);
        gl.glVertexPointer(3, gl.GL_FLOAT, 0, axisVertexBuf);
        gl.glDrawArrays(gl.GL_LINES, 0, 6);

        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        // gl.glFrontFace(gl.GL_CW);
        gl.glVertexPointer(3, gl.GL_FLOAT, 0, vertexBuf);
        gl.glColorPointer(3, gl.GL_FLOAT, 0, colorBuf);

        // gl.glDrawElements(gl.GL_TRIANGLES, 36, gl.GL_UNSIGNED_SHORT, cube);
        // gl.glColor4f(1, 1, 1, 1);
        for (int i = 0; i < N; ++i) {
            gl.glDrawArrays(gl.GL_LINE_STRIP, i*N, N);
            gl.glDrawElements(gl.GL_LINE_STRIP, N, GL10.GL_UNSIGNED_SHORT, verticeIdx[i]);
        }

        // gl.glColor4f(0, 1, 0, 1);
        // gl.glDrawElements(gl.GL_LINES, 6, gl.GL_UNSIGNED_SHORT, axis);
    }
}
