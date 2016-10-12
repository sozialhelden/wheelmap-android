package org.wheelmap.android.view.progress;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

class Utils {

    public static float dpToPx(Context context, int dp){
        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

}
