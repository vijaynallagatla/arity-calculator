// Copyright (C) 2009 Mihai Preda

package calculator;

import android.view.ViewGroup;
import android.view.View;
import android.view.LayoutInflater;

import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.TextView;

import arity.calculator.R;

class HistoryAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private History history;

    static class TagData {
        TextView input;
        TextView result;
    }

    HistoryAdapter(Context context, History history) {
        this.history = history;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return history.size;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int pos, View view, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.history_line, parent, false);
            TagData tag = new TagData();
            tag.input = (TextView) view.findViewById(R.id.input);
            tag.result = (TextView) view.findViewById(R.id.result);
            view.setTag(tag);
        }
        TagData tag = (TagData) view.getTag();
        int revPos = history.size - pos - 1;
        tag.input.setText(history.lines[revPos]);
        tag.result.setText("" + revPos);
        return view;
    }

    /*
    public boolean hasStableIds() {
        return true;
    }
    */
}
