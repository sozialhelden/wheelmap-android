package org.wheelmap.android.fragment;

import org.wheelmap.android.app.WheelmapApp;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragment;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibraryConstants;

import de.akquinet.android.androlog.Log;

public class LocationFragment extends SherlockFragment {
	private final static String TAG = LocationFragment.class.getSimpleName();

	private static final long ACTIVE_FREQUENCY = 2 * 60 * 1000;
	private static final int ACTIVE_MAXIMUM_AGE = 10 * 60 * 1000;

	private static final long PASSIVE_FREQUENCE = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
	private static final int PASSIVE_MAXIMUM_AGE = (int) AlarmManager.INTERVAL_HOUR;

	private final IntentFilter mIntentFilter = new IntentFilter(
			LocationLibraryConstants
					.getLocationChangedPeriodicBroadcastAction());

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v(TAG, "onReceive: received location update");
			mLocationInfo = (LocationInfo) intent
					.getSerializableExtra(LocationLibraryConstants.LOCATION_BROADCAST_EXTRA_LOCATIONINFO);
			updateLocation();
		}
	};
	private LocationInfo mLocationInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "setLocationInfo: creating location");
		setLocationInfo(new LocationInfo(WheelmapApp.get()));
		Log.d(TAG, "locationInfo = " + getLocationInfo().lastLat + " "
				+ getLocationInfo().lastLong);
		updateLocation();
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().registerReceiver(mReceiver, mIntentFilter);
		LocationLibrary.initialiseLibrary(getActivity(), ACTIVE_FREQUENCY,
				ACTIVE_MAXIMUM_AGE, "org.wheelmap.android.online");
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mReceiver);
		LocationLibrary.initialiseLibrary(getActivity(), PASSIVE_FREQUENCE,
				PASSIVE_MAXIMUM_AGE, "org.wheelmap.android.online");
	}

	protected LocationInfo getLocationInfo() {
		return mLocationInfo;
	}

	protected void setLocationInfo(LocationInfo locationInfo) {
		mLocationInfo = locationInfo;
	}

	protected void updateLocation() {

	}

}
