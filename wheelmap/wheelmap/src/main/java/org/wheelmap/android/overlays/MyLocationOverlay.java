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

import android.graphics.Paint;
import android.graphics.Paint.Style;

public class MyLocationOverlay extends CircleOverlay<OverlayCircle> {
	OverlayCircle mCircleLarge, mCircleSmall, mCircleMed;
	private int itemCount = 0;
	private final static float RADIUS_SMALL_CIRCLE = 2.0f;
	private final static float RADIUS_MED_CIRCLE = 8.0f;

	public MyLocationOverlay() {
		super(null, null);

		Paint fillPaintDark = new Paint(Paint.ANTI_ALIAS_FLAG);
		fillPaintDark.setARGB(60, 127, 159, 239);

		Paint outlinePaintDark = new Paint(Paint.ANTI_ALIAS_FLAG);
		outlinePaintDark.setARGB(255, 79, 92, 140);
		outlinePaintDark.setStrokeWidth(4);
		outlinePaintDark.setStyle(Style.STROKE);

		Paint fillPaintLight = new Paint(Paint.ANTI_ALIAS_FLAG);
		fillPaintLight.setARGB(255, 47, 111, 223);

		Paint outlinePaintLight = new Paint(Paint.ANTI_ALIAS_FLAG);
		outlinePaintLight.setARGB(255, 132, 132, 132);
		outlinePaintLight.setStrokeWidth(10);
		outlinePaintLight.setStyle(Style.STROKE);

		mCircleLarge = new OverlayCircle(fillPaintDark, outlinePaintDark);
		mCircleSmall = new OverlayCircle(fillPaintLight, outlinePaintLight);
		mCircleMed = new OverlayCircle(fillPaintDark, outlinePaintDark);
	}

	public void setLocation(GeoPoint center, float radius) {
		mCircleLarge.setCircleData(center, radius);
		mCircleSmall.setCircleData(center, RADIUS_SMALL_CIRCLE);
		itemCount = 2;
		populate();
	}

	public void setItem(GeoPoint center) {
		mCircleMed.setCircleData(center, RADIUS_MED_CIRCLE);
		itemCount = 3;
		populate();
	}

	public void unsetItem() {
		itemCount = 2;
		populate();
	}

	@Override
	public int size() {
		return itemCount;
	}

	@Override
	protected OverlayCircle createCircle(int i) {
		if (i == 0)
			return mCircleLarge;
		else if (i == 1)
			return mCircleSmall;
		else
			return mCircleMed;
	}
}