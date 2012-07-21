package org.wheelmap.android.activity;

import org.mapsforge.android.maps.GeoPoint;
import org.wheelmap.android.fragment.ErrorDialogFragment;
import org.wheelmap.android.fragment.POIDetailFragment;
import org.wheelmap.android.fragment.POIDetailFragment.OnPOIDetailListener;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.service.SyncServiceHelper;

import wheelmap.org.WheelchairState;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.view.Window;

import de.akquinet.android.androlog.Log;

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
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setSupportProgressBarIndeterminateVisibility(false);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		FragmentManager fm = getSupportFragmentManager();
		mFragment = (POIDetailFragment) fm
				.findFragmentByTag(POIDetailFragment.TAG);
		if (mFragment != null) {
			return;
		}

		Intent intent = getIntent();
		Long id;
		String wmId;
		// check if this intent is started via custom scheme link
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			Uri uri = intent.getData();
			String uriArg = uri.getLastPathSegment();
			if (uriArg.length() == 0) {
				Log.e(TAG, "wmID is empty - cant start fragment");
				// do something meaningful here
				finish();
			}

			wmId = uriArg;
			id = Extra.ID_UNKNOWN;
		} else {
			Bundle extras = intent.getExtras();
			if (extras == null)
				return;

			id = extras.getLong(Extra.POI_ID, Extra.ID_UNKNOWN);
			wmId = extras.getString(Extra.WM_ID);
		}

		mFragment = POIDetailFragment.newInstance(id, wmId);

		fm.beginTransaction()
				.add(android.R.id.content, mFragment, POIDetailFragment.TAG)
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

					WheelchairState state = WheelchairState
							.valueOf(data.getIntExtra(Extra.WHEELCHAIR_STATE,
									Extra.UNKNOWN));
					writeNewStateToDB(poiID, state);
					SyncServiceHelper.executeUpdateServer(this);
				}
			}
		}
	}

	private void writeNewStateToDB(long id, WheelchairState state) {
		if (id == Extra.ID_UNKNOWN || state == null)
			return;

		Uri uri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI_POI_ID,
				String.valueOf(id));
		ContentValues values = new ContentValues();
		values.put(Wheelmap.POIs.WHEELCHAIR, state.getId());
		values.put(Wheelmap.POIs.UPDATE_TAG, Wheelmap.UPDATE_WHEELCHAIR_STATE);
		getContentResolver().update(uri, values, null, null);
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
		setSupportProgressBarIndeterminateVisibility(loading);
	}

	@Override
	public void onError(SyncServiceException e) {
		FragmentManager fm = getSupportFragmentManager();
		ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance(e);
		if (errorDialog == null)
			return;

		errorDialog.show(fm, ErrorDialogFragment.TAG);
	}
}
