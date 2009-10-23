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

public class GraphView extends View {
    private int width, height;
    private Matrix matrix = new Matrix();
    private Paint paint = new Paint();

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(false);
    }

    protected void onSizeChanged(int w, int h, int ow, int oh) {
        width = w;
        height = h;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(0xffffffff);
        paint.setColor(0xff808080);
        paint.setStrokeWidth(2);
        canvas.drawLine(width/2, 0, width/2, height, paint);
        canvas.drawLine(0, height/2, width, height/2, paint);

        matrix.reset();                
    }
}
