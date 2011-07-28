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
