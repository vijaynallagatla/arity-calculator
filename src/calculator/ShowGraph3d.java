// Copyright (C) 2009 Mihai Preda

package calculator;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import arity.calculator.R;

public class ShowGraph3d extends Activity {
    private GLSurfaceView surfaceView;
    private static Graph3d graph = new Graph3d();

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        graph.setFunction(Calculator.graphed3d);
        surfaceView = new Graph3dView(this, graph);
        setContentView(surfaceView);
    }

    protected void onPause() {
        super.onPause();
        surfaceView.onPause();
    }

    protected void onResume() {
        super.onResume();
        surfaceView.onResume();
    }
}
