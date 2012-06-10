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
package org.wheelmap.android.fragment;

import org.mapsforge.android.maps.GeoPoint;
import org.wheelmap.android.manager.MyLocationManager;
import org.wheelmap.android.model.QueriesBuilderHelper;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.utils.DetachableResultReceiver;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;

public class POIsMapsforgeWorkerFragment extends SherlockFragment implements
		DetachableResultReceiver.Receiver, LoaderCallbacks<Cursor> {
	public final static String TAG = "mapsforgeworker";
	private final static int LOADER_ID_LIST = 0;

	OnPOIsMapsforgeWorkerFragmentListener mListener;
	private DetachableResultReceiver mReceiver;

	private MyLocationManager mLocationManager;
	private Location mLocation;

	boolean mSyncing;
	boolean isSearchMode;
	private boolean mRefreshStatus;

	public POIsMapsforgeWorkerFragment() {
		super();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof OnPOIsMapsforgeWorkerFragmentListener)
			mListener = (OnPOIsMapsforgeWorkerFragmentListener) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);

		mReceiver = new DetachableResultReceiver(new Handler());
		mReceiver.setReceiver(this);
		mLocationManager = MyLocationManager.get(mReceiver, true);
		mLocation = mLocationManager.getLastLocation();

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(LOADER_ID_LIST, null, this);
		setPersistentValuesAtListener();
		setPersistentValues();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		mLocationManager.register(mReceiver, true);
	}

	@Override
	public void onPause() {
		super.onPause();
		mLocationManager.release(mReceiver);
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mReceiver.clearReceiver();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	public void executeSearch(Bundle extras) {
		if (!extras.containsKey(SearchManager.QUERY)
				&& !extras.containsKey(SyncService.EXTRA_CATEGORY)
				&& !extras.containsKey(SyncService.EXTRA_NODETYPE)
				&& !extras.containsKey(SyncService.EXTRA_WHEELCHAIR_STATE))
			return;

		final Intent intent = new Intent(Intent.ACTION_SYNC, null,
				getActivity(), SyncService.class);

		intent.putExtras(extras);
		if (!extras.containsKey(SyncService.EXTRA_WHAT)) {
			int what;
			if (extras.containsKey(SyncService.EXTRA_CATEGORY)
					|| extras.containsKey(SyncService.EXTRA_NODETYPE))
				what = SyncService.WHAT_RETRIEVE_NODES;
			else
				what = SyncService.WHAT_SEARCH_NODES_IN_BOX;

			intent.putExtra(SyncService.EXTRA_WHAT, what);
		}

		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
		getActivity().startService(intent);
		setSearchMode( true );
		updateSearchStatus();
	}

	protected void requestUpdate( Bundle extras ) {
		if (isSearchMode)
			return;

		// trigger off background sync
		final Intent intent = new Intent(Intent.ACTION_SYNC, null,
				getActivity(), SyncService.class);
		intent.putExtras(extras);
		intent.putExtra(SyncService.EXTRA_WHAT, SyncService.WHAT_RETRIEVE_NODES);
		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
		getActivity().startService(intent);
	}

	private GeoPoint calcGeoPoint(Location location) {
		int lat = (int) (location.getLatitude() * 1E6);
		int lng = (int) (location.getLongitude() * 1E6);
		return new GeoPoint(lat, lng);
	}

	/** {@inheritDoc} */
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Log.d(TAG, "onReceiveResult in mapsforge resultCode = " + resultCode);
		switch (resultCode) {
		case SyncService.STATUS_RUNNING: {
			updateRefreshStatus(true);
			break;
		}
		case SyncService.STATUS_FINISHED: {
			updateRefreshStatus(false);
			break;
		}
		case SyncService.STATUS_ERROR: {
			updateRefreshStatus(false);
			SyncServiceException e = resultData
					.getParcelable(SyncService.EXTRA_ERROR);
			if (mListener != null)
				mListener.onError(e);
			break;
		}
		case MyLocationManager.WHAT_LOCATION_MANAGER_UPDATE: {
			Location location = (Location) resultData
					.getParcelable(MyLocationManager.EXTRA_LOCATION_MANAGER_LOCATION);
			GeoPoint geoPoint = calcGeoPoint(location);
			updateTargetGeoLocation( geoPoint, location );
			break;
		}

		}
	}
	
	private void updateTargetGeoLocation( GeoPoint geoPoint, Location location ) {
		POIsMapsforgeFragment fragment = (POIsMapsforgeFragment) getTargetFragment();
		
		fragment.centerMap( geoPoint );
		fragment.updateCurrentLocation( geoPoint, location );
	}
	
	private void setPersistentValuesAtListener() {
		updateRefreshStatus( mRefreshStatus );
		updateSearchStatus();
	}
	
	private void setPersistentValues() {
		getLoaderManager().getLoader( LOADER_ID_LIST).forceLoad();
	}
	
	private void updateRefreshStatus( boolean refreshStatus ) {
		mRefreshStatus = refreshStatus;
		if (mListener != null )
			mListener.onRefreshStatusChange( refreshStatus );
	}

	public void setSearchMode(boolean isSearchMode) {
		this.isSearchMode = isSearchMode;
	}

	private void updateSearchStatus() {
		if (mListener != null)
			mListener.onSearchModeChange(isSearchMode);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Log.d( TAG, "onCreateLoader" );
		
		Uri uri = Wheelmap.POIs.CONTENT_URI;
		return new CursorLoader( getActivity(), uri, Wheelmap.POIs.PROJECTION, QueriesBuilderHelper
				.userSettingsFilter(getActivity().getApplicationContext()), null,
		Wheelmap.POIs.DEFAULT_SORT_ORDER );
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d( TAG, "cursorloader - switching cursors in adapter - cursor size = " + cursor.getCount() );
		((POIsMapsforgeFragment) getTargetFragment()).setCursor( cursor );
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		Log.d( TAG, "onLoaderReset - why is that?" );
	}

	public interface OnPOIsMapsforgeWorkerFragmentListener {
		void onError(SyncServiceException e);
		void onSearchModeChange(boolean isSearchMode);
		void onRefreshStatusChange( boolean refreshStatus );
	}

}
