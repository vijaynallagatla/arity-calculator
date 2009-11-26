package calculator;

import android.opengl.GLSurfaceView;
import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.Scroller;
import arity.calculator.R;

public class Graph3dView extends GLSurfaceView {
    private GraphRenderer renderer;
    private float lastTouchX, lastTouchY;
    private VelocityTracker velocityTracker;
    private Scroller scroller;

    Graph3dView(Context context, Graph3d graph) {
        super(context);
        renderer = new GraphRenderer(graph);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setDebugFlags(1);
        scroller = new Scroller(context);
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            if (!scroller.isFinished()) {
                scroller.abortAnimation();
            }
            velocityTracker = VelocityTracker.obtain();
            velocityTracker.addMovement(event);
            lastTouchX = x;
            lastTouchY = y;
            break;

        case MotionEvent.ACTION_MOVE:
            velocityTracker.addMovement(event);
            float deltaX = x - lastTouchX;
            float deltaY = y - lastTouchY;
            if (deltaX > 1 || deltaX < -1 || deltaY > 1 || deltaY < -1) {
                renderer.incAngle(deltaX, deltaY);
                requestRender();
                lastTouchX = x;
                lastTouchY = y;
            }
            break;
            
        case MotionEvent.ACTION_UP:
            velocityTracker.computeCurrentVelocity(1000);
            scroller.fling(Math.round(x), Math.round(y), 
                           Math.round(velocityTracker.getXVelocity()), 
                           Math.round(velocityTracker.getYVelocity()),
                           -1000, 1000, -1000, 1000);
            requestRender();
            // no break

        default:
            if (velocityTracker != null) {
                velocityTracker.recycle();
                velocityTracker = null;
            }
        }
        return true;
    }
}
