// Copyright (C) 2009 Mihai Preda

package calculator;

import android.view.View;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Scroller;

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
    private Paint paint = new Paint(), textPaint = new Paint(), fillPaint = new Paint();
    private Function function;
    private Data next = new Data(), graph = new Data(), endGraph = new Data();
    private float gwidth = 8;
    private float currentX = 0;
    private boolean isFullScreen;
    private VelocityTracker velocityTracker;
    private Scroller scroller;
    private boolean active;
    private float lastTouchX;

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
        paint.setAntiAlias(false);
        textPaint.setAntiAlias(true);
    }

    void setFunction(Function f, boolean isFullScreen) {
        this.function = f;
        this.isFullScreen = isFullScreen;
        graph.clear();
    }

    protected void onSizeChanged(int w, int h, int ow, int oh) {
        width = w;
        height = h;
        graph.clear();
    }

    protected void onDraw(Canvas canvas) {
        if (function == null) {
            return;
        }
        if (scroller.computeScrollOffset()) {
            currentX = scroller.getCurrX() * gwidth / width;
            invalidate();
        }
        drawGraph(canvas);
    }

    private static final float 
        NEG_INF = Float.NEGATIVE_INFINITY, 
        POS_INF = Float.POSITIVE_INFINITY;

    private float eval(float x) {
        float v = (float) function.eval(x);
        if (v == NEG_INF) {
            return -10000f;
        }
        if (v == POS_INF) {
            return 10000f;
        }
        return v;
    }

    private Data computeGraph(float minX, float maxX, float minY, float maxY) {
        long t1 = System.currentTimeMillis();
        final float scale = width / gwidth;
        final float maxStep = Math.min(15.8976f / scale, 1.373f);
        final float minStep = .2f / scale;
        // Calculator.log("step min " + minStep + " max " + maxStep);
        final float ythresh = 2/scale;
        final float ythresh2 = 2 * ythresh;
        next.clear();
        endGraph.clear();
        if (!graph.empty()) {
            if (maxX > graph.topX()) {
                graph.eraseBefore(minX);
            } else {
                graph.eraseAfter(maxX);
                maxX = graph.firstX();
                Data save = endGraph;
                endGraph = graph;
                graph = save;
            }
        }
        if (graph.empty()) {
            graph.push(minX, eval(minX));
        }
        float leftX = graph.topX();
        float leftY = graph.topY();
        float rightX = 0, rightY = 0;
        boolean advance = false;
        int nEval = 1;
        while (true) {
            if (advance) {
                leftX = rightX;
                leftY = rightY;
                next.pop();
            }
            advance = true;
            if (next.empty()) {
                float x = leftX + maxStep;
                next.push(x, eval(x));
                ++nEval;
            }
            rightX = next.topX();
            rightY = next.topY();
            if (leftX > maxX) {
                break;
            }
            if (leftY != leftY && rightY != rightY) { // NaN
                continue;
            }
            if ((leftY < minY && rightY > maxY) || 
                (leftY > maxY && rightY < minY)) {
                graph.push(rightX, Float.NaN);
                graph.push(rightX, rightY);
                continue;
            }
            float span = rightX - leftX;
            if (span <= minStep ||
                (leftY < minY && rightY < minY) ||
                (leftY > maxY && rightY > maxY)) {
                graph.push(rightX, rightY);
                continue;
            }
            float middleX = (leftX + rightX) / 2;
            float middleY = eval(middleX);
            ++nEval;
            if ((leftY < minY && middleY > maxY) ||
                (leftY > maxY && middleY < minY)) {
                graph.push(middleX, Float.NaN);
                graph.push(middleX, middleY);
                leftX = middleX;
                leftY = middleY;
                advance = false;
                continue;
            }
            float diff = Math.abs(leftY + rightY - middleY - middleY);
            if (diff < ythresh) {
                graph.push(rightX, rightY);
            } else if (diff < ythresh2) {
                graph.push(middleX, middleY);
                graph.push(rightX, rightY);
            } else {
                next.push(middleX, middleY);
                advance = false;
            }
        }
        if (!endGraph.empty()) {
            graph.append(endGraph);
        }
        long t2 = System.currentTimeMillis();
        // Calculator.log("graph points " + graph.size + " evals " + nEval + " time " + (t2-t1));
        return graph;
    }
    
    private static Path path = new Path();
    private Path graphToPath(Data graph) {
        boolean first = true;
        int size = graph.size;
        float[] xs = graph.xs;
        float[] ys = graph.ys;
        path.rewind();
        for (int i = 0; i < size; ++i) {
            float y = ys[i];
            float x = xs[i];
            // Calculator.log("path " + x + ' ' + y);
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

    private static final float NTICKS = 15;
    private static float stepFactor(float w) {
        float f = 1;
        while (w / f > NTICKS) {
            f *= 10;
        }
        while (w / f < NTICKS / 10) {
            f /= 10;
        }
        float r = w / f;
        if (r < NTICKS / 5) {
            return f / 5;
        } else if (r < NTICKS / 2) {
            return f / 2;
        } else {
            return f;
        }
    }

    /*
    private static StringBuilder builder = new StringBuilder();
    private static StringBuilder format(float v) {
        builder.setLength(0);
        if (Math.abs(v - (int)v) < .001f) {
            int rv = Math.round(v);
            if (rv != 0) {
                builder.append(Math.round(v));
            }
        } else {
            v *= 10;
            if (Math.abs(v - (int) v) < .001f) {
                builder.append(Math.round(v)/10.);
            } else {
                builder.append(Math.round(v*10)/100.);
            }
        }
        return builder;
    }
    */

    private static String format(float v) {
        if (Math.abs(v - (int)v) < .001f) {
            int rv = Math.round(v);
            return rv == 0 ? "" : Integer.toString(rv);
        }
        v *= 10;
        if (Math.abs(v - (int) v) < .001f) {
            return Float.toString(Math.round(v)/10f);
        }
        return Float.toString(Math.round(v*10)/100f);
    }

    private void drawGraph(Canvas canvas) {
        float minX = currentX - gwidth/2;
        float maxX = minX + gwidth;
        float maxY = gwidth * height / (width*2);
        float minY = -maxY;
        Data graph = computeGraph(minX, maxX, minY, maxY);
        Path path = graphToPath(graph);  

        canvas.drawColor(0xffffffff);
                
        paint.setStrokeWidth(0);
        paint.setAntiAlias(false);
        paint.setStyle(Paint.Style.STROKE);

        final float h2 = height/2f;
        final float scale = width / gwidth;
        
        float x0 = -minX * scale;
        boolean drawYAxis = true;
        if (x0 < 15) {
            x0 = 15;
            drawYAxis = false;
        } else if (x0 > width - 3) {
            x0 = width - 3;
            drawYAxis = false;
        }

        final float tickSize = 3;
        final float y1 = h2 - tickSize;
        final float y2 = h2 + tickSize;
        paint.setColor(0xffd0ffd0);
        float step = stepFactor(gwidth);
        // Calculator.log("width " + gwidth + " step " + step);
        float v = ((int) (minX/step)) * step;
        textPaint.setColor(0xff00b000);
        textPaint.setTextSize(10);
        textPaint.setTextAlign(Paint.Align.CENTER);
        float stepScale = step * scale;
        for (float x = (v - minX) * scale; x <= width; x += stepScale, v += step) {
            canvas.drawLine(x, 0, x, height, paint);
            if (v != 0) {
                canvas.drawText(format(v), x, y2+10, textPaint);
            }
        }
        
        final float x1 = x0 - tickSize;
        final float x2 = x0 + tickSize;
        v = ((int) (minY/step)) * step;
        textPaint.setTextAlign(Paint.Align.RIGHT);
        for (float y = height - (v - minY) * scale; y >= 0; y -= stepScale, v += step) {
            canvas.drawLine(0, y, width, y, paint);
            if (v != 0) {
                canvas.drawText(format(v), x1, y+4, textPaint);
            }
        }

        paint.setColor(0xff00d000);
        if (drawYAxis) {
            canvas.drawLine(x0, 0, x0, height, paint);
        }
        canvas.drawLine(0, h2, width, h2, paint);

        matrix.reset();
        matrix.preTranslate(-currentX, 0);
        matrix.postScale(scale, -scale);
        matrix.postTranslate(width/2, height/2);

        paint.setColor(0xff000000);
        paint.setStrokeWidth(0);
        paint.setAntiAlias(true);
        path.transform(matrix);
        canvas.drawPath(path, paint);
        
        if (isFullScreen) {
            fillPaint.setColor(0x10000000);
            canvas.drawRect(width-130, height-60, width-30, height-20, fillPaint);
            textPaint.setColor(0xc0000000);
            textPaint.setTextSize(22);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("\u2212", width-105, height-30, textPaint);
            canvas.drawText("+", width-55, height-30, textPaint);
        }
    }

    private void zoomOut() {
        if (gwidth < 50) {
            gwidth *= 2;
            invalidate();
            graph.clear();
        }
    }

    private void zoomIn() {
        if (gwidth > 1f) {
            gwidth /= 2;
            invalidate();
            graph.clear();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!isFullScreen) {
            return super.onTouchEvent(event);
        }

        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        if (!active && action != MotionEvent.ACTION_DOWN) {
            return true;
        }        
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            if (!scroller.isFinished()) {
                scroller.abortAnimation();
            }
            active = false;
            if (y > height-60 && y < height-10) {
                if (x > width - 140 && x < width-80) {
                    zoomOut();
                    return true;
                } else if (x > width-80 && x < width-20) {
                    zoomIn();
                    return true;
                }
            }
            active = true;
            velocityTracker = VelocityTracker.obtain();
            velocityTracker.addMovement(event);
            lastTouchX = x;
            break;

        case MotionEvent.ACTION_MOVE:
            velocityTracker.addMovement(event);
            float deltaPix = x - lastTouchX;
            if (deltaPix > 2 || deltaPix < -2) {
                scroll(-deltaPix);
                lastTouchX = x;
                invalidate();
            }
            break;

        case MotionEvent.ACTION_UP:
            velocityTracker.computeCurrentVelocity(1000);
            int speed = Math.round(velocityTracker.getXVelocity());            
            scroller.fling(Math.round(currentX * width / gwidth), 0, -speed, 0, -10000, 10000, 0, 0);
            invalidate();
            // no break

        default:
            if (velocityTracker != null) {
                velocityTracker.recycle();
                velocityTracker = null;
            }
            
        }
        return true;
    }

    private void scroll(float deltaPix) {
        float scale = gwidth / width;
        float delta = deltaPix * scale;
        currentX += delta;
        invalidate();
    }
}
