// Copyright (C) 2009 Mihai Preda

package calculator;

import android.content.Context;
import java.io.*;

class History {
    private static final int VERSION = 1;
    private static final String FILENAME = "history";
    private int allocSize;
    private Context context;
    int size;

    String[] lines;
    
    History(Context context) {
        this.context = context;
        allocSize = 4;
        lines = new String[allocSize];
        size = 0;

        try {
            DataInputStream is = new DataInputStream(new BufferedInputStream(context.openFileInput(FILENAME), 256));
            int version = is.readInt();
            if (version != VERSION) {
                throw new IllegalStateException("invalid histoy version " + version);
            }
            int loadSize = is.readInt();
            for (int i = 0; i < loadSize; ++i) {
                add(is.readUTF());
            }
            is.close();
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
            os.writeInt(size);
            for (int i = 0; i < size; ++i) {
                os.writeUTF(lines[i]);
            }
            os.close();
        } catch (IOException e) {
            throw new RuntimeException("" + e);
        }
    }
  
    void add(String line) {
        if (size >= allocSize) {
            allocSize += allocSize; 
            String[] newLines = new String[allocSize];
            System.arraycopy(lines, 0, newLines, 0, size);
            lines = newLines;
        }
        lines[size++] = line;
    }
    
}
