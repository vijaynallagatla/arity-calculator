package calculator;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class ListBuiltins extends ListActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	
        setListAdapter(
	    new ArrayAdapter<String>(this,
	    android.R.layout.simple_list_item_1, getIntent().getStringArrayExtra("")));
    }   
}
