package org.wheelmap.android.activity;

import org.wheelmap.android.fragment.POIDetailFragment;
import org.wheelmap.android.fragment.POIDetailFragment.OnPOIDetailListener;
import org.wheelmap.android.fragment.WheelchairStateFragment;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.ui.mapsforge.POIsMapsforgeActivity;

import roboguice.inject.ContentView;
import wheelmap.org.WheelchairState;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import de.akquinet.android.androlog.Log;

@ContentView(R.layout.activity_fragment_singleframe)
public class POIDetailActivity extends MapsforgeMapActivity implements
		OnPOIDetailListener {
	private final static String TAG = POIDetailActivity.class.getSimpleName();

	// Definition of the one requestCode we use for receiving resuls.
	static final private int SELECT_WHEELCHAIRSTATE = 0;
	POIDetailFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		getSupportActionBar().setDisplayShowTitleEnabled(false);

		FragmentManager fm = getSupportFragmentManager();
		mFragment = (POIDetailFragment) fm
				.findFragmentByTag(POIDetailFragment.TAG);
		if (mFragment != null) {
			return;
		}

		Intent intent = getIntent();
		// check if this intent is started via custom scheme link
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			Uri uri = intent.getData();
			long wmID = -1l;
			try {
				wmID = Long.parseLong(uri.getLastPathSegment());
			} catch (NumberFormatException e) {
				// TODO: show a dialog with a meaningful message here
				finish();
			}
			Log.d(TAG, "onCreate: wmId = " + wmID);
			mFragment = POIDetailFragment.newInstanceWithWMID(wmID);
		} else {
			long poiID = getIntent().getLongExtra(Wheelmap.POIs.EXTRAS_POI_ID,
					-1);
			Log.d(TAG, "onCreate: poiID = " + poiID);
			mFragment = POIDetailFragment.newInstanceWithPOIID(poiID);
		}

		fm.beginTransaction().add(R.id.frame, mFragment, POIDetailFragment.TAG)
				.commit();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onEditWheelchairState(WheelchairState wState) {
		// Sometimes, the poiId doesnt exists in the db, as the db got loaded
		// again
		// Actually it would be better to use the wmId in this activity, instead
		// of the poiId, as the wmId is persistent during reload
		// This is only a quick fix to take care of a npe here,
		// as mWheelchairState is null in this case.
		if (wState == null)
			return;

		// Start the activity whose result we want to retrieve. The
		// result will come back with request code GET_CODE.
		Intent intent = new Intent(POIDetailActivity.this,
				WheelchairStateActivity.class);
		intent.putExtra(WheelchairStateFragment.EXTRA_WHEELCHAIR_STATE,
				wState.getId());
		startActivityForResult(intent, SELECT_WHEELCHAIRSTATE);

	}

	/**
	 * This method is called when the sending activity has finished, with the
	 * result it supplied.
	 * 
	 * @param requestCode
	 *            The original request code as given to startActivity().
	 * @param resultCode
	 *            From sending activity as per setResult().
	 * @param data
	 *            From sending activity as per setResult().
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// You can use the requestCode to select between multiple child
		// activities you may have started. Here there is only one thing
		// we launch.
		if (requestCode == SELECT_WHEELCHAIRSTATE) {
			// This is a standard resultCode that is sent back if the
			// activity doesn't supply an explicit result. It will also
			// be returned if the activity failed to launch.
			if (resultCode == RESULT_OK) {
				// newly selected wheelchair state as action data
				if (data != null) {
					long poiID = mFragment.getPoiId();

					WheelchairState newState = WheelchairState.valueOf(Integer
							.parseInt(data.getAction()));
					Uri poiUri = Uri.withAppendedPath(
							Wheelmap.POIs.CONTENT_URI_POI_ID,
							String.valueOf(poiID));
					ContentValues values = new ContentValues();
					values.put(Wheelmap.POIs.WHEELCHAIR, newState.getId());
					values.put(Wheelmap.POIs.UPDATE_TAG,
							Wheelmap.UPDATE_WHEELCHAIR_STATE);
					this.getContentResolver().update(poiUri, values, "", null);

					final Intent intent = new Intent(Intent.ACTION_SYNC, null,
							POIDetailActivity.this, SyncService.class);
					intent.putExtra(SyncService.EXTRA_WHAT,
							SyncService.WHAT_UPDATE_SERVER);
					startService(intent);
				}
			}
		}
	}

	@Override
	public void onShowLargeMapAt(int lat, int lon) {
		Intent i = new Intent(POIDetailActivity.this,
				POIsMapsforgeActivity.class);
		i.putExtra(POIsMapsforgeActivity.EXTRA_CENTER_AT_LAT, lat);
		i.putExtra(POIsMapsforgeActivity.EXTRA_CENTER_AT_LON, lon);
		startActivity(i);
	}

	@Override
	public void onEdit(long poiId) {
		Intent i = new Intent(POIDetailActivity.this,
				POIDetailEditableActivity.class);
		i.putExtra(Wheelmap.POIs.EXTRAS_POI_ID, poiId);
		startActivity(i);
	}
}
