package org.wheelmap.android.overlays;

import org.mapsforge.android.maps.overlay.OverlayItem;

public interface OnTapListener {
	public void onTap(OverlayItem item, long poiId, String wmId);
}
