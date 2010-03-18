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
    private final int N = Calculator.useHighQuality3d ? 36 : 24;
    private ShortBuffer verticeIdx;
    private FloatBuffer vertexBuf, colorBuf;
    private int vertexVbo, colorVbo, vertexElementVbo;
    private boolean useVBO;
    private boolean inited = false;
    
    private void init(GL11 gl) {
        short[] b = new short[N*N];
        int p = 0;
        for (int i = 0; i < N; i++) {
            short v = 0;
            for (int j = 0; j < N; v += N+N, j+=2) {
                b[p++] = (short)(v+i);
                b[p++] = (short)(v+N+N-1-i);
            }
            v = (short) (N*(N-2));
            i++;
            for (int j = N-1; j >= 0; v -= N+N, j-=2) {
                b[p++] = (short)(v+N+N-1-i);
                b[p++] = (short)(v+i);
            } 
        }
        verticeIdx = buildBuffer(b);

        String extensions = gl.glGetString(GL10.GL_EXTENSIONS);
        useVBO = extensions.indexOf("vertex_buffer_object") != -1;
        Calculator.log("VBOs support: " + useVBO + " version " + gl.glGetString(GL10.GL_VERSION));
        
        if (useVBO) {
            int[] out = new int[3];
            gl.glGenBuffers(3, out, 0);        
            vertexVbo = out[0];
            colorVbo  = out[1];
            vertexElementVbo = out[2];
        }
        inited = true;
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

    public void update(GL11 gl, Function f, float zoom) {
        if (!inited) {
            init(gl);
            inited = true;
        }
        final int NTICK = Calculator.useHighQuality3d ? 5 : 0;
        final float size = 4*zoom;
        final float minX = -size, maxX = size, minY = -size, maxY = size;

        Calculator.log("update VBOs " + vertexVbo + ' ' + colorVbo + ' ' + vertexElementVbo);
        int nVertex = N*N+6+8 + NTICK*6;
        int nFloats = nVertex * 3;
        float vertices[] = new float[nFloats];
        float colors[] = new float[nVertex << 2];        
        if (f != null) {
            Calculator.log("Graph3d update");
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
            maxAbs *= .9f;
            maxAbs = Math.min(maxAbs, 15);

            for (int i = (N*N*4-4), j = (N*N*3-3); i >= 0; i-=4, j-=3) {
                float z = vertices[j+2];
                float a = z / maxAbs;
                colors[i] = a + 1;
                colors[i+1] = a > 0 ? a : -a;
                colors[i+2] = -a + 1;
                colors[i+3] = 1;
            }
        }
        int base = N*N*3;
        int colorBase = N*N*4;
        int p = base;
        final int baseSize = 2;
        for (int i = -baseSize; i <= baseSize; i+=2*baseSize) {
            vertices[p] = i; vertices[p+1] = -baseSize; vertices[p+2] = 0;
            p += 3;
            vertices[p] = i; vertices[p+1] = baseSize; vertices[p+2] = 0;
            p += 3;
            vertices[p] = -baseSize; vertices[p+1] = i; vertices[p+2] = 0;
            p += 3;
            vertices[p] = baseSize; vertices[p+1] = i; vertices[p+2] = 0;
            p += 3;
        }
        for (int i = colorBase; i < colorBase+8*4; i += 4) {
            colors[i] = 0;
            colors[i+1] = 0;
            colors[i+2] = 1;
            colors[i+3] = 1;
        }
        base += 8*3;
        colorBase += 8*4;

        final float unit = 2;
        final float axis[] = {
            0, 0, 0,
            unit, 0, 0,
            0, 0, 0,
            0, unit, 0,
            0, 0, 0,
            0, 0, unit,
        };
        System.arraycopy(axis, 0, vertices, base, 6*3);
        for (int i = colorBase; i < colorBase+6*4; i+=4) {
            colors[i] = 1;
            colors[i+1] = 1;
            colors[i+2] = 1;
            colors[i+3] = 1;
        }                
        base += 6*3;
        colorBase += 6*4;

        p = base;
        final float tick = .02f;
        for (int i = 1; i <= NTICK; ++i) {
            vertices[p] = i;
            vertices[p+1] = -tick;
            vertices[p+2] = -tick;

            vertices[p+3] = i;
            vertices[p+4] = tick;
            vertices[p+5] = tick;
            p += 6;

            vertices[p] = -tick;
            vertices[p+1] = i;
            vertices[p+2] = -tick;

            vertices[p+3] = tick;
            vertices[p+4] = i;
            vertices[p+5] = tick;
            p += 6;

            vertices[p] = -tick;
            vertices[p+1] = -tick;
            vertices[p+2] = i;

            vertices[p+3] = tick;
            vertices[p+4] = tick;
            vertices[p+5] = i;
            p += 6;
            
        }
        for (int i = colorBase+NTICK*6*4-1; i >= colorBase; --i) {
            colors[i] = 1;
        }

        vertexBuf = buildBuffer(vertices);
        colorBuf  = buildBuffer(colors);

        if (useVBO) {
            gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexVbo);
            gl.glBufferData(GL11.GL_ARRAY_BUFFER, vertexBuf.capacity()*4, vertexBuf, GL11.GL_STATIC_DRAW);           
            vertexBuf = null;

            gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, colorVbo);
            gl.glBufferData(GL11.GL_ARRAY_BUFFER, colorBuf.capacity()*4, colorBuf, GL11.GL_STATIC_DRAW);
            gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
            colorBuf = null;            
        
            gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, vertexElementVbo);
            gl.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, verticeIdx.capacity()*2, verticeIdx, GL11.GL_STATIC_DRAW);
            gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

    public void draw(GL11 gl) {
        if (useVBO) {
            gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexVbo);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, 0);

            gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, colorVbo);
            gl.glColorPointer(4, GL10.GL_FLOAT, 0, 0);

            gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
            // gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, N*N);

            gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, vertexElementVbo);        
            gl.glDrawElements(GL10.GL_LINE_STRIP, N*N, GL10.GL_UNSIGNED_SHORT, 0);
            gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
        } else {
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuf);
            gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuf);
            gl.glDrawElements(GL10.GL_LINE_STRIP, N*N, GL10.GL_UNSIGNED_SHORT, verticeIdx);
        }
        gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, N*N);
        gl.glDrawArrays(GL10.GL_LINES, N*N, 6+8+60);
    }
}
