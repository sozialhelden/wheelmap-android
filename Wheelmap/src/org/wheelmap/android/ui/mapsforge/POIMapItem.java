package org.wheelmap.android.ui.mapsforge;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.OverlayItem;

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
