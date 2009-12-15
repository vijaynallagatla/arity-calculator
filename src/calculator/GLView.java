// Copyright (C) 2009 Mihai Preda

package calculator;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.opengl.GLSurfaceView.Renderer;

abstract class GLView extends SurfaceView implements SurfaceHolder.Callback {
    private boolean hasSurface;
    private boolean paused;
    private EGL10 egl;
    private EGLDisplay display;
    private EGLConfig config;    
    private EGLSurface surface;
    private EGLContext eglContext;
    private GL11 gl;
    private int width, height;
    private boolean sizeChangePending;
    private Renderer renderer;

    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    public GLView(Context context) {
        super(context);
        init();
    }

    public GLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        SurfaceHolder holder = getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
        holder.addCallback(this);
    }
    
    public void onResume() {
        Calculator.log("onResume " + this);
        paused = false;
        if (hasSurface) {
            initGL();
        }
    }

    public void onPause() {
        Calculator.log("onPause " + this);
        deinitGL();
    }

    private void initGL() {
        egl = (EGL10) EGLContext.getEGL();
        display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        int[] ver = new int[2];
        egl.eglInitialize(display, ver);
        
        int[] configSpec = {EGL10.EGL_NONE};
        EGLConfig[] configOut = new EGLConfig[1];
        int[] nConfig = new int[1];
        egl.eglChooseConfig(display, configSpec, configOut, 1, nConfig);
        config = configOut[0];
        eglContext = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, null);
        surface = egl.eglCreateWindowSurface(display, config, getHolder(), null);
        egl.eglMakeCurrent(display, surface, surface, eglContext);
        gl = (GL11) eglContext.getGL();
        renderer.onSurfaceCreated(gl, null);
        renderer.onSurfaceChanged(gl, width, height);
        myDraw();
    }

    private void deinitGL() {
        paused = true;
        if (display != null) {
            egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            egl.eglDestroySurface(display, surface);
            egl.eglDestroyContext(display, eglContext);
            egl.eglTerminate(display);

            egl = null;
            config = null;
            eglContext = null;
            surface = null;
            display = null;
            gl = null;
        }
    }

    protected void myDraw() {
        // Calculator.log("draw " + stopped);
        if (hasSurface && !paused) {
            // Calculator.log("myDraw " + this);
            renderer.onDrawFrame(gl);
            if (!egl.eglSwapBuffers(display, surface)) {
                Calculator.log("swapBuffers error " + egl.eglGetError());
            }
            if (egl.eglGetError() == EGL11.EGL_CONTEXT_LOST) {
                Calculator.log("egl context lost " + this);
                paused = true;
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Calculator.log("surfaceCreated " + this);
    }
    
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Calculator.log("surfaceChanged " + format + ' ' + this);
        this.width  = width;
        this.height = height;
        if (!hasSurface && !paused) {
            hasSurface = true;
            initGL();
        } else {
            hasSurface = true;
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Calculator.log("surfaceDestroyed " + this);
        hasSurface = false;
        deinitGL();
    }
}
