// Copyright (C) 2009 Mihai Preda
  
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

public class Calculator extends Activity implements TextWatcher, 
						    View.OnKeyListener,
						    AdapterView.OnItemClickListener
{
    private static final int MSG_INPUT_CHANGED = 1;
    private static final String INFINITY = "Infinity";
    private static final String INFINITY_UNICODE = "\u221e";

    private TextView result;
    private EditText input;
    private ListView historyView;
    private History history;
    private HistoryAdapter adapter;
    private Symbols symbols = new Symbols();
    private Defs defs;
    private int nDigits = 0;
    private boolean pendingClearResult;
    private String[] builtins;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Configuration config = getResources().getConfiguration();
        boolean isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE;
        boolean hasKeyboard = config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO;

        result = (TextView) findViewById(R.id.result);

        input  = (EditText) findViewById(R.id.input);
        input.setOnKeyListener(this);
        input.addTextChangedListener(this);
        input.setEditableFactory(new CalculatorEditable.Factory());
            
        // input.setInputType(0);

        history = new History(this);
        adapter = new HistoryAdapter(this, history);
	changeInput(history.getText());
        
        historyView = (ListView) findViewById(R.id.history);
        if (historyView != null) {
            historyView.setAdapter(adapter);
	    historyView.setOnItemClickListener(this);
        }

	Symbol[] syms = symbols.getTopFrame();
	int size = syms.length;
	builtins = new String[size];
	for (int i = 0; i < size; ++i) {
	    String s = syms[i].getName();
	    int arity = syms[i].getArity();
	    String args = arity==0 ? "" : arity==1 ? "(x)" : arity==2 ? "(x,y)" : "(x,y,z)";
	    builtins[i] = s + args;
	}
	symbols.pushFrame();
	defs = new Defs(this, symbols);
	if (history.pos == 0) {
	    String[] init = {
		"sqrt(pi)\u00f70.5!",
		"e^(i\u00d7pi)",
		"ln(e^100)",
		"bmi(w,h)=w/h^2",
		"bmi(75,1.82)",
	    };
	    nDigits = 10;
	    for (String s : init) {
		onEnter(s);
	    }
	    nDigits = 0;
	}
    }

    public void onPause() {
        super.onPause();
	onUp();
        history.save();
	defs.save();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
	MenuInflater inflater = new MenuInflater(this);
	inflater.inflate(R.menu.main, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
	super.onOptionsItemSelected(item);
	int id = item.getItemId();
	switch (id) {
	case R.id.list_defs: {
	    Intent i = new Intent(this, ListDefs.class);
	    i.putStringArrayListExtra("", defs.lines);
	    startActivity(i);
	    break;
	}

	case R.id.list_builtins: {
	    Intent i = new Intent(this, ListBuiltins.class);
	    //String[] list = symbols.getDictionary();
	    /*
	    for (Symbol symbol : predefined) {
		String str = symbol.getName() + ' ' + symbol.getArity() + ' ' + symbol.getComment();
		list.add(str);
	    }
	    */
	    i.putExtra("", builtins);
	    startActivity(i);
	    break;
	}
	    
	default:
	    return false;
	}
	return true;
    }

    // OnItemClickListener
    public void onItemClick(AdapterView parent, View view, int pos, long id) {
	history.moveToPos(pos, input.getText().toString());
	changeInput(history.getText());
    }
    
    // TextWatcher
    public void afterTextChanged(Editable s) {
        handler.removeMessages(MSG_INPUT_CHANGED);
        handler.sendEmptyMessageDelayed(MSG_INPUT_CHANGED, 250);
	if (pendingClearResult && s.length() != 0) {
	    result.setText(null);
	    pendingClearResult = false;
	}
    }
    
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }


    // OnKeyListener
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        log("key " + keyCode + ' ' + event);
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                doEnter();
                break;
                
            case KeyEvent.KEYCODE_DPAD_UP:
                onUp();
                break;
                
            case KeyEvent.KEYCODE_DPAD_DOWN:            
                onDown();
                break;
            default:
                return false;
            }
            return true;
        } else {
            switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return true;
            }
            return false;
        }
    }

    private Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {                    
                case MSG_INPUT_CHANGED:
                    // String text = input.getText().toString();
                    evaluate();
                }
            }
        };

    static void log(String mes) {
        Log.d("***", mes);
    }

    void evaluate() {
        String text = input.getText().toString();
        if (text.length() == 0) {
            return;
        }
        String res = evaluate(text);
        if (res != null) {
            result.setText(res);
            result.setEnabled(true);
        } else {
            result.setEnabled(false);
        }
    }
    
    private String formatEval(Complex value) {
	if (nDigits == 0) {
            nDigits = getResultSpace();
        }
	String res = Util.complexToString(value, nDigits, 2);
	return res.replace(INFINITY, INFINITY_UNICODE);
    }

    private String evaluate(String text) {
        try {
	    if (Symbols.isDefinition(text)) {
		Function f = symbols.compile(text);
		return f.arity()==0 ? formatEval(f.evalComplex()) : "function";
	    } else {
		return formatEval(symbols.evalComplex(text));
	    }
        } catch (SyntaxException e) {
            return null;
        }
    }

    private int getResultSpace() {
        int width = result.getWidth() - result.getTotalPaddingLeft() - result.getTotalPaddingRight();
        float oneDigitWidth = result.getPaint().measureText("5555555555") / 10f;
        return (int) (width / oneDigitWidth);
    }

    private StringBuilder oneChar = new StringBuilder(" ");
    void onKey(char key) {
        int cursor = input.getSelectionStart();
        oneChar.setCharAt(0, key);
        input.getText().insert(cursor, oneChar);
    }

    void onEnter() {
	onEnter(input.getText().toString());
    }

    void onEnter(String text) {
	boolean historyChanged = false;
	try {
	    FunctionAndName fan = symbols.compileWithName(text);
	    if (fan.name != null) {
		symbols.define(fan);
		defs.add(text);
	    }
	    Function f = fan.function;
	    historyChanged = f.arity() == 0 ?
		history.onEnter(text, formatEval(f.evalComplex())) :
		history.onEnter(text, null);
	} catch (SyntaxException e) {
	    historyChanged = history.onEnter(text, null);
	}
        if (historyChanged) {
            adapter.notifyDataSetInvalidated();
        }
	changeInput(history.getText());
    }
    
    private void changeInput(String newInput) {
        input.setText(newInput);
	input.setSelection(newInput.length());
	if (newInput.length() > 0) {
	    result.setText(null);
	} else {
	    pendingClearResult = true;
	}
    }
    
    /*
    private void updateChecked() {
        int pos = history.getListPos();
        if (pos >= 0) {
            log("check " + pos);
            historyView.setItemChecked(pos, true);
            adapter.notifyDataSetInvalidated();
        }
    }
    */

    void onUp() {
	log("up from " + history.pos);
        if (history.moveUp(input.getText().toString())) {
	    log("move up true");
            changeInput(history.getText());
            // updateChecked();
        }
    }

    void onDown() {
	log("down from " + history.pos);
        if (history.moveDown(input.getText().toString())) {
	    log("move down true");
            changeInput(history.getText());
            // updateChecked();
        }
    }
    
    private static final KeyEvent 
        KEY_DEL = new KeyEvent(0, KeyEvent.KEYCODE_DEL),
        KEY_ENTER = new KeyEvent(0, KeyEvent.KEYCODE_ENTER);

    void doEnter() {
        onEnter();
    }

    void doBackspace() {
        input.dispatchKeyEvent(KEY_DEL);
    }

    
}
