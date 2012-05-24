/*
Copyright (C) 2011 Michal Harakal and Michael Kroez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS-IS" BASIS
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
        
*/

package org.wheelmap.android.ui.mapsforge;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.overlay.OverlayItem;

import android.graphics.drawable.Drawable;

import wheelmap.org.WheelchairState;

public class POIMapItem extends OverlayItem {

    /**
     * Stores the wheelchair state
     */
    private final WheelchairState mState;
    
    /**
     * Stores the unique id
     */
    private final int id;
    
	
	/**
     * Constructs a new GeoPoint with the given latitude and longitude, measured in degrees.
     * 
     * @param point
     *            The GeoPoint of POI
     * @param state
     *            wheelchair state.
     */
    public POIMapItem(GeoPoint point, WheelchairState state, int id, Drawable marker) {
    	super();
    	this.mState = state;
        this.id = id;
        setPoint( point );
    	setMarker( marker );        
    }

    /**
     * Returns the WheelchairState
     * 
     * @return the WheelchairState of the POI
     */
    public WheelchairState getWheelchairState() {
        return this.mState;
    }
    
    /**
     * Returns id
     * 
     * @return id of the POI
     */
    public int getId() {
        return this.id;
    }
}
