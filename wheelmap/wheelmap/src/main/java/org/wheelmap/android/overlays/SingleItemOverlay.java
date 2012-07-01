package org.wheelmap.android.overlays;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.overlay.ItemizedOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.wheelmap.android.manager.SupportManager.NodeType;

import wheelmap.org.WheelchairState;
import android.graphics.drawable.Drawable;

public class SingleItemOverlay extends ItemizedOverlay<OverlayItem> {
	private OverlayItem item;
	private int items;
	private OnTapListener mListener;

	public SingleItemOverlay(OnTapListener listener) {
		super(null);
		items = 0;
		mListener = listener;
	}

	public void setItem(String title, String snippet, NodeType nodeType,
			WheelchairState state, int latitude, int longitude) {

		Drawable marker = nodeType.stateDrawables.get(state);
		item = new OverlayItem();
		item.setTitle(title);
		item.setSnippet(snippet);
		item.setMarker(marker);
		item.setPoint(new GeoPoint(latitude, longitude));
		items = 1;

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

	@Override
	public boolean onTap(int index) {
		if (mListener != null) {
			mListener.onTap(item);
			return true;
		}

		return false;
	}
}
