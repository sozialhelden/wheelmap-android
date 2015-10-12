package org.wheelmap.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class VerticalScrollView extends ScrollView{

    private GestureDetector mGestureDetector;

    public VerticalScrollView(Context context) {
        super(context);
        init(context);
    }

    public VerticalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VerticalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context){
        mGestureDetector = new GestureDetector(context,
                new VerticalScrollDetector(context));
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        boolean verticalScroll = mGestureDetector.onTouchEvent(ev);
        if(verticalScroll){
           return false;
        }

        return super.onInterceptTouchEvent(ev);
    }

    class VerticalScrollDetector extends GestureDetector.SimpleOnGestureListener {

        final float min_horizontal;
        final float max_vertical;

        public VerticalScrollDetector(Context context){
            float density = context.getResources().getDisplayMetrics().density;
            min_horizontal = 10 * density;
            max_vertical = 50 * density;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {



            if (Math.abs(distanceY) < max_vertical && Math.abs(distanceX) > min_horizontal) {
                return true;
            }

            if(distanceX > distanceY){
               return true;
            }

            return false;
        }
    }

}
