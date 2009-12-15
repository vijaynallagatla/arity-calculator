// Copyright (C) 2009 Mihai Preda

package calculator;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.opengl.GLSurfaceView;
import org.javia.arity.Function;

public class ShowGraph extends Activity {
    private Grapher view;
    private GraphView graphView;
    private GLSurfaceView surfaceView;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Function f = Calculator.graphedFunction;
        int arity = f.arity();
        /*
        if (arity == 1) {
            graphView = new GraphView(this);
            graphView.setFunction(f);
            setContentView(graphView);
        } else {
            surfaceView = new GLSurfaceView(this);
            GraphRenderer renderer = new GraphRenderer();
            renderer.setFunction(f);
            surfaceView.setRenderer(renderer);
            surfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR);
            setContentView(surfaceView);
        }
        */
        view = arity == 1 ? new GraphView(this) : new Graph3dView(this);
        view.setFunction(f);
        setContentView((View) view);
    }

    protected void onPause() {
        super.onPause();
        view.onPause();
        /*
        if (surfaceView != null) {
            surfaceView.onPause();
        }
        */
    }

    protected void onResume() {
        super.onResume();
        view.onResume();
        /*
        if (surfaceView != null) {
            surfaceView.onResume();
        }
        */
    }
}
