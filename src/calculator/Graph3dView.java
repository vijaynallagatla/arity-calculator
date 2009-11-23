package calculator;

import android.opengl.GLSurfaceView;
import android.content.Context;
import android.view.MotionEvent;
import arity.calculator.R;

public class Graph3dView extends GLSurfaceView {
    private GraphRenderer renderer;
    private float lastTouchX, lastTouchY;

    Graph3dView(Context context, Graph3d graph) {
        super(context);
        renderer = new GraphRenderer(graph);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setDebugFlags(1);
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            lastTouchX = x;
            lastTouchY = y;
            break;

        case MotionEvent.ACTION_MOVE:
            float deltaX = x - lastTouchX;
            float deltaY = y - lastTouchY;
            lastTouchX = x;
            lastTouchY = y;
            renderer.incAngle(deltaX, deltaY);
            requestRender();
            break;
            
        case MotionEvent.ACTION_UP:
            // no break

        default:
        }
        return true;
    }
}
