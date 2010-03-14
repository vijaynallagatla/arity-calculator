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
    private float zoomLevel = 1, targetZoom, zoomStep = 0;

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
            if (canZoomIn(zoomLevel)) {
                targetZoom = zoomLevel * .625f;
                zoomStep = -zoomLevel / 40;
                changed = true;
            }
        } else {
            if (canZoomOut(zoomLevel)) {
                targetZoom = zoomLevel * 1.6f;
                zoomStep = zoomLevel / 20;
                changed = true;
            }
        }
        if (changed) {
            zoomController.setZoomInEnabled(canZoomIn(targetZoom));
            zoomController.setZoomOutEnabled(canZoomOut(targetZoom));
            if (!renderer.shouldRotate()) {
                renderer.setRotation(0, 0);
            }
            startLooping();
        }
    }

    @Override
    protected void myDraw() {
        if ((zoomStep < 0 && zoomLevel > targetZoom) ||
            (zoomStep > 0 && zoomLevel < targetZoom)) {
            zoomLevel += zoomStep;
            renderer.setZoom(zoomLevel);
        } else if (zoomStep != 0) {
            zoomStep = 0;
            zoomLevel = targetZoom;
            renderer.isDirty = true;
            if (!renderer.shouldRotate()) {
                stopLooping();
            }
        }
        super.myDraw();
    }

    private boolean canZoomIn(float zoom) {
        return zoom > .2f;
    }

    private boolean canZoomOut(float zoom) {
        return zoom < 5;
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
            if (renderer.shouldRotate()) {
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
