package calculator;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.util.Log;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.content.Intent;

import java.util.ArrayList;

import arity.calculator.R;

import org.javia.arity.*;

public class ShowGraph extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph);
    }

    public void onPause() {
        super.onPause();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
        /*
	MenuInflater inflater = new MenuInflater(this);
	inflater.inflate(R.menu.main, menu);
        return true;
        */
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
	return super.onOptionsItemSelected(item);
        /*
	int id = item.getItemId();
	switch (id) {
	}
        */
    }
}
