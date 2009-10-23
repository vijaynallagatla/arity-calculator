// Copyright (C) 2009 Mihai Preda

package calculator;

import android.view.View;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.EditText;
import android.content.Context;
import android.text.Editable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Region;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import android.util.AttributeSet;

import android.util.Log;
import arity.calculator.R;

public class KeyboardView extends View {
    private char[][] keys;
    private int nLine, nCol;
    private Paint downPaint = new Paint();
    private int width, height;
    private Bitmap bitmap;
    private boolean isDown;
    private float downX, downY;
    private int downLine, downCol;
    private Rect rect = new Rect();
    private float cellw, cellh;
    private Calculator calculator;
    private KeyboardView aboveView;
    private boolean isLarge;

    public KeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        downPaint.setAntiAlias(false);
        downPaint.setColor(0xffffffff);
        downPaint.setStyle(Paint.Style.STROKE);
        calculator = (Calculator) context;        
    }
    
    void init(char[][] keys) {
        this.keys = keys;
        nLine = keys.length;
        nCol = keys[0].length;
    }

    void setAboveView(KeyboardView aboveView) {
        this.aboveView = aboveView;
    }
    
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        Calculator.log("size " + w + ' ' + h);
        width = w;
        height = h;

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        
        cellw = width / (float) nCol;
        cellh = height / (float) nLine;
        isLarge = cellw > 50;
        
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(isLarge ? 26 : 22);
        textPaint.setColor(0xffffffff);
        textPaint.setTextAlign(Paint.Align.CENTER);
        final float extraY = isLarge ? 10 : 8;

        Paint linePaint = new Paint();
        linePaint.setAntiAlias(false);
        for (int line = 0; line < nLine; ++line) {
            final float y1 = getY(line);
            final float y =  y1 + cellh/2 + extraY;
            char[] lineKeys = keys[line];
            for (int col = 0; col < nCol; ++col) {
                final float x1 = getX(col);
                final float x = x1 + cellw/2;
                final char c = lineKeys[col];
                final int backColor = (('a' <= c && c <= 'z') || c == Calculator.PI) ? 0xff303030 :
                    (('0' <= c && c <= '9') || c == '.') ? 0xff303030 :
                    (c == 'E' || c == 'C' || c == Calculator.ARROW) ? 0xff306060 :
                    (c == '+' || c == '\u2212' || c == '\u00d7' || c == '\u00f7') ? 0xff808080 :
                    0xffb0b0b0;
                linePaint.setColor(backColor);
                canvas.drawRect(x1, y1, x1+cellw, y1+cellh, linePaint);

                switch (c) {
                case 'E':
                    drawDrawable(canvas, R.drawable.enter, x1, y1);
                    break;

                case 'C':
                    drawDrawable(canvas, R.drawable.delete, x1, y1);
                    break;

                default:
                    // textPaint.setColor(('0' <= c && c <= '9') ? 0xffffff00 : 0xffffffff);
                    canvas.drawText(lineKeys, col, 1, x, y, textPaint);
                }
            }
        }

        linePaint.setStrokeWidth(0);
        linePaint.setColor(0xff000000);
        for (int line = 0; line <= nLine; ++line) {
            final float y = getY(line);
            canvas.drawLine(0, y, width, y, linePaint);
        }
        for (int col = 0; col <= nCol; ++col) {
            final float x = getX(col);
            canvas.drawLine(x, 0, x, height, linePaint);
        }
    }

    private void drawDrawable(Canvas canvas, int id, float x, float y) {
        Drawable d = calculator.getResources().getDrawable(id);
        int iw = d.getIntrinsicWidth();
        int ih = d.getIntrinsicHeight();
        int x1 = Math.round(x + (cellw - iw)/2.f);
        int y1 = Math.round(y + (cellh - ih)/2.f);
        d.setBounds(x1, y1, x1 + iw, y1 + ih);
        d.draw(canvas);
    }

    private float getY(int line) {
        return line * height / (float) nLine;
    }

    private float getX(int col) {
        return col * width / (float) nCol;
    }

    private int getLine(float y) {
        int line = (int) (y * nLine / height);
        if (line < 0) {
            line = 0;
        } else if (line >= nLine) {
            line = nLine - 1;
        }
        return line;
    }
    
    private int getCol(float x) {
        int col = (int) (x * nCol / width);
        if (col < 0) {
            col = 0;
        } else if (col >= nCol) {
            col = nCol - 1;
        }
        return col;
    }

    private void drawDown(Canvas canvas, float x, float y) {
        canvas.drawRect(x, y, x+cellw-.5f, y+cellh-.5f, downPaint);
    }

    /*
    private boolean hasLargeClip(Canvas canvas, float x1, float y1) {
        return !(canvas.getClipBounds(rect) 
                 && (int)x1 - rect.left <= 1 
                 && (int)y1 - rect.top <= 1 
                 && rect.right - (int)(x1 + cellw) <= 1
                 && rect.bottom - (int)(y1 + cellh) <= 1);
    }
    */

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (isDown) {
            float x1 = getX(downCol);
            float y1 = getY(downLine);
            //if (hasLargeClip(canvas, x1, y1)) {
            canvas.drawBitmap(bitmap, 0, 0, null);
            drawDown(canvas, x1, y1);
        } else {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
    }

    private static final float DELTAY = 8;
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            float y = event.getY();
            if (y < DELTAY && aboveView != null) {
                event.offsetLocation(0, aboveView.getHeight() - DELTAY);
                aboveView.onTouchEvent(event);
            } else {
                isDown = true;
                downY = y >= DELTAY ? y - DELTAY : 0;
                downLine = getLine(downY);
                downCol = getCol(downX);
                invalidateCell(downLine, downCol);
                char key = keys[downLine][downCol];
                calculator.onKey(key);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isDown) {
                isDown = false;
                invalidateCell(downLine, downCol);
            } else if (aboveView != null) {
                aboveView.onTouchEvent(event);
            }
        } else {
            return false;
        }
        return true;
    }   

    private void invalidateCell(int line, int col) {
        float x1 = getX(col);
        float y1 = getY(line);
        int x2 = (int)(x1+cellw);
        int y2 = (int)(y1+cellh);
        invalidate((int)x1, (int)y1, x2, y2);
        // log("invalidate " + x + ' '  + y + ' ' + ((int)x1) + ' ' + ((int)y1) + ' ' + x2 + ' ' + y2);
    }
}
