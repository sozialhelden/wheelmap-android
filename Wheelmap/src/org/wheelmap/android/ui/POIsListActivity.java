/*
Copyright (C) 2011 Michal Harakal and Michael Kroez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS-IS" BASIS
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
        
*/

package org.wheelmap.android.ui;

import org.wheelmap.android.R;
import org.wheelmap.android.manager.MyLocationManager;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.POIsCursorWrapper;
import org.wheelmap.android.model.POIsListCursorAdapter;
import org.wheelmap.android.model.QueriesBuilderHelper;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.ui.mapsforge.POIsMapsforgeActivity;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.GeocoordinatesMath;

import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.markupartist.android.widget.PullToRefreshListView;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;

public class POIsListActivity extends ListActivity implements
		DetachableResultReceiver.Receiver, OnRefreshListener {

	private final static String TAG = "poislist";
	private MyLocationManager mLocationManager;
	private Location mLocation, mLastQueryLocation;

	private final static double QUERY_DISTANCE_DEFAULT = 0.8;
	private final static double QUERY_AUTO_DISTANCE_MIN = 0.25;
	private final static String PREF_KEY_LIST_DISTANCE = "listDistance";
	public final static String EXTRA_IS_RECREATED = "org.wheelmap.android.ORIENTATION_CHANGE";
	public final static String EXTRA_FIRST_VISIBLE_POSITION = "org.wheelmap.android.FIRST_VISIBLE_POSITION";
	
	private State mState;
	private float mDistance;
	private int mFirstVisiblePosition = 0;
	private boolean isInForeground;
	
	private TextView mNoPoisText;

	GoogleAnalyticsTracker tracker;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// GA
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession("UA-25843648-1", 20, this);
		tracker.setAnonymizeIp(true);
		tracker.trackPageView("/ListActivity");

		setContentView(R.layout.activity_list);
		mNoPoisText = (TextView) findViewById( R.id.nopois );

		TextView mapView = (TextView) findViewById(R.id.switch_maps);

		// Attach event handlers
		mapView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(POIsListActivity.this,
						POIsMapsforgeActivity.class);
				intent.putExtra(POIsMapsforgeActivity.EXTRA_NO_RETRIEVAL, false);
				startActivity(intent);
				overridePendingTransition(0, 0);
				tracker.trackEvent("Clicks", // Category
						"Button", // Action
						"SwitchMaps", // Label
						0); // Value

			}

		});

		mState = (State) getLastNonConfigurationInstance();
		final boolean previousState = mState != null;

		if (previousState) {
			// Start listening for SyncService updates again
			mState.mReceiver.setReceiver(this);
			updateRefreshStatus();
		} else {
			mState = new State();
			mState.mReceiver.setReceiver(this);
		}

		mLocationManager = MyLocationManager.get(mState.mReceiver, true);
		mLocation = mLocationManager.getLastLocation();
		mDistance = getDistanceFromPreferences();

		getListView().setTextFilterEnabled(true);

		((PullToRefreshListView) getListView()).setOnRefreshListener(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.getExtras() != null) {
			isRecreated(intent.getExtras());
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		isInForeground = true;
		Log.d( TAG, "onResume isInForeground = " + isInForeground);
		mLocationManager.register(mState.mReceiver, true);
		runQueryOnCreation();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		isInForeground = false;
		Log.d( TAG, "onPause isInForeground = " + isInForeground);
		mLocationManager.release(mState.mReceiver);
		setIsRecreated(true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the tracker when it is no longer needed.
		tracker.stopSession();
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState( state );
		isRecreated(state);
		mFirstVisiblePosition = state.getInt( EXTRA_FIRST_VISIBLE_POSITION, 1);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(EXTRA_IS_RECREATED, true);
		saveListPosition();
		outState.putInt( EXTRA_FIRST_VISIBLE_POSITION, mFirstVisiblePosition );
		super.onSaveInstanceState(outState);
	}

	private void isRecreated(Bundle state) {
		boolean isRecreated;

		if (!state.containsKey(EXTRA_IS_RECREATED))
			isRecreated = false;
		else
			isRecreated = state.getBoolean(EXTRA_IS_RECREATED);

//		Log.d(TAG, "isRecreated? isRecreated = " + isRecreated);
		setIsRecreated(isRecreated);
	}

	public void setIsRecreated(boolean recreated) {
//		Log.d(TAG, "setIsRecreated = " + recreated);
		mState.mIsRecreated = recreated;
	}

	public void runQueryOnCreation() {
		Log.d(TAG, "runQueryOnCreation: mIsRecreated = " + mState.mIsRecreated);

		if (!mState.mIsRecreated)
			((PullToRefreshListView) getListView()).prepareForRefresh();
		runQuery(!mState.mIsRecreated);
	}

	public void runQuery(boolean forceReload) {
		Log.d(TAG, "runQuery: forceReload = " + forceReload);
		if (forceReload) {
			mFirstVisiblePosition = 0;
			requestData();
		}
		// setIsRecreated(true);

		Uri uri = Wheelmap.POIs.CONTENT_URI_POI_SORTED;
		Cursor cursor = managedQuery(uri, Wheelmap.POIs.PROJECTION,
				QueriesBuilderHelper
						.userSettingsFilter(getApplicationContext()),
				createWhereValues(), "");
		Cursor wrappingCursor = createCursorWrapper(cursor);
		startManagingCursor(wrappingCursor);

		POIsListCursorAdapter adapter = new POIsListCursorAdapter(this,
				wrappingCursor);

		setListAdapter(adapter);
		Log.d( TAG, "runQuery: mFirstVisible = " + mFirstVisiblePosition );
		getListView().setSelection( mFirstVisiblePosition );

	}

	public String[] createWhereValues() {

		String[] lonlat = new String[] {
				String.valueOf(mLocation.getLongitude()),
				String.valueOf(mLocation.getLatitude()) };
		return lonlat;
	}

	public Cursor createCursorWrapper(Cursor cursor) {
		Wgs84GeoCoordinates wgsLocation = new Wgs84GeoCoordinates(
				mLocation.getLongitude(), mLocation.getLatitude());
		return new POIsCursorWrapper(cursor, wgsLocation);
	}

	private float getDistanceFromPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		String prefDist = prefs.getString(PREF_KEY_LIST_DISTANCE,
				String.valueOf(QUERY_DISTANCE_DEFAULT));
		return Float.valueOf(prefDist);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Clear any strong references to this Activity, we'll reattach to
		// handle events on the other side.
		mState.mReceiver.clearReceiver();
		return mState;
	}

	public void onInfoClick(View v) {
		Intent intent = new Intent(this, InfoActivity.class);
		startActivity(intent);
	}

	public void onNewPOIClick(View v) {
		saveListPosition();
		
		// create new POI and start editing
		ContentValues cv = new ContentValues();
		cv.put(Wheelmap.POIs.NAME, getString(R.string.new_default_name));
		cv.put(Wheelmap.POIs.COORD_LAT,
				Math.ceil(mLocation.getLatitude() * 1E6));
		cv.put(Wheelmap.POIs.COORD_LON,
				Math.ceil(mLocation.getLongitude() * 1E6));
		cv.put(Wheelmap.POIs.CATEGORY_ID, 1);
		cv.put(Wheelmap.POIs.NODETYPE_ID, 1);

		Uri new_pois = getContentResolver().insert(Wheelmap.POIs.CONTENT_URI,
				cv);

		// edit activity
		Log.i(TAG, new_pois.toString());
		long poiId = Long.parseLong(new_pois.getLastPathSegment());
		Intent i = new Intent(POIsListActivity.this,
				POIDetailActivityEditable.class);
		i.putExtra(Wheelmap.POIs.EXTRAS_POI_ID, poiId);
		startActivity(i);

	}
	
	private void saveListPosition() {
		mFirstVisiblePosition = getListView().getFirstVisiblePosition();
	}

	@Override
	public void onRefresh() {
		runQuery(true);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		startActivity(new Intent(this, NewSettingsActivity.class));
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		saveListPosition();
		Cursor cursor = (Cursor) l.getAdapter().getItem(position);
		if ( cursor == null )
			return;
		
		long poiId = POIHelper.getId(cursor);
		Intent i = new Intent(POIsListActivity.this, POIDetailActivity.class);
		i.putExtra(Wheelmap.POIs.EXTRAS_POI_ID, poiId);
		startActivity(i);
	}

	private void updateRefreshStatus() {
		if ( mState.mSyncing )
			mNoPoisText.setVisibility( View.INVISIBLE );
		else {
			mNoPoisText.setVisibility( View.VISIBLE );
			((PullToRefreshListView) getListView()).onRefreshComplete();
		}
	}

	private boolean isFarerThanDeltaDistance(Location location) {
		if (mLastQueryLocation == null)
			return false;

		Wgs84GeoCoordinates crrCoordinates = new Wgs84GeoCoordinates(
				location.getLongitude(), location.getLatitude());
		Wgs84GeoCoordinates lastQueryCoordinates = new Wgs84GeoCoordinates(
				mLastQueryLocation.getLongitude(),
				mLastQueryLocation.getLatitude());

		if (GeocoordinatesMath.calculateDistance(lastQueryCoordinates,
				crrCoordinates) >= QUERY_AUTO_DISTANCE_MIN)
			return true;
		else
			return false;
	}

	/** {@inheritDoc} */
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Log.d( TAG, "onReceiveResult in list resultCode = " + resultCode );
		switch (resultCode) {
		case SyncService.STATUS_RUNNING: {
			mState.mSyncing = true;
			updateRefreshStatus();
			break;
		}
		case SyncService.STATUS_FINISHED: {
			mState.mSyncing = false;
			updateRefreshStatus();
			break;
		}
		case SyncService.STATUS_ERROR: {
			// Error happened down in SyncService, show as toast.
			mState.mSyncing = false;
			updateRefreshStatus();
			final SyncServiceException e = resultData
					.getParcelable(SyncService.EXTRA_ERROR);
			showErrorDialog(e);
			break;
		}
		case MyLocationManager.WHAT_LOCATION_MANAGER_UPDATE: {
			mLocation = (Location) resultData
					.getParcelable(MyLocationManager.EXTRA_LOCATION_MANAGER_LOCATION);
			if (isFarerThanDeltaDistance(mLocation)) {
				((PullToRefreshListView) getListView()).prepareForRefresh();
				runQuery(true);
			}
			break;
		}
		}
	}

	/**
	 * State specific to {@link HomeActivity} that is held between configuration
	 * changes. Any strong {@link Activity} references <strong>must</strong> be
	 * cleared before {@link #onRetainNonConfigurationInstance()}, and this
	 * class should remain {@code static class}.
	 */
	private static class State {
		public DetachableResultReceiver mReceiver;
		public boolean mSyncing = false;
		public boolean mIsRecreated = false;

		private State() {
			mReceiver = new DetachableResultReceiver(new Handler());
		}
	}

	private void requestData() {
		final Intent intent = new Intent(Intent.ACTION_SYNC, null,
				POIsListActivity.this, SyncService.class);
		intent.putExtra(SyncService.EXTRA_WHAT, SyncService.WHAT_RETRIEVE_NODES);
		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mState.mReceiver);
		intent.putExtra(SyncService.EXTRA_LOCATION, mLocation);
		intent.putExtra(SyncService.EXTRA_DISTANCE_LIMIT, mDistance);
		startService(intent);
		mLastQueryLocation = mLocation;
	}

	private void showErrorDialog(SyncServiceException e) {
		if (!isInForeground)
			return;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Log.d( TAG, "showErrorDialog: e.getCode = " + e.getErrorCode());
		if ( e.getErrorCode() == SyncServiceException.ERROR_NETWORK_FAILURE)
			builder.setTitle( R.string.error_network_title );
		else
			builder.setTitle(R.string.error_occurred);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setMessage(e.getRessourceString());
		builder.setNeutralButton(R.string.okay,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
