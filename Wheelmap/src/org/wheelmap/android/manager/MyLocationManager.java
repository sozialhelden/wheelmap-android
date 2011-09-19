package org.wheelmap.android.manager;

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
	private Location mBestLastKnownLocation;

	private MultiResultReceiver mReceiver;

	private boolean requestOnce;
	private boolean doesRequestUpdates;
	private boolean wasLastKnownLocation;

	private static final long TIME_DISTANCE_LIMIT = 1000 * 60 * 5; // 5 Minutes
	private static final long TIME_GPS_UPDATE_INTERVAL = 1000 * 10;
	private static final float TIME_GPS_UPDATE_DISTANCE = 20f;
	private static final long TIME_NETWORK_SUPERSEED_TIME = 1000 * 15;

	private MyLocationManager(Context context) {

		mLocationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		mGPSLocationListener = new MyGPSLocationListener();
		mNetworkLocationListener = new MyNetworkLocationListener();
		mReceiver = new MultiResultReceiver(new Handler());

		mBestLastKnownLocation = calcBestLastKnownLocation();
		if (mBestLastKnownLocation == null) {
			// Berlin, Andreasstra§e 10
			mBestLastKnownLocation = new Location("");
			mBestLastKnownLocation.setLongitude(13.431240);
			mBestLastKnownLocation.setLatitude(52.512523);
		}
		wasLastKnownLocation = true;
		notifyReceiver();

		requestOnce = true;
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
		return mBestLastKnownLocation;
	}

	public void register(ResultReceiver receiver, boolean resendLast) {
		if (mReceiver.getReceiverCount() == 0) {
			requestLocationUpdates();
		}
		if (receiver != null)
			mReceiver.addReceiver(receiver, resendLast);
	}

	public void release(ResultReceiver receiver) {
		mReceiver.removeReceiver(receiver);
		if (mReceiver.getReceiverCount() == 0)
			releaseLocationUpdates();
	}

	private void requestLocationUpdates() {
		if (!doesRequestUpdates) {
			Log.d(TAG, "requestLocationUpdates");
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, TIME_GPS_UPDATE_INTERVAL,
					TIME_GPS_UPDATE_DISTANCE, mGPSLocationListener);
			mLocationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0,
					mNetworkLocationListener);
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
		if ( gpsLocation == null && networkLocation == null )
			return null;
		else if (gpsLocation == null)
			return networkLocation;
		else if ( networkLocation == null)
			return gpsLocation;
		else if (now - gpsLocation.getTime() < TIME_DISTANCE_LIMIT)
			return gpsLocation;
		else if (gpsLocation.getTime() < networkLocation.getTime())
			return gpsLocation;
		else
			return networkLocation;
	}

	private void notifyReceiver() {
		Bundle b = new Bundle();
		b.putParcelable(EXTRA_LOCATION_MANAGER_LOCATION, mBestLastKnownLocation);
		mReceiver.send(WHAT_LOCATION_MANAGER_UPDATE, b );
	}

	private class MyGPSLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG, "MyGPSLocationListener: location received. Accuracy = "
					+ location.getAccuracy());
			requestOnce = false;
			wasLastKnownLocation = false;

			mBestLastKnownLocation = location;
			notifyReceiver();

			if (requestOnce && mReceiver.getReceiverCount() == 0) {
				releaseLocationUpdates();
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
			long now = System.currentTimeMillis();
			if (wasLastKnownLocation) {
				Log.d( TAG, "network location superseeds lastKnownLocation" );
				mBestLastKnownLocation = location;
				wasLastKnownLocation = false;
				notifyReceiver();
			} else if ((mBestLastKnownLocation.getProvider().equals(LocationManager.GPS_PROVIDER))
					&& (now - mBestLastKnownLocation.getTime() > TIME_NETWORK_SUPERSEED_TIME)) {
				Log.d( TAG, "network location superseeds old gps location" );
				mBestLastKnownLocation = location;
				notifyReceiver();
			} else if (mBestLastKnownLocation.getProvider().equals( LocationManager.NETWORK_PROVIDER )) {
				Log.d(TAG,  "network location superseeds old network location"  );
				mBestLastKnownLocation = location;
				notifyReceiver();
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

}
