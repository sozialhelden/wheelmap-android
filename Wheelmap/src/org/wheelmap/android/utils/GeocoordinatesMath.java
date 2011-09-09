package org.wheelmap.android.utils;

import wheelmap.org.BoundingBox;
import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;

public class GeocoordinatesMath {

	public enum DistanceUnit {
		MILES, KILOMETRES
	};

	private static final double LAT_DIST_PER_DEGREE_IN_KM = 111;
	private static final double LAT_DIST_PER_DEGREE_IN_MILES = 69;
	private static final double EARTH_RADIUS_IN_KM = 6370;
	private static final double EARTH_RADIUS_IN_MILES = 3956;
	
	public static DistanceUnit DISTANCE_UNIT = DistanceUnit.KILOMETRES;

	public static BoundingBox calculateBoundingBox(Wgs84GeoCoordinates point,
			double dist) {

		double LAT_DIST_PER_DEGREE = DISTANCE_UNIT == DistanceUnit.KILOMETRES ? LAT_DIST_PER_DEGREE_IN_KM
				: LAT_DIST_PER_DEGREE_IN_MILES;

		double longDifference = dist
				/ Math.abs(Math.cos(Math.toRadians(point.latitude))
						* LAT_DIST_PER_DEGREE);
		double westLon = point.longitude - longDifference;
		double eastLon = point.longitude + longDifference;

		double latDifference = dist / LAT_DIST_PER_DEGREE;
		double southLat = point.latitude - latDifference;
		double northLat = point.latitude + latDifference;

		return new BoundingBox(new Wgs84GeoCoordinates(westLon, southLat),
				new Wgs84GeoCoordinates(eastLon, northLat));

	}

	public static double calculateDistance(Wgs84GeoCoordinates point,
			Wgs84GeoCoordinates pointDest) {

		double EARTH_RADIUS = DISTANCE_UNIT == DistanceUnit.KILOMETRES ? EARTH_RADIUS_IN_KM
				: EARTH_RADIUS_IN_MILES;

		double distance = EARTH_RADIUS
				* 2
				* Math.asin(Math.sqrt(Math.pow(Math.sin((point.latitude - Math
						.abs(pointDest.latitude)) * Math.PI / 180 / 2), 2)
						+ Math.cos(point.latitude * Math.PI / 180)
						* Math.cos(Math.abs(pointDest.latitude) * Math.PI / 180)
						* Math.pow(Math
								.sin((point.longitude - pointDest.longitude)
										* Math.PI / 180 / 2), 2)));

		return distance;
	}
}
