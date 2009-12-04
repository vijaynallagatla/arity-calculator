// Copyright (C) 2009 Mihai Preda

package calculator;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import org.javia.arity.*;

class Graph3d {
    static Graph3d instance = new Graph3d();

    private static final int N = 24;
    private float minX = -4, maxX = 4, minY = -4, maxY = 4; 

    private ShortBuffer verticeIdx;
    private int vertexVbo, colorVbo, vertexElementVbo;
    
    private Graph3d() {
        short[] b = new short[N*N];
        int p = 0;
        for (int i = 0; i < N; i++) {
            short v = 0;
            for (int j = 0; j < N; v += N+N, j+=2) {
                b[p++] = (short)(v+i);
                b[p++] = (short)(v+N+N-1-i);
            }
            v = N*(N-2);
            i++;
            for (int j = N-1; j >= 0; v -= N+N, j-=2) {
                b[p++] = (short)(v+N+N-1-i);
                b[p++] = (short)(v+i);
            } 
        }
        verticeIdx = buildBuffer(b);
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

    public void init(GL11 gl, Function f) {
        int[] out = new int[3];
        gl.glGenBuffers(3, out, 0);        
        vertexVbo = out[0];
        colorVbo  = out[1];
        vertexElementVbo = out[2];
        update(gl, f);
    }

    public void update(GL11 gl, Function f) {
        Calculator.log("update VBOs " + vertexVbo + ' ' + colorVbo + ' ' + vertexElementVbo);
        int nVertex = N*N+6+8;
        int nFloats = nVertex * 3;
        float vertices[] = new float[nFloats];
        float colors[] = new float[nFloats];        
        if (f != null) {
            float sizeX = maxX - minX;
            float sizeY = maxY - minY;
            float stepX = sizeX / (N-1);
            float stepY = sizeY / (N-1);
            int pos = 0;
            float maxAbs = 0;
            float y = minY;
            float x = minX - stepX;
            for (int i = 0; i < N; i++, y+=stepY) {
                float xinc = (i & 1) == 0 ? stepX : -stepX;
                x += xinc;
                for (int j = 0; j < N; ++j, x+=xinc, pos+=3) {
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
            maxAbs = Math.min(maxAbs, 6);

            for (int i = (N*N*3-3); i >= 0; i-=3) {
                float z = vertices[i+2];
                float a = z / maxAbs;
                colors[i] = a + 1;
                colors[i+1] = a > 0 ? a : -a;
                colors[i+2] = -a + 1;
            }
        }
        final int base = N*N*3;
        int p = base;
        for (int i = -4; i <= 4; i+=8) {
            vertices[p] = i; vertices[p+1] = -4; vertices[p+2] = 0;
            p += 3;
            vertices[p] = i; vertices[p+1] = 4; vertices[p+2] = 0;
            p += 3;
            vertices[p] = -4; vertices[p+1] = i; vertices[p+2] = 0;
            p += 3;
            vertices[p] = 4; vertices[p+1] = i; vertices[p+2] = 0;
            p += 3;
        }
        for (int i = base; i < base+8*3; i += 3) {
            colors[i] = 0;
            colors[i+1] = 0;
            colors[i+2] = 1;
        }
        final float unit = 6;
        final float axis[] = {
            0, 0, 0,
            unit, 0, 0,
            0, 0, 0,
            0, unit, 0,
            0, 0, 0,
            0, 0, unit,
        };
        System.arraycopy(axis, 0, vertices, base+8*3, 6*3);
        for (int i = base+8*3; i < base+(6+8)*3; i+=3) {
            colors[i] = 1;
            colors[i+1] = 1;
            colors[i+2] = 1;
        }                

        int nBytes = nFloats * 4;
        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexVbo);
        gl.glBufferData(GL11.GL_ARRAY_BUFFER, nBytes, buildBuffer(vertices), GL11.GL_STATIC_DRAW);
        
        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, colorVbo);
        gl.glBufferData(GL11.GL_ARRAY_BUFFER, nBytes, buildBuffer(colors), GL11.GL_STATIC_DRAW);
        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);            
        
        gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, vertexElementVbo);
        gl.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, verticeIdx.capacity()*2, verticeIdx, GL11.GL_STATIC_DRAW);
        gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void draw(GL11 gl) {
        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexVbo);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, 0);

        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, colorVbo);
        gl.glColorPointer(3, GL10.GL_FLOAT, 0, 0);

        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
        gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, N*N);

        gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, vertexElementVbo);        
        gl.glDrawElements(GL10.GL_LINE_STRIP, N*N, GL10.GL_UNSIGNED_SHORT, 0);
        gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);

        gl.glDrawArrays(GL10.GL_LINES, N*N, 6+8);
    }
}
