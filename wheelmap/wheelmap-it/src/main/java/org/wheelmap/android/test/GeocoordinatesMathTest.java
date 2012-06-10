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


import junit.framework.Assert;

import org.wheelmap.android.utils.GeocoordinatesMath;

import wheelmap.org.BoundingBox;
import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;
import android.test.AndroidTestCase;
import android.util.Log;

public class GeocoordinatesMathTest extends AndroidTestCase {

	private static final String TAG = "wheelmaptest";
	
	private static final double DELTA_ENV = 0.00000001d;

	public void testBoundingBox() {
		Wgs84GeoCoordinates pointOne = new Wgs84GeoCoordinates( 0, 0 );
		
		GeocoordinatesMath.DISTANCE_UNIT = GeocoordinatesMath.DistanceUnit.KILOMETRES;
		
		double distanceOne = 10d;
		BoundingBox bbOne = GeocoordinatesMath.calculateBoundingBox( pointOne, distanceOne );
		Log.d( TAG, "BoundingBox bbOne = " + bbOne.toString());		
		
		double distanceTwo = 2000d;
		BoundingBox bbTwo = GeocoordinatesMath.calculateBoundingBox( pointOne, distanceTwo );
		Log.d( TAG, "BoundingBox bbTwo = " + bbTwo.toString());
	}
	
	public void testDistance() {
		Wgs84GeoCoordinates pointOne = new Wgs84GeoCoordinates( 0, 0);
		GeocoordinatesMath.DISTANCE_UNIT = GeocoordinatesMath.DistanceUnit.KILOMETRES;
		
		double expectedRoughDistOne = 14.164743972d;
		
		double pointNumOne = 0.09009009d;
		Wgs84GeoCoordinates pointDestOne = new Wgs84GeoCoordinates( -pointNumOne, -pointNumOne );
		double distOne = GeocoordinatesMath.calculateDistance( pointOne, pointDestOne);
		Log.d( TAG, "distanceOne = " + distOne );
		Assert.assertTrue( isExpectedWithDelta( expectedRoughDistOne, distOne ));
		
		Wgs84GeoCoordinates pointDestTwo = new Wgs84GeoCoordinates( -pointNumOne, pointNumOne );
		double distTwo = GeocoordinatesMath.calculateDistance( pointOne, pointDestTwo );
		Log.d( TAG, "distanceTwo = " + distTwo );
		Assert.assertTrue( isExpectedWithDelta( expectedRoughDistOne, distTwo ));

		
		Wgs84GeoCoordinates pointDestThree = new Wgs84GeoCoordinates( pointNumOne, -pointNumOne );
		double distThree = GeocoordinatesMath.calculateDistance( pointOne, pointDestThree );
		Log.d( TAG, "distanceThree = " + distThree );
		Assert.assertTrue( isExpectedWithDelta( expectedRoughDistOne, distThree ));

		
		Wgs84GeoCoordinates pointDestFour = new Wgs84GeoCoordinates( pointNumOne, pointNumOne );
		double distFour = GeocoordinatesMath.calculateDistance( pointOne, pointDestFour );
		Log.d( TAG, "distanceFour = " + distFour );
		Assert.assertTrue( isExpectedWithDelta( expectedRoughDistOne, distFour ));

		
		double expectedRoughDistTwo = 2809.19532927;
		double pointNumTwo = 18.018018018d;
		Wgs84GeoCoordinates pointDestFive = new Wgs84GeoCoordinates( -pointNumTwo, -pointNumTwo );
		double distFive = GeocoordinatesMath.calculateDistance( pointOne, pointDestFive);
		Log.d( TAG, "distanceFive = " + distFive );
		Assert.assertTrue( isExpectedWithDelta( expectedRoughDistTwo, distFive ));
		
		Wgs84GeoCoordinates pointDestSix = new Wgs84GeoCoordinates( -pointNumTwo, pointNumTwo );
		double distSix = GeocoordinatesMath.calculateDistance( pointOne, pointDestSix );
		Log.d( TAG, "distanceSix = " + distSix );
		Assert.assertTrue( isExpectedWithDelta( expectedRoughDistTwo, distSix ));
		
		Wgs84GeoCoordinates pointDestSeven = new Wgs84GeoCoordinates( pointNumTwo, -pointNumTwo );
		double distSeven = GeocoordinatesMath.calculateDistance( pointOne, pointDestSeven );
		Log.d( TAG, "distanceSeven = " + distSeven );
		Assert.assertTrue( isExpectedWithDelta( expectedRoughDistTwo, distSeven ));

		Wgs84GeoCoordinates pointDestEight = new Wgs84GeoCoordinates( pointNumTwo, pointNumTwo );
		double distEight = GeocoordinatesMath.calculateDistance( pointOne, pointDestEight );
		Log.d( TAG, "distanceEight = " + distEight );
		Assert.assertTrue( isExpectedWithDelta( expectedRoughDistTwo, distEight ));
	}
	
	public boolean isExpectedWithDelta( double expected, double actual ) {
		return Math.abs( expected - actual ) <= DELTA_ENV;
	}
	
}
