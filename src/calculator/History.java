// Copyright (C) 2009 Mihai Preda

package calculator;

import android.content.Context;
import java.io.*;
import java.util.ArrayList;

class History {
    private static final int VERSION = 1;
    private static final String FILENAME = "history";
    private static final int SIZE_LIMIT = 50;
    private Context context;
    ArrayList<HistoryEntry> entries = new ArrayList<HistoryEntry>();
    private int pos;
    HistoryEntry aboveTop = new HistoryEntry("", "");
        
    History(Context context) {
        this.context = context;
        try {
            DataInputStream is = new DataInputStream(new BufferedInputStream(context.openFileInput(FILENAME), 256));
            int version = is.readInt();
            if (version != VERSION) {
                throw new IllegalStateException("invalid histoy version " + version);
            }
            aboveTop = new HistoryEntry(is);
            int loadSize = is.readInt();
            for (int i = 0; i < loadSize; ++i) {                
                entries.add(new HistoryEntry(is));
            }
            is.close();
            pos = entries.size();
        } catch (FileNotFoundException e) {
            // ignore
        } catch (IOException e) {
            throw new RuntimeException("" + e);
        }        
    }

    void save() {
        try {
            DataOutputStream os = new DataOutputStream(new BufferedOutputStream(context.openFileOutput(FILENAME, 0), 256));
            os.writeInt(VERSION);
            aboveTop.save(os);
            os.writeInt(entries.size());
            for (HistoryEntry entry : entries) {
                entry.save(os);
            }
            os.close();
        } catch (IOException e) {
            throw new RuntimeException("" + e);
        }
    }
    
    private HistoryEntry currentEntry() {
        if (pos < entries.size()) {
            return entries.get(pos);
        } else {
            return aboveTop;
        }
    }

    int getListPos() {
        return entries.size() - 1 - pos;
    }

    boolean onEnter(String text, String result) {
        currentEntry().onEnter();
        pos = entries.size();
        if (text.length() == 0) {
            return false;
        }
        if (entries.size() > 0) {
            HistoryEntry top = entries.get(entries.size()-1);
            if (text.equals(top.line) && result.equals(top.result)) {
                return false;
            }
        }
        if (entries.size() > SIZE_LIMIT) {
            entries.remove(0);
        }
        entries.add(new HistoryEntry(text, result));
        pos = entries.size();
        return true;
    }

    boolean moveUp(String text) {
        if (pos >= entries.size()) {
            return false;
        }
        currentEntry().editLine = text;
        ++pos;
        return true;
    }
    
    boolean moveDown(String text) {
        if (pos <= 0) {
            return false;
        }
        currentEntry().editLine = text;
        --pos;
        return true;
    }

    String getText() {
        return currentEntry().editLine;
    }    
}
