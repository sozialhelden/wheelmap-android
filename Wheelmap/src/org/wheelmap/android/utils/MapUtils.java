package org.wheelmap.android.utils;

import org.mapsforge.android.maps.GeoPoint;


public class MapUtils {
		
	public static boolean NearPonits(GeoPoint p1, GeoPoint p2, int lngSpan, int latSpan) {
		int lonDistance = Math.abs(p1.getLongitudeE6() - p2.getLongitudeE6());
		int latDistance = Math.abs(p1.getLatitudeE6() - p2.getLatitudeE6());
		double latPer = (double)latDistance / latSpan;
		double lonPer = (double)lonDistance / lngSpan;
		
		
		// distance between points is less 10% of screen span 	
		return (latPer < 0.1) && (lonPer < 0.1);
	}
		
}
