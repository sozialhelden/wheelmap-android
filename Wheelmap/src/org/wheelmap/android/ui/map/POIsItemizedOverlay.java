package org.wheelmap.android.ui.map;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class POIsItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

	public POIsItemizedOverlay(Drawable marker) {
		super(boundCenterBottom(marker));
	}

	public void addOverlay(OverlayItem overlay) 
	{ 
		mOverlays.add(overlay); 
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return(mOverlays.get(i));
	}

	@Override
	protected boolean onTap(int i) {
		//Toast.makeText(RestaurantMap.this,										item.getSnippet(),										Toast.LENGTH_SHORT).show();

		return(true);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

}
