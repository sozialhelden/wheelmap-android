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



import android.graphics.drawable.Drawable;

@Deprecated
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
        if (index > 0) {
            return null;
        }
        return mItem;
    }

}
