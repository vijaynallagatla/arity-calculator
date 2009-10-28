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
    private Function function;
    private Data next = new Data(), graph = new Data();
    private boolean invalidated = true;
    private Bitmap bitmap;

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(false);        
    }

    void setFunction(Function f) {
        this.function = f;
        invalidated = true;
    }

    protected void onSizeChanged(int w, int h, int ow, int oh) {
        width = w;
        height = h;
        invalidated = true;
        bitmap = null;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (function == null) {
            return;
        }
        if (invalidated) {
            drawBitmap();
        }
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    private Data computeGraph(float minX, float maxX, float minY, float maxY) {
        final float scale = width / (maxX - minX);
        final float maxStep = 16 / scale;
        final float minStep = .1f / scale;
        Calculator.log("step min " + minStep + " max " + maxStep);
        final float ythresh = 1/scale;
        next.clear();
        graph.clear();
        float leftX = minX, leftY = (float) function.eval(minX);
        graph.push(leftX, leftY);
        float rightX, rightY;
        while (true) {
            if (next.empty()) {
                rightX = leftX + maxStep;
                rightY = (float) function.eval(rightX);
            } else {
                rightX = next.topX();
                rightY = next.topY();
                next.pop();
            }
            if (leftX > maxX) {
                break;
            }
            if (leftY != leftY && rightY != rightY) { // NaN
                leftX = rightX;
                leftY = rightY;
                continue;
            }
            float span = rightX - leftX;
            if (span <= minStep ||
                (leftY < minY && rightY < minY) ||
                (leftY > maxY && rightY > maxY)) {
                graph.push(rightX, rightY);
                leftX = rightX;
                leftY = rightY;
                continue;
            }
            if ((leftY < -100 && rightY > 100) || 
                (leftY > 100 && rightY < -100)) {
                graph.push(rightX, Float.NaN);
                graph.push(rightX, rightY);
                leftX = rightX;
                leftY = rightY;
                continue;
            }

            float middleX = (leftX + rightX) / 2;
            float middleY = (float) function.eval(middleX);
            float diff = Math.abs(leftY + rightY - middleY - middleY);
            if (diff < ythresh || 
                (leftY < minY && middleY < minY && rightY < minY) ||
                (leftY > maxY && middleY > maxY && rightY > maxY)) {
                graph.push(rightX, rightY);
                leftX = rightX;
                leftY = rightY;
            } else {
                next.push(middleX, middleY);
            }
        }
        return graph;
    }

    private Path path = new Path();
    private Path graphToPath(Data graph) {
        boolean first = true;
        int size = graph.size;
        float[] xs = graph.xs;
        float[] ys = graph.ys;
        path.rewind();
        for (int i = 0; i < size; ++i) {
            float y = ys[i];
            float x = xs[i];
            // Calculator.log("" + x + ' ' + y);
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
        }
        return path;
    }

    private void drawBitmap() {
        invalidated = false;

        float maxX = findBestSizeX(function, width, height);
        float minX = -maxX;
        float maxY = maxX * height / width;
        float minY = -maxY;
        Data graph = computeGraph(minX, maxX, minY, maxY);
        Path path = graphToPath(graph);        

        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        }
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(0xffffffff);
                
        paint.setColor(0xffa0ffa0);
        paint.setStrokeWidth(0);
        paint.setAntiAlias(false);
        paint.setStyle(Paint.Style.STROKE);

        final float scale = width / (maxX - minX);
        matrix.reset();
        matrix.preScale(scale, -scale);
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
    }

    private static boolean isGood(float v, float minY, float maxY) {
        return minY <= v && v <= maxY;
    }

    private static float goodness(float a, float b, float c, float minY, float maxY) {
        return (isGood(a, minY, maxY) && isGood(b, minY, maxY) && isGood(c, minY, maxY)) ? 
            Math.abs(a + c - b - b) : 0;
    }

    private static final int N = 20;
    private static float goodness(Function f, float minX, float maxX, float minY, float maxY) {
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

    private static final float VARS[] = {1, 2, 3, 5, 10};
    private static float findBestSizeX(Function f, int width, int height) {
        float bestGood = -1;
        float bestX = -1;
        final float ratio = height / (float) width;
        for (float maxX : VARS) {
            float maxY = maxX * ratio;
            float good = goodness(f, -maxX, maxX, -maxY, maxY);
            // Calculator.log("good " + maxX + ' ' + maxY + ' ' + good);
            if (good >= bestGood) {
                bestGood = good;
                bestX = maxX;
            }
        }
        return bestX;
    }
}
