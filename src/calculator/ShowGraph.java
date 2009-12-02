package calculator;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import arity.calculator.R;

public class ShowGraph extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph);
        GraphView graphView = (GraphView) findViewById(R.id.graph);
        graphView.setFunction(Calculator.graphedFunction, true);
    }

    /*
    public void onPause() {
        super.onPause();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
	MenuInflater inflater = new MenuInflater(this);
	inflater.inflate(R.menu.main, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
	return super.onOptionsItemSelected(item);
	int id = item.getItemId();
	switch (id) {
	}
    }
    */
}
