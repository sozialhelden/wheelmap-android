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
package org.wheelmap.android.overlays;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.overlay.CircleOverlay;
import org.mapsforge.android.maps.overlay.OverlayCircle;
import org.wheelmap.android.online.R;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.Style;

import java.util.HashSet;
import java.util.Set;

@Deprecated
public class MyLocationOverlay extends CircleOverlay<OverlayCircle> {

    private Set<OverlayCircle> mCircles = new HashSet<OverlayCircle>();

    private OverlayCircle mCircleLarge, mCircleSmall, mCircleMed;

    private final static float RADIUS_SMALL_CIRCLE = 2.0f;

    private final static float RADIUS_MED_CIRCLE = 8.0f;

    public MyLocationOverlay(Context context) {
        super(null, null);

        Paint fillPaintDark = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaintDark.setColor(context.getResources().getColor(
                R.color.position_marker_fill_blue_dark));

        Paint outlinePaintDark = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaintDark.setColor(context.getResources().getColor(
                R.color.position_marker_outline_blue_dark));
        outlinePaintDark.setStrokeWidth(4);
        outlinePaintDark.setStyle(Style.STROKE);

        Paint fillPaintLight = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaintLight.setColor(context.getResources().getColor(
                R.color.position_marker_fill_blue_light));

        Paint outlinePaintLight = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaintLight.setColor(context.getResources().getColor(
                R.color.position_marker_outline_blue_light));
        outlinePaintLight.setStrokeWidth(10);
        outlinePaintLight.setStyle(Style.STROKE);

        mCircleLarge = new OverlayCircle(fillPaintDark, outlinePaintDark);
        mCircleSmall = new OverlayCircle(fillPaintLight, outlinePaintLight);
        mCircleMed = new OverlayCircle(fillPaintDark, outlinePaintDark);
        mCircles.add(mCircleSmall);
        mCircles.add(mCircleLarge);
    }

    public void setLocation(GeoPoint center, float radius) {
        mCircleLarge.setCircleData(center, radius);
        mCircleSmall.setCircleData(center, RADIUS_SMALL_CIRCLE);
        populate();
    }

    public void setItem(GeoPoint center) {
        mCircleMed.setCircleData(center, RADIUS_MED_CIRCLE);
        mCircles.add(mCircleMed);
        populate();
    }

    public void unsetItem() {
        mCircles.remove(mCircleMed);
        populate();
    }

    @Override
    public int size() {
        return mCircles.size();
    }

    @Override
    protected OverlayCircle createCircle(int i) {
        if (i > mCircles.size() - 1) {
            return null;
        }

        return (OverlayCircle) mCircles.toArray()[i];
    }
}