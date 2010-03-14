// Copyright (C) 2009 Mihai Preda

package calculator;

import org.javia.arity.Function;
import android.widget.ZoomButtonsController;

interface Grapher extends ZoomButtonsController.OnZoomListener {
    public void setFunction(Function f);
    public void onPause();
    public void onResume();
    public String captureScreenshot();
}
