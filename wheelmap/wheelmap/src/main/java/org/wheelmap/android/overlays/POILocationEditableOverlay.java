package org.wheelmap.android.overlays;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.overlay.ItemizedOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;

import android.graphics.drawable.Drawable;

public class POILocationEditableOverlay extends ItemizedOverlay<OverlayItem> {

	private OverlayItem mItem;
	private int mItemCount;

	public POILocationEditableOverlay(double latitude, double longitude,
			Drawable marker) {
		super(null);
		ItemizedOverlay.boundCenterBottom(marker);
		mItem = new OverlayItem();
		mItem.setMarker(marker);
		mItem.setPoint(new GeoPoint(latitude, longitude));
		mItemCount = 1;
	}

	public void setPosition(GeoPoint geoPoint) {
		mItem.setPoint(geoPoint);
		populate();
	}

	@Override
	public int size() {
		return mItemCount;
	}

	@Override
	protected OverlayItem createItem(int index) {
		if (index > 0)
			return null;
		return mItem;
	}

}
