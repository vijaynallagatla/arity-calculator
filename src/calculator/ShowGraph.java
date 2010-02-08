// Copyright (C) 2009 Mihai Preda

package calculator;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.opengl.GLSurfaceView;
import org.javia.arity.Function;
import java.util.ArrayList;

public class ShowGraph extends Activity {
    private Grapher view;
    private GraphView graphView;
    private GLSurfaceView surfaceView;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        ArrayList<Function> funcs = Calculator.graphedFunction;
        int size = funcs.size();
        if (size == 1) {
            Function f = funcs.get(0);
            view = f.arity() == 1 ? new GraphView(this) : new Graph3dView(this);
            view.setFunction(f);
        } else {
            view = new GraphView(this);
            ((GraphView) view).setFunctions(funcs);
        }
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
