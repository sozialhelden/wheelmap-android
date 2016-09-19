/*
 * #%L
 * Wheelmap - App
 * %%
 * Copyright (C) 2011 - 2012 Michal Harakal - Michael Kroez - Sozialhelden e.V.
 * %%
 * Wheelmap App based on the Wheelmap Service by Sozialhelden e.V.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wheelmap.android.utils;

import android.content.res.ColorStateList;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.TintableBackgroundView;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import org.wheelmap.android.online.R;

public class ViewTool {


    public static void setBackgroundTint(View v, @ColorInt int color) {
        ColorStateList csl = new ColorStateList(new int[][]{new int[0]}, new int[]{color});
        setBackgroundTintList(v, csl);
    }

    /**
     * workaround to solve bug from lollipop
     * http://stackoverflow.com/questions/27735890/lollipops-backgroundtint-has-no-effect-on-a-button
     */
    public static void setBackgroundTintList(View v, ColorStateList csl) {

        // the setBackgroundTintList method is buggy on lollipop
        // fallback to support method
        if (v instanceof TintableBackgroundView && Build.VERSION.SDK_INT <= 21) {
            ((TintableBackgroundView) v).setSupportBackgroundTintList(csl);
        } else {
            // do whatever ViewCompat think is right
            ViewCompat.setBackgroundTintList(v, csl);
        }

    }

    public static void nullViewDrawables(View view) {
        if (view != null) {
            try {
                ViewGroup viewGroup = (ViewGroup) view;

                int childCount = viewGroup.getChildCount();
                for (int index = 0; index < childCount; index++) {
                    View child = viewGroup.getChildAt(index);
                    nullViewDrawables(child);
                }
            } catch (Exception e) {
            }

            nullViewDrawableSingle(view);
        }
    }

    private static void nullViewDrawableSingle(View view) {
        try {
            view.setBackgroundDrawable(null);
        } catch (Exception e) {
        }

        try {
            ImageView imageView = (ImageView) view;
            imageView.setImageDrawable(null);
        } catch (Exception e) {
        }
    }

    public static void logMemory() {
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        Log.d("viewtool", "memory: totalMemory = " + totalMemory + " freeMemory = "
                + freeMemory);

    }

    public static void setAlphaForView(View alphaView, float alpha) {

        AlphaAnimation animation = new AlphaAnimation(alpha, alpha);

        animation.setDuration(0);
        animation.setFillAfter(true);

        alphaView.startAnimation(animation);
    }
}
