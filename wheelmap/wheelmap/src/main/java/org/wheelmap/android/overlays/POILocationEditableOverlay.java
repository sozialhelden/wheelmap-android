package org.wheelmap.android.overlays;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.overlay.ItemizedOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;

import android.graphics.drawable.Drawable;

public class POILocationEditableOverlay extends ItemizedOverlay<OverlayItem> {

	private OverlayItem item;
	Drawable marker;

	private int items;

	public POILocationEditableOverlay(int latitude, int longitude,
			Drawable marker) {
		super(null);
		items = 0;
		marker = marker;
		ItemizedOverlay.boundCenterBottom(marker);
		item = new OverlayItem();
		item.setMarker(marker);
		item.setPoint(new GeoPoint(latitude, longitude));
		items = 1;
	}

	public void setPosition(GeoPoint geoPoint) {
		item.setPoint(geoPoint);
		populate();
	}

	@Override
	public int size() {
		return items;
	}

	@Override
	protected OverlayItem createItem(int index) {
		if (index > 0)
			return null;
		return item;
	}

}
