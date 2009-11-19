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
        surfaceView = new GLSurfaceView(this);
        graph.setFunction(Calculator.graphed3d);
        surfaceView.setRenderer(new GraphRenderer(graph));
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
