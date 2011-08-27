package org.wheelmap.android.service;

import org.wheelmap.android.net.RESTExecutor;
import org.wheelmap.android.utils.CurrentLocation;
import org.wheelmap.android.utils.CurrentLocation.LocationResult;
import org.wheelmap.android.utils.GeocoordinatesMath;
import org.wheelmap.android.utils.ParceableBoundingBox;

import wheelmap.org.BoundingBox;
import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;
import wheelmap.org.WheelchairState;
import android.app.IntentService;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Background {@link Service} that synchronizes data living in
 * {@link POIsProvider}. Reads data from remote source
 */
public class SyncService extends IntentService {
	private static final String TAG = "SyncService";

	public static final String EXTRA_STATUS_RECEIVER = "org.wheelmap.android.STATUS_RECEIVER";
	public static final String EXTRA_STATUS_BOUNDING_BOX = "org.wheelmap.android.EXTRA_STATUS_BOUNCING_BOX";
	public static final String EXTRA_STATUS_DISTANCE_LIMIT = "org.wheelmap.android.EXTRA_STATUS_DISTANCE_LIMIT";

	public static final String PREF_KEY_WHEELCHAIR_STATE = "wheelchairState";

	public static final int STATUS_RUNNING = 0x1;
	public static final int STATUS_ERROR = 0x2;
	public static final int STATUS_FINISHED = 0x3;

	private CurrentLocation mCurrentLocation;

	RESTExecutor mRemoteExecutor;

	public SyncService() {
		super(TAG);
		// current location
		mCurrentLocation = new CurrentLocation();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		final ContentResolver resolver = getContentResolver();
		mRemoteExecutor = new RESTExecutor(resolver);
	}

	// execute request with filters settings
	private void retrieveDatainBoundingBox(BoundingBox bb,
			ResultReceiver receiver, WheelchairState wheelState) {

		try {
			final long startRemote = System.currentTimeMillis();
			// Retrieve all Pages is terribly slow. Anybody knows why?
			mRemoteExecutor.retrieveAllPages(bb, wheelState);
			// mRemoteExecutor.retrieveSinglePage(bb, wheelState);
			Log.d(TAG, "remote sync took "
					+ (System.currentTimeMillis() - startRemote) + "ms");

		} catch (Exception e) {
			Log.e(TAG, "Problem while syncing", e);

			if (receiver != null) {
				// Pass back error to surface listener
				final Bundle responsebundle = new Bundle();
				responsebundle.putString(Intent.EXTRA_TEXT, e.toString());
				receiver.send(STATUS_ERROR, responsebundle);
			}
		}
	}

	final class MyLocationResult implements LocationResult {

		private ResultReceiver mReceiver;
		private WheelchairState mWheelState;
		private float mDistance;

		public MyLocationResult(ResultReceiver receiver,
				WheelchairState wheelState, float distance) {
			mReceiver = receiver;
			mWheelState = wheelState;
			mDistance = distance;
		}

		@Override
		public void gotLocation(final Location location) {
			// calculate bounding box from current location around 20 km
			Log.d(TAG,
					"MyLocationResult:gotLocation: location retrieved - retrieving data.");
			BoundingBox bb = GeocoordinatesMath.calculateBoundingBox(
					new Wgs84GeoCoordinates(location.getLongitude(), location
							.getLatitude()), mDistance);
			retrieveDatainBoundingBox(bb, mReceiver, mWheelState);
			Log.d(TAG, "sync finished");
			if (mReceiver != null)
				mReceiver.send(STATUS_FINISHED, Bundle.EMPTY);
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent(intent=" + intent.toString() + ")");

		final ResultReceiver receiver = intent
				.getParcelableExtra(EXTRA_STATUS_RECEIVER);
		if (receiver != null)
			receiver.send(STATUS_RUNNING, Bundle.EMPTY);

		WheelchairState wheelState = getWheelchairStateFromPreferences();

		final Bundle bundle = intent.getExtras();
		ParceableBoundingBox parcBoundingBox = (ParceableBoundingBox) bundle
				.getSerializable(SyncService.EXTRA_STATUS_BOUNDING_BOX);
		float distance = bundle.getFloat(EXTRA_STATUS_DISTANCE_LIMIT);

		if (parcBoundingBox != null) {
			Log.d(TAG,
					"retrieving with bounding box: "
							+ parcBoundingBox.toString());
			retrieveDatainBoundingBox(parcBoundingBox.toBoundingBox(),
					receiver, wheelState);
			Log.d(TAG, "sync finished");
			if (receiver != null)
				receiver.send(STATUS_FINISHED, Bundle.EMPTY);
		} else if (distance > 0) {
			Log.d(TAG, "retrieving with current location and distance = "
					+ distance);
			MyLocationResult locationResult = new MyLocationResult(receiver,
					wheelState, distance);
			mCurrentLocation.getLocation(this, locationResult);
			// next processing goes via listener in mCurrentLocation
		}
	}

	public WheelchairState getWheelchairStateFromPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		int prefWheelchairState = Integer.valueOf(prefs.getString(
				PREF_KEY_WHEELCHAIR_STATE, "0"));
		return WheelchairState.valueOf(prefWheelchairState);
	}
}
