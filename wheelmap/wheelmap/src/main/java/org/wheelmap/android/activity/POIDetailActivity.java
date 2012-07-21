package org.wheelmap.android.activity;

import org.mapsforge.android.maps.GeoPoint;
import org.wheelmap.android.fragment.POIDetailFragment;
import org.wheelmap.android.fragment.POIDetailFragment.OnPOIDetailListener;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.service.SyncServiceHelper;

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
			long wmID = Extra.ID_UNKNOWN;
			try {
				wmID = Long.parseLong(uri.getLastPathSegment());
			} catch (NumberFormatException e) {
				// TODO: show a dialog with a meaningful message here
				finish();
			}
			Log.d(TAG, "onCreate: wmId = " + wmID);
			mFragment = POIDetailFragment.newInstanceWithWMID(wmID);
		} else {
			long poiID = getIntent().getLongExtra(Extra.POI_ID,
					Extra.ID_UNKNOWN);
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

		// Start the activity whose result we want to retrieve. The
		// result will come back with request code GET_CODE.
		Intent intent = new Intent(POIDetailActivity.this,
				WheelchairStateActivity.class);
		intent.putExtra(Extra.WHEELCHAIR_STATE, wState.getId());
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

					SyncServiceHelper.executeUpdateServer(this);
				}
			}
		}
	}

	@Override
	public void onShowLargeMapAt(GeoPoint point) {
		Intent intent = new Intent(POIDetailActivity.this,
				MainSinglePaneActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.putExtra(Extra.SELECTED_TAB, MainSinglePaneActivity.TAB_MAP);
		intent.putExtra(Extra.CENTER_MAP, true);
		intent.putExtra(Extra.LATITUDE, point.getLatitudeE6());
		intent.putExtra(Extra.LONGITUDE, point.getLongitudeE6());
		startActivity(intent);
	}

	@Override
	public void onEdit(long poiId) {
		Intent intent = new Intent(POIDetailActivity.this,
				POIDetailEditableActivity.class);
		intent.putExtra(Extra.POI_ID, poiId);
		startActivity(intent);
	}

	@Override
	public void onLoadStatus(boolean loading) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(SyncServiceException e) {
		// TODO Auto-generated method stub

	}
}
