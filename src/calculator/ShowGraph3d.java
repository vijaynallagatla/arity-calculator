// Copyright (C) 2009 Mihai Preda

package calculator;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import arity.calculator.R;

public class ShowGraph3d extends Activity {
    private Graph3dView surfaceView;
    private static Graph3d graph = new Graph3d();

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        graph.setFunction(Calculator.graphed3d);
        surfaceView = new Graph3dView(this, graph);
        setContentView(surfaceView);
    }

    protected void onPause() {
        super.onPause();
        Calculator.log("activity pause");
        surfaceView.onPause();
        surfaceView = null;
    }

    protected void onResume() {
        super.onResume();
        Calculator.log("activity resume");
        if (surfaceView == null) {
            surfaceView = new Graph3dView(this, graph);
            setContentView(surfaceView);
        }
        surfaceView.onResume();
    }
}
