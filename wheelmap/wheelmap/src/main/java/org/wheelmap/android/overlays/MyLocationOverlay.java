package org.wheelmap.android.overlays;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.overlay.CircleOverlay;
import org.mapsforge.android.maps.overlay.OverlayCircle;

import android.graphics.Paint;
import android.graphics.Paint.Style;

public class MyLocationOverlay extends CircleOverlay<OverlayCircle> {
	OverlayCircle mCircleLarge, mCircleSmall;
	private final static float RADIUS_SMALL_CIRCLE = 2.0f;
	private final static int NUMBER_OF_CIRCLES = 2;

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
	}

	public void setLocation(GeoPoint center, float radius) {
		mCircleLarge.setCircleData(center, radius);
		mCircleSmall.setCircleData(center, RADIUS_SMALL_CIRCLE);
		populate();
	}

	@Override
	public int size() {
		return NUMBER_OF_CIRCLES;
	}

	@Override
	protected OverlayCircle createCircle(int i) {
		if (i == 1)
			return mCircleLarge;
		else
			return mCircleSmall;
	}
}