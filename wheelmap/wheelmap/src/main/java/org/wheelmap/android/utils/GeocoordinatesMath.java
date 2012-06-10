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
