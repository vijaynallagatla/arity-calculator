package calculator;

import android.view.MotionEvent;
import android.os.Build;

class MotionEventWrap {
    private static final boolean IS_API_5 = getSdkVersion() >= 5;

    static int getSdkVersion() {
        try {
            return Integer.parseInt(Build.VERSION.SDK);
        } catch (NumberFormatException e) {
            Calculator.log("invalid SDK " + Build.VERSION.SDK);
            return 3;
        }
    }

    static int getPointerCount(MotionEvent event) {
        return IS_API_5 ? MotionEventWrapNew.getPointerCount(event) : 1;
    }

    static float getX(MotionEvent event, int idx) {
        return IS_API_5 ? MotionEventWrapNew.getX(event, idx) : 0;
    }

    static float getY(MotionEvent event, int idx) {
        return IS_API_5 ? MotionEventWrapNew.getX(event, idx) : 0;
    }
}
