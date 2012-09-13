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

public abstract class LocationFragment extends SherlockFragment implements
		Receiver {

	private final static String TAG = LocationFragment.class.getSimpleName();
	private MyLocationManager mLocationManager;
	private Location mLocation;
	private DetachableResultReceiver mReceiver;

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
				mLocation = (Location) resultData.getParcelable(Extra.LOCATION);
				if (!isAdded())
					return;
				updateLocation();
				break;
			}

			}
		}
	};

	protected abstract void updateLocation();
}
