package org.wheelmap.android.view;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

public class MyLayerDrawable extends LayerDrawable {

    public MyLayerDrawable(Drawable[] drawables){
         super(drawables);
    }

    @Override
    public int getIntrinsicHeight() {
        return getDrawable(0).getIntrinsicHeight();
    }

    @Override
    public int getIntrinsicWidth() {
        return getDrawable(0).getIntrinsicWidth();
    }

    @Override
    public int getMinimumHeight() {
        return getDrawable(0).getMinimumHeight();
    }

    @Override
    public int getMinimumWidth() {
        return getDrawable(0).getMinimumWidth();
    }
}
