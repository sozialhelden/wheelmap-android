package org.wheelmap.android.utils;

import android.util.Log;

import com.google.android.maps.GeoPoint;

public class MapUtils {
	/*public static void sortListOfMapObject(List<? extends MapObject> list, final double lat, final double lon){
		Collections.sort(list, new Comparator<MapObject>() {
			@Override
			public int compare(MapObject o1, MapObject o2) {
				return Double.compare(MapUtils.getDistance(o1.getLocation(), lat, lon), MapUtils.getDistance(o2.getLocation(),
						lat, lon));
			}
		});
	}
	*/
		
	public static boolean NearPonits(GeoPoint p1, GeoPoint p2, int lngSpan, int latSpan) {
		int lonDistance = Math.abs(p1.getLongitudeE6() - p2.getLongitudeE6());
		int latDistance = Math.abs(p1.getLatitudeE6() - p2.getLatitudeE6());
		double latPer = (double)latDistance / latSpan;
		double lonPer = (double)lonDistance / lngSpan;
		
		
		// distance between points is less 10% of screen span 	
		return (latPer < 0.1) && (lonPer < 0.1);
	}
		
}
