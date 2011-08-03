package org.wheelmap.android.utils;

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
	
	public static boolean NearPonits(GeoPoint p1, GeoPoint p2) {
		return (((Math.abs(p1.getLatitudeE6() - p2.getLatitudeE6())) < 1000) && 
				(Math.abs(p1.getLongitudeE6() - p2.getLongitudeE6())) < 1000);
	}
		
}
