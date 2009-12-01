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

abstract class GLView extends SurfaceView implements SurfaceHolder.Callback {
    GL11 gl;
    EGLDisplay display;
    EGLSurface surface;
    EGL10 egl;
    EGLConfig config;
    EGLContext eglContext;
    private boolean stopped;

    protected abstract void onSurfaceChanged(GL10 gl, int width, int height);
    protected abstract void onDrawFrame(GL11 gl);

    public GLView(Context context) {
        super(context);
        getHolder().addCallback(this);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_GPU);        
        init();
    }
    
    protected void myDraw() {
        // Calculator.log("draw " + stopped);
        if (!stopped) {
            onDrawFrame(gl);
            egl.eglSwapBuffers(display, surface);
            if (egl.eglGetError() == EGL11.EGL_CONTEXT_LOST) {
                Calculator.log("egl context lost");
                stop();
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Calculator.log("surfaceCreated");
        surface = egl.eglCreateWindowSurface(display, config, getHolder(), null);
        egl.eglMakeCurrent(display, surface, surface, eglContext);
        gl = (GL11) eglContext.getGL();        
    }
    
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Calculator.log("surfaceChanged " + format);
        onSurfaceChanged(gl, width, height);
        myDraw();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Calculator.log("surfaceDestroyed");
    }

    /*
    public void onPause() {
        stopped = true;
    }
    
    public void onResume() {
        stopped = false;
        if (surface != null) {
            draw();
        }
    }
    */

    private void init() {
        egl = (EGL10) EGLContext.getEGL();
        display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        int[] ver = new int[2];
        egl.eglInitialize(display, ver);

        int[] configSpec = {EGL10.EGL_NONE};
        EGLConfig[] configOut = new EGLConfig[1];
        int[] nConfig = new int[1];
        egl.eglChooseConfig(display, configSpec, configOut, 1, nConfig);
        config = configOut[0];
        Calculator.log("egl config: depth " + getConfig(EGL10.EGL_DEPTH_SIZE)
                       + " stencil " + getConfig(EGL10.EGL_STENCIL_SIZE)
                       + " red " + getConfig(EGL10.EGL_RED_SIZE)
                       + " green " + getConfig(EGL10.EGL_GREEN_SIZE)
                       + " blue " + getConfig(EGL10.EGL_BLUE_SIZE)
                       + " apha " + getConfig(EGL10.EGL_ALPHA_SIZE));
        eglContext = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, null);
    }    

    public void stop() {
        stopped = true;
        egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE,
                           EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(display, surface);
        egl.eglDestroyContext(display, eglContext);
        egl.eglTerminate(display);
    }

    /*
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Calculator.log("onAttachedToWindow");
    }
    */

    /*
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Calculator.log("onDetachedFromWindow");
        // deinit();
    }
    */
    
    private int getConfig(int atrib) {
        int[] ret = new int[1];
        if (egl.eglGetConfigAttrib(display, config, atrib, ret)) {
            return ret[0];
        }
        return -1;
    }
}
