package com.example.nhat.mapexample.others;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by Nhat on 10/28/2016.
 */

public class MapRootLayout extends FrameLayout {

    public MapRootLayout(Context context) {
        super(context);
    }

    public MapRootLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MapRootLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(isEnabled()) {
            return super.dispatchTouchEvent(ev);
        }
        return true;
    }
}
