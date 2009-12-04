// Copyright (C) 2009 Mihai Preda

package calculator;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import org.javia.arity.Function;

public class ShowGraph extends Activity {
    private Grapher view;    

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Function f = Calculator.graphedFunction;
        view = f.arity() == 1 ? new GraphView(this) : new Graph3dView(this);
        view.setFunction(f);
        setContentView((View) view);
    }

    protected void onPause() {
        super.onPause();
        view.onPause();
    }

    protected void onResume() {
        super.onResume();
        view.onResume();
    }
}
