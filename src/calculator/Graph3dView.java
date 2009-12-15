// Copyright (C) 2009 Mihai Preda

package calculator;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.opengl.GLSurfaceView.Renderer;
import org.javia.arity.Function;

public class Graph3dView extends GLView implements Grapher {
    private float lastTouchX, lastTouchY;
    private VelocityTracker velocityTracker;
    private boolean isRotating = true;
    private boolean isFullScreen;
    private GraphRenderer renderer = new GraphRenderer();

    private Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                myDraw();
            }
        };

    public Graph3dView(Context context, AttributeSet attrs) {
        super(context, attrs);
        isFullScreen = false;
        init();
    }

    public Graph3dView(Context context) {
        super(context);
        isFullScreen = true;
        init();
    }

    private void init() {
        setRenderer(renderer);
        isRotating = true;
    }

    public void setFunction(Function f) {
        renderer.setFunction(f);
    }

    private void requestDraw() {
        if (!handler.hasMessages(1)) {
            handler.sendEmptyMessage(1);
        }
    }

    protected void myDraw() {
        super.myDraw();
        if (isRotating) {
            requestDraw();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        // Calculator.log("touch " + event);
        if (!isFullScreen) {
            // isRotating = false;
            return super.onTouchEvent(event);
        }

        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            isRotating = false;
            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain();
            }
            velocityTracker.addMovement(event);
            lastTouchX = x;
            lastTouchY = y;
            break;

        case MotionEvent.ACTION_MOVE:
            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain();
            }
            velocityTracker.addMovement(event);
            float deltaX = x - lastTouchX;
            float deltaY = y - lastTouchY;
            if (deltaX > 1 || deltaX < -1 || deltaY > 1 || deltaY < -1) {
                renderer.setRotation(deltaX, deltaY);
                myDraw();
                lastTouchX = x;
                lastTouchY = y;
            }
            break;
            
        case MotionEvent.ACTION_UP:
            velocityTracker.computeCurrentVelocity(1000);
            float vx = velocityTracker.getXVelocity();
            float vy = velocityTracker.getYVelocity();
            // Calculator.log("velocity " + vx + ' ' + vy);
            final float limit = 50;
            isRotating = vx < -limit || vx > limit || vy < -limit || vy > limit;
            renderer.setRotation(vx/100, vy/100);
            if (isRotating) {
                requestDraw();
            }
            // no break

        case MotionEvent.ACTION_CANCEL:
            if (velocityTracker != null) {
                velocityTracker.recycle();
                velocityTracker = null;
            }
            break;

        default:
            // Calculator.log("touch action " + action + ' ' + event);
        }
        return true;
    }
}
