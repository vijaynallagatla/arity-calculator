// Copyright (C) 2009 Mihai Preda

package calculator;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import arity.calculator.R;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.Matrix;

public class ShowGraph3d extends Activity {
    private Graph3dView view;
    private float[] rotation = new float[16];
    // private View dummy;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // Graph3d.instance.setFunction(Calculator.graphed3d);
        // dummy = new View(this);
        Matrix.setIdentityM(rotation, 0);
        Matrix.rotateM(rotation, 0, -75, 1, 0, 0);
        view = new Graph3dView(this, rotation);
        setContentView(view);        
    }

    protected void onPause() {
        super.onPause();
        Calculator.log("activity pause");
        // setContentView(dummy);
        view.onPause();
    }

    protected void onResume() {
        super.onResume();
        Calculator.log("activity resume");
        view.onResume();
    }
}
