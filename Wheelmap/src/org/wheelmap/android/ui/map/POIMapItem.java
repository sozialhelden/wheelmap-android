package org.wheelmap.android.ui.map;

import wheelmap.org.WheelchairState;

import com.google.android.maps.GeoPoint;

public class POIMapItem {
	
	 /**
     * Stores the geo location of POI
     */
    private final GeoPoint mPoint;

    /**
     * Stores the wheelchair state
     */
    private final WheelchairState mState;
    
    /**
     * Stores the wheelchair state
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
    public POIMapItem(GeoPoint point, WheelchairState state, int id) {
        this.mPoint = point;
        this.mState = state;
        this.id = id;
    }
    
    /**
     * Returns the GeoPoint 
     * 
     * @return the geopoint
     */
    public GeoPoint getPoint() {
        return this.mPoint;
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
