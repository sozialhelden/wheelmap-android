package org.wheelmap.android.service;

import org.wheelmap.android.net.RESTExecutor;
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
	public static final String EXTRA_STATUS_BOUNDING_BOX = "org.wheelmap.android.EXTRA_STATUS_BOUNDING_BOX";
	public static final String EXTRA_STATUS_LOCATION = "org.wheelmap.android.EXTRA_STATUS_LOCATION";
	public static final String EXTRA_STATUS_DISTANCE_LIMIT = "org.wheelmap.android.EXTRA_STATUS_DISTANCE_LIMIT";

	public static final String PREF_KEY_WHEELCHAIR_STATE = "wheelchairState";

	public static final int STATUS_RUNNING = 0x1;
	public static final int STATUS_ERROR = 0x2;
	public static final int STATUS_FINISHED = 0x3;

	RESTExecutor mRemoteExecutor;

	public SyncService() {
		super(TAG);
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
			// mRemoteExecutor.retrieveAllPages(bb, wheelState);
			mRemoteExecutor.retrieveSinglePage(bb, wheelState);
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
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent(intent=" + intent.toString() + ")");

		final ResultReceiver receiver = intent
				.getParcelableExtra(EXTRA_STATUS_RECEIVER);
		if (receiver != null)
			receiver.send(STATUS_RUNNING, Bundle.EMPTY);

		WheelchairState wheelState = getWheelchairStateFromPreferences();

		final Bundle bundle = intent.getExtras();

		if (bundle.containsKey(SyncService.EXTRA_STATUS_BOUNDING_BOX)) {

			ParceableBoundingBox parcBoundingBox = (ParceableBoundingBox) bundle
					.getSerializable(SyncService.EXTRA_STATUS_BOUNDING_BOX);

			Log.d(TAG,
					"retrieving with bounding box: "
							+ parcBoundingBox.toString());
			retrieveDatainBoundingBox(parcBoundingBox.toBoundingBox(),
					receiver, wheelState);
		} else if (bundle.containsKey(EXTRA_STATUS_LOCATION)) {
			float distance = bundle.getFloat(EXTRA_STATUS_DISTANCE_LIMIT);
			Location location = (Location) bundle
					.getParcelable(EXTRA_STATUS_LOCATION);
			Log.d(TAG,
					"retrieving with current location = ("
							+ location.getLongitude() + ","
							+ location.getLatitude() + ") and distance = "
							+ distance);
			BoundingBox bb = GeocoordinatesMath.calculateBoundingBox(
					new Wgs84GeoCoordinates(location.getLongitude(), location
							.getLatitude()), distance);
			retrieveDatainBoundingBox(bb, receiver, wheelState);
			
		}
		Log.d(TAG, "sync finished");
		if (receiver != null)
			receiver.send(STATUS_FINISHED, Bundle.EMPTY);
	}

	public WheelchairState getWheelchairStateFromPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String prefWheelchairState = prefs.getString(PREF_KEY_WHEELCHAIR_STATE,
				WheelchairState.DEFAULT.toString());
		WheelchairState ws = WheelchairState.valueOf(Integer
				.valueOf(prefWheelchairState));
		return ws;
	}
}
