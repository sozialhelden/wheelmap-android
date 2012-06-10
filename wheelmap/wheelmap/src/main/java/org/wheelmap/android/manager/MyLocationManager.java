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
package org.wheelmap.android.manager;

import java.util.List;

import org.wheelmap.android.utils.MultiResultReceiver;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

public class MyLocationManager {
	private final static String TAG = "mylocationmanager";

	public final static int WHAT_LOCATION_MANAGER_UPDATE = 0x11;
	public final static String EXTRA_LOCATION_MANAGER_LOCATION = "org.wheelmap.android.manager.mlm.LOCATION";

	private static MyLocationManager INSTANCE;
	private LocationManager mLocationManager;

	private MyGPSLocationListener mGPSLocationListener;
	private MyNetworkLocationListener mNetworkLocationListener;
	private Location mCurrentBestLocation;
	private List<String> mProviders;

	private MultiResultReceiver mReceiver;

	private boolean doesRequestUpdates;
	private boolean gpsExists;
	private boolean networkExists;
	private boolean wasBestLastKnownLocation;

	private static final long TIME_DISTANCE_LIMIT = 1000 * 60 * 5; // 5 Minutes
	private static final long TIME_GPS_UPDATE_INTERVAL = 1000 * 10;
	private static final float TIME_GPS_UPDATE_DISTANCE = 20f;

	private MyLocationManager(Context context) {

		mLocationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		mProviders = mLocationManager.getAllProviders();
		gpsExists = findProvider(LocationManager.GPS_PROVIDER);
		networkExists = findProvider(LocationManager.NETWORK_PROVIDER);

		mGPSLocationListener = new MyGPSLocationListener();
		mNetworkLocationListener = new MyNetworkLocationListener();
		mReceiver = new MultiResultReceiver(new Handler());

		mCurrentBestLocation = calcBestLastKnownLocation();
		if (mCurrentBestLocation == null) {
			// Berlin, Andreasstra�e 10
			mCurrentBestLocation = new Location(
					LocationManager.NETWORK_PROVIDER);
			mCurrentBestLocation.setLongitude(13.431240);
			mCurrentBestLocation.setLatitude(52.512523);
			mCurrentBestLocation.setAccuracy(1000 * 100);
		}
		wasBestLastKnownLocation = true;
		
		updateLocation( mCurrentBestLocation );
		requestLocationUpdates();

	}

	public static MyLocationManager initOnce(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new MyLocationManager(context);
		}

		return INSTANCE;
	}

	public void clear() {
		releaseLocationUpdates();
		mReceiver.clearReceiver();
		INSTANCE = null;
	}

	public static MyLocationManager get(ResultReceiver receiver,
			boolean resendLast) {

		INSTANCE.register(receiver, resendLast);
		return INSTANCE;
	}

	public Location getLastLocation() {
		return mCurrentBestLocation;
	}

	public void register(ResultReceiver receiver, boolean resendLast) {
		if (mReceiver.getReceiverCount() == 0) {
			requestLocationUpdates();
		}
		if (receiver != null) {
			mReceiver.addReceiver(receiver, resendLast);
		}
	}

	public void release(ResultReceiver receiver) {
		mReceiver.removeReceiver(receiver);
		if (mReceiver.getReceiverCount() == 0)
			releaseLocationUpdates();
	}

	private boolean findProvider(String find) {
		for (String provider : mProviders) {
			if (provider.equals(find))
				return true;
		}

		return false;
	}

	private void requestLocationUpdates() {

		if (!doesRequestUpdates) {
			Log.d(TAG, "requestLocationUpdates");

			if (gpsExists) {
				mLocationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, TIME_GPS_UPDATE_INTERVAL,
						TIME_GPS_UPDATE_DISTANCE, mGPSLocationListener);
			}
			
			if (networkExists) {
				mLocationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 0, 0,
						mNetworkLocationListener);
			}
			doesRequestUpdates = true;
		}
	}

	private void releaseLocationUpdates() {
		Log.d(TAG, "releaseLocationUpdates");
		mLocationManager.removeUpdates(mGPSLocationListener);
		mLocationManager.removeUpdates(mNetworkLocationListener);
		doesRequestUpdates = false;
	}

	private Location calcBestLastKnownLocation() {
		Location networkLocation = mLocationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		Location gpsLocation = mLocationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		long now = System.currentTimeMillis();
		if (gpsLocation == null && networkLocation == null)
			return null;
		else if (gpsLocation == null)
			return networkLocation;
		else if (networkLocation == null)
			return gpsLocation;
		else if (now - gpsLocation.getTime() < TIME_DISTANCE_LIMIT)
			return gpsLocation;
		else if (gpsLocation.getTime() < networkLocation.getTime())
			return gpsLocation;
		else
			return networkLocation;
	}

	private class MyGPSLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG, "MyGPSLocationListener: location received. Accuracy = "
					+ location.getAccuracy());

			if ( wasBestLastKnownLocation || isBetterLocation( location, mCurrentBestLocation )) {
				Log.d( TAG, "gps location superseeds mCurrentBestLocation location" );
				updateLocation( location );
				wasBestLastKnownLocation = false;
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	private class MyNetworkLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG,
					"MyNetworkLocationListener: location received. Accuracy = "
							+ location.getAccuracy());
			if ( wasBestLastKnownLocation || isBetterLocation( location, mCurrentBestLocation )) {
				Log.d( TAG, "network location superseeds mCurrentBestLocation location" );
				updateLocation( location );
				wasBestLastKnownLocation = false;
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}
	
	public interface LocationUpdate {
		public void onNewLocation(Location location);
	}

	public void updateLocation(Location location) {
		mCurrentBestLocation = location;
		Bundle b = new Bundle();
		b.putParcelable(EXTRA_LOCATION_MANAGER_LOCATION, location);
		mReceiver.send(WHAT_LOCATION_MANAGER_UPDATE, b);
	}
	
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    // Log.d( TAG, "location.getTime = " + location.getTime() + " currentBestLocation.getTime() = " + currentBestLocation.getTime());
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;
	    
	    // Log.d( TAG, "isSignificantlyNewer = " + isSignificantlyNewer + " isSignificantlyOlder = " + isSignificantlyOlder + " timeDelta = " + timeDelta );

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    }
	    /*
	    else if (isSignificantlyOlder) {
	        return false;
	    }
	     */

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}

}
