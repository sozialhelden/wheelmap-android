/*
 * #%L
 * Wheelmap-it - Integration tests
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
package org.wheelmap.android.test;

import org.junit.Assert;
import org.wheelmap.android.net.request.BoundingBox;
import org.wheelmap.android.net.request.BoundingBox.Wgs84GeoCoordinates;
import org.wheelmap.android.utils.GeoCoordinatesMath;

import android.location.Location;
import android.test.AndroidTestCase;
import android.util.Log;

public class GeocoordinatesMathTest extends AndroidTestCase {

    private static final String TAG = GeoCoordinatesMath.class.getSimpleName();

    private static final double DELTA_ENV = 0.0001d;

    public void testBoundingBox() {
        Wgs84GeoCoordinates pointOne = new Wgs84GeoCoordinates(0, 0);
        GeoCoordinatesMath.useAngloDistanceUnit(false);

        double distanceOne = 10d;
        BoundingBox bbOne = GeoCoordinatesMath.calculateBoundingBox(pointOne,
                distanceOne);
        Log.d(TAG, "BoundingBox bbOne = " + bbOne.toString());

        double distanceTwo = 2000d;
        BoundingBox bbTwo = GeoCoordinatesMath.calculateBoundingBox(pointOne,
                distanceTwo);
        Log.d(TAG, "BoundingBox bbTwo = " + bbTwo.toString());
    }

    public void testDistance() {
        Location pointOne = new Location("");
        pointOne.setLongitude(0);
        pointOne.setLatitude(0);
        GeoCoordinatesMath.useAngloDistanceUnit(false);

        double expectedRoughDistOne = 14.164743972d;

        double pointNumOne = 0.09009009d;
        Location pointDestOne = new Location("");
        pointDestOne.setLatitude(-pointNumOne);
        pointDestOne.setLongitude(-pointNumOne);
        double distOne = GeoCoordinatesMath.calculateDistance(pointOne,
                pointDestOne);
        Log.d(TAG, "distanceOne = " + distOne);
        Assert.assertTrue(isExpectedWithDelta(expectedRoughDistOne, distOne));

        Location pointDestTwo = new Location("");
        pointDestTwo.setLongitude(-pointNumOne);
        pointDestTwo.setLatitude(pointNumOne);

        double distTwo = GeoCoordinatesMath.calculateDistance(pointOne,
                pointDestTwo);
        Log.d(TAG, "distanceTwo = " + distTwo);
        Assert.assertTrue(isExpectedWithDelta(expectedRoughDistOne, distTwo));

        Location pointDestThree = new Location("");
        pointDestThree.setLongitude(pointNumOne);
        pointDestThree.setLatitude(-pointNumOne);
        double distThree = GeoCoordinatesMath.calculateDistance(pointOne,
                pointDestThree);
        Log.d(TAG, "distanceThree = " + distThree);
        Assert.assertTrue(isExpectedWithDelta(expectedRoughDistOne, distThree));

        Location pointDestFour = new Location("");
        pointDestFour.setLatitude(pointNumOne);
        pointDestFour.setLongitude(pointNumOne);

        double distFour = GeoCoordinatesMath.calculateDistance(pointOne,
                pointDestFour);
        Log.d(TAG, "distanceFour = " + distFour);
        Assert.assertTrue(isExpectedWithDelta(expectedRoughDistOne, distFour));

        double expectedRoughDistTwo = 2809.19532927;
        double pointNumTwo = 18.018018018d;
        Location pointDestFive = new Location("");
        pointDestFive.setLongitude(-pointNumTwo);
        pointDestFive.setLatitude(-pointNumTwo);
        double distFive = GeoCoordinatesMath.calculateDistance(pointOne,
                pointDestFive);
        Log.d(TAG, "distanceFive = " + distFive);
        Assert.assertTrue(isExpectedWithDelta(expectedRoughDistTwo, distFive));

        Location pointDestSix = new Location("");
        pointDestSix.setLongitude(-pointNumTwo);
        pointDestSix.setLatitude(pointNumTwo);

        double distSix = GeoCoordinatesMath.calculateDistance(pointOne,
                pointDestSix);
        Log.d(TAG, "distanceSix = " + distSix);
        Assert.assertTrue(isExpectedWithDelta(expectedRoughDistTwo, distSix));

        Location pointDestSeven = new Location("");
        pointDestSeven.setLongitude(pointNumTwo);
        pointDestSeven.setLatitude(-pointNumTwo);
        double distSeven = GeoCoordinatesMath.calculateDistance(pointOne,
                pointDestSeven);
        Log.d(TAG, "distanceSeven = " + distSeven);
        Assert.assertTrue(isExpectedWithDelta(expectedRoughDistTwo, distSeven));

        Location pointDestEight = new Location("");
        pointDestEight.setLongitude(pointNumTwo);
        pointDestEight.setLatitude(pointNumTwo);

        double distEight = GeoCoordinatesMath.calculateDistance(pointOne,
                pointDestEight);
        Log.d(TAG, "distanceEight = " + distEight);
        Assert.assertTrue(isExpectedWithDelta(expectedRoughDistTwo, distEight));
    }

    public boolean isExpectedWithDelta(double expected, double actual) {
        boolean result = Math.abs(expected - actual) <= DELTA_ENV;
        if (!result) {
            Log.d(TAG, "expected = " + expected + " actual = " + actual);
        }
        return result;
    }
}
