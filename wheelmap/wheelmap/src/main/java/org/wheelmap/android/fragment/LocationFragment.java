package org.wheelmap.android.fragment;

import org.wheelmap.android.manager.MyLocationManager;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Extra.What;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.DetachableResultReceiver.Receiver;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;

import com.actionbarsherlock.app.SherlockFragment;

import de.akquinet.android.androlog.Log;
import org.wheelmap.android.utils.GeocoordinatesMath;

public abstract class LocationFragment extends SherlockFragment {

	private final static String TAG = LocationFragment.class.getSimpleName();
	private MyLocationManager mLocationManager;
	private Location mLocation;
	private Location mLastLocation;
	private DetachableResultReceiver mReceiver;

	private final static float DISTANCE_TO_RELOAD = 2l;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		mReceiver = new DetachableResultReceiver(new Handler());
		mReceiver.setReceiver(mReceiverInterface);
		mLocationManager = MyLocationManager.get();
		setLocation(MyLocationManager.getLastLocation());

	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume: registerReceiver");
		mLocationManager.register(mReceiver, true);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
		mLocationManager.release(mReceiver);

	}

	protected Location getLocation() {
		return mLocation;
	}

	protected void setLocation(Location locationInfo) {
		mLocation = locationInfo;
	}

	private Receiver mReceiverInterface = new Receiver() {
		public void onReceiveResult(int resultCode, Bundle resultData) {
			Log.d(TAG, "onReceiveResult resultCode = " + resultCode);
			switch (resultCode) {
			case What.LOCATION_MANAGER_UPDATE: {
				mLastLocation = mLocation;
				mLocation = (Location) resultData.getParcelable(Extra.LOCATION);
				if (!isAdded())
					return;
				updateLocation(mLocation);
				break;
			}

			}
		}
	};

	protected abstract void updateLocation(Location location);

	protected boolean isNewDistanceFar() {
		float distance = GeocoordinatesMath.calculateDistance(mLastLocation, mLocation );

		if ( distance > DISTANCE_TO_RELOAD)
			return true;
		else
			return false;
	}
}
