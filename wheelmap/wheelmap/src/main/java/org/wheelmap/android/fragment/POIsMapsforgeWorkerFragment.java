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
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Extra.What;
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

import com.actionbarsherlock.app.SherlockFragment;

import de.akquinet.android.androlog.Log;

public class POIsMapsforgeWorkerFragment extends SherlockFragment implements
		WorkerFragment, DetachableResultReceiver.Receiver,
		LoaderCallbacks<Cursor> {
	public final static String TAG = POIsMapsforgeWorkerFragment.class
			.getSimpleName();
	private final static int LOADER_ID_LIST = 0;

	private DisplayFragment mDisplayFragment;
	private OnPOIsMapsforgeWorkerListener mListener;
	private DetachableResultReceiver mReceiver;

	private MyLocationManager mLocationManager;
	private Location mLocation;
	private Cursor mCursor;

	boolean isSearchMode;
	private boolean mRefreshStatus;
	private GeoPoint mGeoPoint;

	public interface OnPOIsMapsforgeWorkerListener {
		void onError(SyncServiceException e);

		void onSearchModeChange(boolean isSearchMode);
	}

	public POIsMapsforgeWorkerFragment() {
		super();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof OnPOIsMapsforgeWorkerListener)
			mListener = (OnPOIsMapsforgeWorkerListener) activity;
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
			setRefreshStatus(true);
			break;
		}
		case SyncService.STATUS_FINISHED: {
			setRefreshStatus(false);
			break;
		}
		case SyncService.STATUS_ERROR: {
			setRefreshStatus(false);
			SyncServiceException e = resultData.getParcelable(Extra.EXCEPTION);
			if (mListener != null)
				mListener.onError(e);
			break;
		}
		case What.LOCATION_MANAGER_UPDATE: {
			Location location = (Location) resultData
					.getParcelable(Extra.LOCATION);
			mGeoPoint = calcGeoPoint(location);
			mLocation = location;

			updateDisplayLocation();
			break;
		}

		}
	}

	private void updateDisplayLocation() {
		if (mDisplayFragment != null)
			mDisplayFragment.setCurrentLocation(mGeoPoint, mLocation);
	}

	private void setSearchModeInternal(boolean isSearchMode) {
		this.isSearchMode = isSearchMode;
		update();
	}

	private void setRefreshStatus(boolean refreshState) {
		mRefreshStatus = refreshState;
		update();
	}

	private void update() {
		if (mDisplayFragment != null)
			mDisplayFragment.onUpdate(this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Log.d(TAG, "onCreateLoader");

		Uri uri = Wheelmap.POIs.CONTENT_URI;
		return new CursorLoader(getActivity(), uri, Wheelmap.POIs.PROJECTION,
				QueriesBuilderHelper.userSettingsFilter(getActivity()
						.getApplicationContext()), null,
				Wheelmap.POIs.DEFAULT_SORT_ORDER);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d(TAG,
				"cursorloader - switching cursors in adapter - cursor size = "
						+ cursor.getCount());
		mCursor = cursor;
		update();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		Log.d(TAG, "onLoaderReset - why is that?");
	}

	@Override
	public void requestSearch(Bundle bundle) {
		if (!bundle.containsKey(SearchManager.QUERY)
				&& !bundle.containsKey(Extra.CATEGORY)
				&& !bundle.containsKey(Extra.NODETYPE)
				&& !bundle.containsKey(Extra.WHEELCHAIR_STATE))
			return;

		if (!bundle.containsKey(Extra.WHAT)) {
			int what;
			if (bundle.containsKey(Extra.CATEGORY)
					|| bundle.containsKey(Extra.NODETYPE))
				what = What.RETRIEVE_NODES;
			else
				what = What.SEARCH_NODES_IN_BOX;

			bundle.putInt(Extra.WHAT, what);
		}

		bundle.putParcelable(Extra.STATUS_RECEIVER, mReceiver);

		final Intent intent = new Intent(Intent.ACTION_SYNC, null,
				getActivity(), SyncService.class);
		intent.putExtras(bundle);
		getActivity().startService(intent);
		setSearchMode(true);
	}

	@Override
	public void requestUpdate(Bundle bundle) {
		if (isSearchMode)
			return;

		final Intent intent = new Intent(Intent.ACTION_SYNC, null,
				getActivity(), SyncService.class);
		intent.putExtras(bundle);
		intent.putExtra(Extra.WHAT, What.RETRIEVE_NODES);
		intent.putExtra(Extra.STATUS_RECEIVER, mReceiver);
		getActivity().startService(intent);
	}

	@Override
	public void registerDisplayFragment(DisplayFragment fragment) {
		mDisplayFragment = fragment;
	}

	@Override
	public void unregisterDisplayFragment(DisplayFragment fragment) {
		mDisplayFragment = null;
	}

	@Override
	public Cursor getCursor() {
		return mCursor;
	}

	@Override
	public boolean isRefreshing() {
		return mRefreshStatus;
	}

	@Override
	public boolean isSearchMode() {
		return isSearchMode;
	}

	@Override
	public void setSearchMode(boolean isSearchMode) {
		this.isSearchMode = isSearchMode;
	}
}
