// Copyright (C) 2009 Mihai Preda

package calculator;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.ZoomButtonsController;
import android.util.AttributeSet;
import android.opengl.GLSurfaceView.Renderer;
import org.javia.arity.Function;

public class Graph3dView extends GLView implements Grapher {
    private float lastTouchX, lastTouchY;
    private VelocityTracker velocityTracker;
    private boolean isFullScreen;
    private GraphRenderer renderer = new GraphRenderer();
    private ZoomButtonsController zoomController = new ZoomButtonsController(this);
    private float zoomLevel = 1;

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
        startLooping();
        zoomController.setOnZoomListener(this);
    }

    public void setFunction(Function f) {
        renderer.setFunction(f);
    }

    public void onVisibilityChanged(boolean visible) {
    }

    public void onZoom(boolean zoomIn) {
        boolean changed = false;
        if (zoomIn) {
            if (canZoomIn()) {
                zoomLevel = zoomLevel / 2;
                changed = true;
            }
        } else {
            if (canZoomOut()) {
                zoomLevel = zoomLevel + zoomLevel;
                changed = true;
            }
        }
        if (changed) {
            zoomController.setZoomInEnabled(canZoomIn());
            zoomController.setZoomOutEnabled(canZoomOut());
            renderer.setZoom(zoomLevel);
            if (!isLooping()) {
                requestDraw();
            }
        }
    }

    private boolean canZoomIn() {
        return zoomLevel > .3f;
    }

    private boolean canZoomOut() {
        return zoomLevel < 3;
    }

    @Override
    public void onDetachedFromWindow() {
        zoomController.setVisible(false);
        super.onDetachedFromWindow();
    }

    public boolean onTouchEvent(MotionEvent event) {
        // Calculator.log("touch " + event);
        if (!isFullScreen) {
            return super.onTouchEvent(event);
        }

        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            zoomController.setVisible(true);
            stopLooping();
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
            renderer.setRotation(vx/100, vy/100);
            final float limit = 50;
            if (vx < -limit || vx > limit || vy < -limit || vy > limit) {
                startLooping();
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
