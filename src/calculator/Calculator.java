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
import android.text.TextWatcher;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.view.KeyEvent;
import android.view.View;
import android.util.Log;
import android.content.res.Resources;
import android.content.res.Configuration;

import arity.calculator.R;

import org.javia.arity.Symbols;
import org.javia.arity.SyntaxException;
import org.javia.arity.Util;

public class Calculator extends Activity implements TextWatcher, View.OnKeyListener
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
    private int nDigits = 0;

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
        input.setEditableFactory(new Factory());
            
        // input.setInputType(0);

        history = new History(this);
        adapter = new HistoryAdapter(this, history);
        
        historyView = (ListView) findViewById(R.id.history);
        if (historyView != null) {
            historyView.setAdapter(adapter);
        }
    }

    static class Factory extends Editable.Factory {
        public Editable newEditable(CharSequence source) {
            return new CalculatorEditable(source);
        }
    }

    static class CalculatorEditable extends SpannableStringBuilder {
        private static final char MINUS = '\u2212', TIMES = '\u00d7', DIV = '\u00f7';
        private boolean isRec;

        public CalculatorEditable(CharSequence source) {
            super(source);
        }

        public SpannableStringBuilder replace(int start, int end, CharSequence buf, int bufStart, int bufEnd) {
            if (isRec || bufEnd - bufStart != 1) {
                return super.replace(start, end, buf, bufStart, bufEnd);
            } else {
                isRec = true;                
                try {
                    char c = buf.charAt(bufStart);
                    return internalReplace(start, end, c);
                } finally {
                    isRec = false;
                }
            }
        }

        private boolean isOperator(char c) {
            return "\u2212\u00d7\u00f7+-/*".indexOf(c) != -1;
        }

        private SpannableStringBuilder internalReplace(int start, int end, char c) {
            switch (c) {
            case '-': c = MINUS; break;
            case '*': c = TIMES; break;
            case '/': c = DIV;   break;
            }
            if (c == '.') {
                int p = start - 1;
                while (p >= 0 && Character.isDigit(charAt(p))) {
                    --p;
                }
                if (p >= 0 && charAt(p) == '.') {
                    return super.replace(start, end, "");
                }
            }

            char prevChar = start > 0 ? charAt(start-1) : '\0';
            
            if (c == MINUS && prevChar == MINUS) {
                return super.replace(start, end, "");
            }
            
            if (isOperator(c)) {
                while (isOperator(prevChar) && 
                       (c != MINUS || prevChar == '+')) {
                    --start;
                    prevChar = start > 0 ? charAt(start-1) : '\0';
                }
            }
            
            //don't allow leading operator + * /
            if (start == 0 && isOperator(c) && c != MINUS) {
                return super.replace(start, end, "");
            }
            return super.replace(start, end, "" + c);
        }
    }

    public void onPause() {
        super.onPause();
        history.save();
    }

    // TextWatcher
    public void afterTextChanged(Editable s) {
        handler.removeMessages(MSG_INPUT_CHANGED);
        handler.sendEmptyMessageDelayed(MSG_INPUT_CHANGED, 250);
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

    private void evaluate() {
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
    
    private String evaluate(String text) {
        if (nDigits == 0) {
            nDigits = getResultSpace();
        }
        try {
            return Util.complexToString(symbols.evalComplex(text), nDigits, 2).replace(INFINITY, INFINITY_UNICODE);
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
        String text = input.getText().toString();
        if (history.onEnter(text, evaluate(text))) {
            adapter.notifyDataSetInvalidated();
        }
        input.setText(history.getText());
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
        if (history.moveUp(input.getText().toString())) {
            input.setText(history.getText());
            // updateChecked();
        }
    }

    void onDown() {
        if (history.moveDown(input.getText().toString())) {
            input.setText(history.getText());
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
