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
import org.mapsforge.android.maps.overlay.ItemizedOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.wheelmap.android.manager.SupportManager.NodeType;
import org.wheelmap.android.model.Extra;

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
			WheelchairState state, double latitude, double longitude) {

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
			mListener.onTap(item, Extra.ID_UNKNOWN);
			return true;
		}

		return false;
	}
}
