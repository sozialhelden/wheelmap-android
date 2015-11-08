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
import org.wheelmap.android.model.WheelchairFilterState;
import org.wheelmap.android.model.Wheelmap.POIs;

import android.content.ContentValues;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class SingleItemOverlay extends ItemizedOverlay<OverlayItem> {

    private OverlayItem item = new OverlayItem();

    private ContentValues itemValues;

    private int items;

    private OnTapListener mListener;

    public SingleItemOverlay(OnTapListener listener) {
        super(null);
        items = 0;
        mListener = listener;
    }

    public void setItem(ContentValues values, NodeType nodeType,
            WheelchairFilterState state) {
        itemValues = values;
        //Drawable marker = nodeType.stateDrawables.get(state);
        Drawable marker = nodeType.getStateDrawable(state);

        item.setTitle(values.getAsString(POIs.NAME));
        item.setSnippet(values.getAsString(POIs.DESCRIPTION));
        item.setMarker(marker);
        item.setPoint(new GeoPoint(values.getAsDouble(POIs.LATITUDE), values
                .getAsDouble(POIs.LONGITUDE)));
        Log.d("singleitemoverlay", "item point = " + item.getPoint().toString());
        items = 1;
        populate();
    }

    @Override
    public int size() {
        return items;
    }

    @Override
    protected OverlayItem createItem(int index) {
        if (index > 0) {
            return null;
        }
        return item;
    }

    @Override
    public boolean onTap(int index) {
        if (mListener != null) {
            mListener.onTap(item, itemValues);
            return true;
        }

        return false;
    }
}
