/*
 * #%L
 * Wheelmap - App
 * %%
 * Copyright (C) 2011 - 2012 Michal Harakal - Michael Kroez - Sozialhelden e.V.
 * %%
 * Wheelmap App based on the Wheelmap Service by Sozialhelden e.V.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wheelmap.android.activity;

import org.mapsforge.android.maps.GeoPoint;
import org.wheelmap.android.fragment.ErrorDialogFragment;
import org.wheelmap.android.fragment.ErrorDialogFragment.OnErrorDialogListener;
import org.wheelmap.android.fragment.POIDetailFragment;
import org.wheelmap.android.fragment.POIDetailFragment.OnPOIDetailListener;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.PrepareDatabaseHelper;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.service.SyncServiceHelper;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.DetachableResultReceiver.Receiver;

import wheelmap.org.WheelchairState;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.view.Window;

import de.akquinet.android.androlog.Log;

public class POIDetailActivity extends MapsforgeMapActivity implements
		OnPOIDetailListener, OnErrorDialogListener, Receiver {
	private final static String TAG = POIDetailActivity.class.getSimpleName();

	// Definition of the one requestCode we use for receiving resuls.
	static final private int SELECT_WHEELCHAIRSTATE = 0;
	POIDetailFragment mFragment;

	private DetachableResultReceiver mReceiver;
	private String wmID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		setSupportProgressBarIndeterminateVisibility(false);

		FragmentManager fm = getSupportFragmentManager();
		mFragment = (POIDetailFragment) fm
				.findFragmentByTag(POIDetailFragment.TAG);
		if (mFragment != null) {
			return;
		}

		executeIntent(getIntent());
	}

	private void executeIntent(Intent intent) {
		if (intent == null)
			return;

		// check if this intent is started via custom scheme link
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			Uri uri = intent.getData();
			if (uri == null) {
				Log.d(TAG, "uri has no data - cant extract wmID");
				showErrorMessage(getString(R.string.error_noid_title),
						getString(R.string.error_noid_message));
				return;
			}

			wmID = uri.getLastPathSegment();
			try {
				Long.parseLong(wmID);
			} catch (NumberFormatException e) {
				Log.e(TAG, " wmID = " + wmID, e);
				finish();
				return;
			}

			showDetailForWmId(wmID);
			return;
		}

		Long poiId = intent.getLongExtra(Extra.POI_ID, Extra.ID_UNKNOWN);
		showDetailFragment(poiId);
		setIntent(null);
	}

	private void showDetailForWmId(String wmId) {
		long poiId = PrepareDatabaseHelper.getRowIdForWMId(
				getContentResolver(), wmId, POIs.TAG_RETRIEVED);
		if (poiId != Extra.ID_UNKNOWN) {
			long copyPoiId = PrepareDatabaseHelper.createCopyIfNotExists(
					getContentResolver(), poiId, false);
			showDetailFragment(copyPoiId);
			return;
		}

		mReceiver = new DetachableResultReceiver(new Handler());
		mReceiver.setReceiver(this);
		SyncServiceHelper.retrieveNode(this, wmId, mReceiver);
	}

	private void showDetailFragment(long id) {
		if (id == Extra.ID_UNKNOWN)
			return;

		FragmentManager fm = getSupportFragmentManager();
		mFragment = POIDetailFragment.newInstance(id);

		fm.beginTransaction()
				.add(android.R.id.content, mFragment, POIDetailFragment.TAG)
				.commit();
	}

	@Override
	public void onEditWheelchairState(WheelchairState wState) {

		// Start the activity whose result we want to retrieve. The
		// result will come back with request code GET_CODE.
		Intent intent = new Intent(this, WheelchairStateActivity.class);
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
		if (requestCode == SELECT_WHEELCHAIRSTATE) {
			if (resultCode == RESULT_OK) {
				// newly selected wheelchair state as action data
				if (data != null) {
					long poiID = mFragment.getPoiId();

					WheelchairState state = WheelchairState
							.valueOf(data.getIntExtra(Extra.WHEELCHAIR_STATE,
									Extra.UNKNOWN));
					updateDatabase(poiID, state);
					SyncServiceHelper.executeUpdateServer(this, null);
				}
			}
		}
	}

	private void updateDatabase(long id, WheelchairState state) {
		if (id == Extra.ID_UNKNOWN || state == null)
			return;

		ContentValues values = new ContentValues();
		values.put(POIs.WHEELCHAIR, state.getId());
		values.put(POIs.DIRTY, POIs.DIRTY_STATE);

		PrepareDatabaseHelper.editCopy(getContentResolver(), id, values);
	}

	@Override
	public void onShowLargeMapAt(GeoPoint point) {
		Intent intent = new Intent(this, MainSinglePaneActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.putExtra(Extra.SELECTED_TAB, MainSinglePaneActivity.TAB_MAP);
		intent.putExtra(Extra.CENTER_MAP, true);
		intent.putExtra(Extra.LATITUDE, point.getLatitude());
		intent.putExtra(Extra.LONGITUDE, point.getLongitude());
		intent.putExtra(Extra.REQUEST, true);
		startActivity(intent);
		finish();
	}

	@Override
	public void onEdit(long poiId) {
		Intent intent = new Intent(this, POIDetailEditableActivity.class);
		intent.putExtra(Extra.POI_ID, poiId);
		startActivity(intent);
	}

	public void showErrorMessage(String title, String message) {
		FragmentManager fm = getSupportFragmentManager();
		ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance(
				title, message, Extra.UNKNOWN);
		if (errorDialog == null)
			return;

		errorDialog.show(fm, ErrorDialogFragment.TAG);
	}

	public void showErrorDialog(SyncServiceException e) {
		FragmentManager fm = getSupportFragmentManager();
		ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance(e,
				Extra.UNKNOWN);
		if (errorDialog == null)
			return;

		errorDialog.show(fm, ErrorDialogFragment.TAG);
	}

	/** {@inheritDoc} */
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Log.d(TAG, "onReceiveResult in list resultCode = " + resultCode);
		switch (resultCode) {
		case SyncService.STATUS_RUNNING: {
			setSupportProgressBarIndeterminateVisibility(true);
			break;
		}
		case SyncService.STATUS_FINISHED: {
			long id = PrepareDatabaseHelper.getRowIdForWMId(
					getContentResolver(), wmID, POIs.TAG_COPY);
			setSupportProgressBarIndeterminateVisibility(false);
			showDetailFragment(id);
			break;
		}
		case SyncService.STATUS_ERROR: {
			setSupportProgressBarIndeterminateVisibility(false);
			final SyncServiceException e = resultData
					.getParcelable(Extra.EXCEPTION);
			showErrorDialog(e);
			break;
		}
		default: {
			// noop
		}
		}
	}

	@Override
	public void onErrorDialogClose(int id) {
		finish();
	}
}
