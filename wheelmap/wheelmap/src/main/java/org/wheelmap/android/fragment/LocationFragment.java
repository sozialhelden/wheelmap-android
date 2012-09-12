package org.wheelmap.android.fragment;

import org.wheelmap.android.app.WheelmapApp;

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

public abstract class LocationFragment extends SherlockFragment {
	private final static String TAG = LocationFragment.class.getSimpleName();

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
		LocationLibrary.forceLocationUpdate(getActivity());
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mReceiver);
	}

	protected LocationInfo getLocationInfo() {
		return mLocationInfo;
	}

	protected void setLocationInfo(LocationInfo locationInfo) {
		mLocationInfo = locationInfo;
	}

	abstract protected void updateLocation();

}
