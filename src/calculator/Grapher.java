// Copyright (C) 2009 Mihai Preda

package calculator;

import org.javia.arity.Function;

interface Grapher {
    public void setFunction(Function f);
    public void onPause();
    public void onResume();    
}
