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

import org.wheelmap.android.net.request.BoundingBox;
import org.wheelmap.android.net.request.BoundingBox.Wgs84GeoCoordinates;

import android.location.Location;

public class GeoMath {

    private enum DistanceUnit {
        MILES, KILOMETRES
    }

    ;

    private static final double LAT_DIST_PER_DEGREE_IN_KM = 111;

    private static final double LAT_DIST_PER_DEGREE_IN_MILES = 69;

    private static final double EARTH_RADIUS_IN_KM = 6370;

    private static final double EARTH_RADIUS_IN_MILES = 3956;

    private static double sEarthRadius;

    private static double sLatDistPerDegree;

    private static DistanceUnit sDistanceUnit;

    public static BoundingBox calculateBoundingBox(Wgs84GeoCoordinates point,
            double dist) {

        double longDifference = dist
                / Math.abs(Math.cos(Math.toRadians(point.latitude))
                * sLatDistPerDegree);
        double westLon = point.longitude - longDifference;
        double eastLon = point.longitude + longDifference;

        double latDifference = dist / sLatDistPerDegree;
        double southLat = point.latitude - latDifference;
        double northLat = point.latitude + latDifference;

        return new BoundingBox(new Wgs84GeoCoordinates(westLon, southLat),
                new Wgs84GeoCoordinates(eastLon, northLat));

    }

    public static float calculateDistance(Location point, Location pointDest) {
        //if (point == null || pointDest == null)
        //    return 0f;
        if (point == null) {
            return Float.MAX_VALUE;
        }

        if (pointDest == null) {
            return 0f;
        }

        double distance = sEarthRadius
                * 2
                * Math.asin(Math.sqrt(Math.pow(
                Math.sin((point.getLatitude() - Math.abs(pointDest
                        .getLatitude())) * Math.PI / 180 / 2), 2)
                + Math.cos(point.getLatitude() * Math.PI / 180)
                * Math.cos(Math.abs(pointDest.getLatitude()) * Math.PI
                / 180)
                * Math.pow(
                Math.sin((point.getLongitude() - pointDest
                        .getLongitude()) * Math.PI / 180 / 2),
                2)));

        return (float) distance;
    }

    public static void useAngloDistanceUnit(boolean mUseAngloDistanceUnit) {
        if (mUseAngloDistanceUnit) {
            sDistanceUnit = DistanceUnit.MILES;
            sEarthRadius = EARTH_RADIUS_IN_MILES;
            sLatDistPerDegree = LAT_DIST_PER_DEGREE_IN_MILES;
        } else {
            sDistanceUnit = DistanceUnit.KILOMETRES;
            sEarthRadius = EARTH_RADIUS_IN_KM;
            sLatDistPerDegree = LAT_DIST_PER_DEGREE_IN_KM;
        }
    }

    public static boolean isUsingAngloDistanceUnit() {
        return sDistanceUnit == DistanceUnit.MILES;
    }

    static {
        useAngloDistanceUnit(false);
    }

}
