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

import org.javia.arity.*;

public class GraphView extends View {
    private int width, height;
    private Matrix matrix = new Matrix();
    private Paint paint = new Paint();
    // private Symbols symbols;
    // private float minX = -3, maxX = 3;
    static private int NPOINTS = 30;
    //private float ys[] = new float[NPOINTS];
    private Path path = new Path(), transPath = new Path();
    Function function;

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(false);
    }

    void setFunction(Function f) {
        this.function = f;
    }

    protected void onSizeChanged(int w, int h, int ow, int oh) {
        width = w;
        height = h;
    }

    private boolean isGood(float v, float minY, float maxY) {
        // return v != Float.NEGATIVE_INFINITY && v != Float.POSITIVE_INFINITY && v != Float.NaN;
        return minY <= v && v <= maxY;
        // Float.NEGATIVE_INFINITY < v && v < Float.POSITIVE_INFINITY &&
    }

    private float goodness(float a, float b, float c, float minY, float maxY) {
        return (isGood(a, minY, maxY) && isGood(b, minY, maxY) && isGood(c, minY, maxY)) ? 
            Math.abs(a + c - b - b) : 0;
    }

    private static final int N = 12;
    private float goodness(Function f, float minX, float maxX, float minY, float maxY) {
        float step = (maxX - minX) / (N - 1);
        float a, b = (float) f.eval(minX), c = (float) f.eval(minX + step);
        float x = minX + step + step;
        float sum = 0;
        for (int i = 0; i < N - 2; ++i, x += step) {
            a = b;
            b = c;
            c = (float) f.eval(x);
            sum += goodness(a, b, c, minY, maxY);
        }
        return sum / (maxY - minY);
    }

    private static final float VARS[] = {1, 3, 10};
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (function == null) {
            return;
        }

        int nvars = VARS.length;
        float bestGood = -1;
        float bestX = -1;
        for (int i = 0; i < nvars; ++i) {
            float maxX = VARS[i];
            float maxY = maxX * height / width;
            float good = goodness(function, -maxX, maxX, -maxY, maxY);
            Calculator.log("good " + maxX + ' ' + maxY + ' ' + good);
            if (good >= bestGood) {
                bestGood = good;
                bestX = maxX;
            }            
        }                
        float maxX = bestX;
        float minX = -maxX;
        float maxY = maxX * height / width;
        float minY = -maxY;

        canvas.drawColor(0xffffffff);
                
        //float minY = Float.POSITIVE_INFINITY;
        //float maxY = Float.NEGATIVE_INFINITY;

        paint.setColor(0xffa0ffa0);
        paint.setStrokeWidth(0);
        paint.setAntiAlias(false);
        paint.setStyle(Paint.Style.STROKE);

        path.rewind();
        final float scale = width / (maxX - minX);
        final float step = 5 / scale;
        boolean first = true;
        float a = Float.NaN, b = Float.NaN;
        for (float x = minX; ; x += step) {
            float y = (float) function.eval(x);
            //minY = y < minY ? y : minY;
            //maxY = y > maxY ? y : maxY;
            if (y == y) { // !NaN
                if (first) {
                    path.moveTo(x, y);
                    first = false;
                } else {
                    path.lineTo(x, y);
                }
            } else {
                first = true;
            }
            if (x > maxX) {
                break;
            }
        }

        // float scaley = scalex; // height / (maxY - minY);
        /*
        if (scaley > scalex && scaley/scalex < 1.5) {
            scaley = scalex;
        }
        */
        // Calculator.log("scale " + scalex + ' ' + scaley);


        matrix.reset();
        matrix.preScale(scale, -scale);
        // Calculator.log("size " + width + ' ' + height);
        matrix.postTranslate(width/2, height/2);
        canvas.concat(matrix);

        canvas.drawLine(0, minY, 0, maxY, paint);        
        canvas.drawLine(minX, 0, maxX, 0, paint);
        float tickSize = 3 / scale;
        paint.setColor(0xff00ff00);
        for (int i = (int) minX; i <= (int) maxX; ++i) {
            canvas.drawLine(i, -tickSize, i, tickSize, paint); 
        }
        for (int i = (int) minY; i <= (int) maxY; ++i) {
            canvas.drawLine(-tickSize, i, tickSize, i, paint);
        }
       
        paint.setColor(0xff000000);
        paint.setStrokeWidth(0);
        paint.setAntiAlias(true);
        canvas.drawPath(path, paint);

        /*
        paint.setColor(0xffff0000);
        canvas.drawPoint(10, 10, paint);
        canvas.drawPath(path, paint);
        canvas.drawLine(0, 0, 10, 5, paint);
        
        // Paint pt = new Paint();
        paint.setColor(0xff00ff00);
        Path p = new Path();
        p.moveTo(20, 20);
        p.lineTo(100, 50);
        p.lineTo(110, 100);
        canvas.drawPath(p, paint);
        */
    }
}
