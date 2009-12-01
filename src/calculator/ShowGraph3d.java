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
    private static Graph3d graph = new Graph3d();
    private float[] rotation = new float[16];
    private View dummy;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        graph.setFunction(Calculator.graphed3d);
        dummy = new View(this);
        Matrix.setIdentityM(rotation, 0);
        Matrix.rotateM(rotation, 0, -75, 1, 0, 0);
    }

    protected void onPause() {
        super.onPause();
        Calculator.log("activity pause");
        setContentView(dummy);
        view.stop();
        view = null;
        // view.onPause();
    }

    protected void onResume() {
        super.onResume();
        Calculator.log("activity resume");
        view = new Graph3dView(this, graph, rotation);
        setContentView(view);
        // view.onResume();
    }
}
