package org.wheelmap.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;


public class SquareTextView extends TextView {

    public SquareTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareTextView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ViewGroup.LayoutParams p = getLayoutParams();
        int height = Math.max((int)(getMeasuredWidth()*0.7),getMeasuredHeight());
        setMeasuredDimension(getMeasuredWidth(),height);
    }
}
