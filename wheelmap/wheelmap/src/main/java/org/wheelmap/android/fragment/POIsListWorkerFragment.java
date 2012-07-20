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

import org.wheelmap.android.fragment.SearchDialogFragment.OnSearchDialogListener;
import org.wheelmap.android.manager.MyLocationManager;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Extra.What;
import org.wheelmap.android.model.POIsCursorWrapper;
import org.wheelmap.android.model.QueriesBuilderHelper;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.utils.DetachableResultReceiver;

import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.actionbarsherlock.app.SherlockFragment;

import de.akquinet.android.androlog.Log;

public class POIsListWorkerFragment extends SherlockFragment implements
		WorkerFragment, DetachableResultReceiver.Receiver,
		LoaderCallbacks<Cursor>, OnSearchDialogListener {
	public static final String TAG = POIsListWorkerFragment.class
			.getSimpleName();
	private final static int LOADER_ID_LIST = 0;
	private final static double QUERY_DISTANCE_DEFAULT = 0.8;
	private final static String PREF_KEY_LIST_DISTANCE = "listDistance";

	private DisplayFragment mDisplayFragment;

	private OnPOIsListWorkerListener mListener;
	private DetachableResultReceiver mReceiver;
	private boolean mRefreshStatus = false;

	private MyLocationManager mLocationManager;
	private Location mLocation;
	private float mDistance;
	private Cursor mCursor;

	public interface OnPOIsListWorkerListener {

		public void onError(SyncServiceException e);
	}

	public POIsListWorkerFragment() {
		super();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof OnPOIsListWorkerListener)
			mListener = (OnPOIsListWorkerListener) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		setRetainInstance(true);

		mReceiver = new DetachableResultReceiver(new Handler());
		mReceiver.setReceiver(this);
		mLocationManager = MyLocationManager.get(mReceiver, true);
		mLocation = mLocationManager.getLastLocation();
		mDistance = getDistanceFromPreferences();
		requestUpdate(null);
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

	/** {@inheritDoc} */
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Log.d(TAG, "onReceiveResult in list resultCode = " + resultCode);
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
			final SyncServiceException e = resultData
					.getParcelable(Extra.EXCEPTION);
			if (mListener != null)
				mListener.onError(e);
			break;
		}
		case What.LOCATION_MANAGER_UPDATE: {
			mLocation = (Location) resultData.getParcelable(Extra.LOCATION);
			break;
		}
		}
	}

	private void setRefreshStatus(boolean refreshState) {
		mRefreshStatus = refreshState;
		update();
	}

	private float getDistanceFromPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity()
						.getApplicationContext());

		String prefDist = prefs.getString(PREF_KEY_LIST_DISTANCE,
				String.valueOf(QUERY_DISTANCE_DEFAULT));
		return Float.valueOf(prefDist);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Log.d(TAG, "onCreateLoader");
		Uri uri = Wheelmap.POIs.CONTENT_URI_POI_SORTED;
		String query = QueriesBuilderHelper.userSettingsFilter(getActivity());
		String whereValues[] = new String[] {
				String.valueOf(mLocation.getLongitude()),
				String.valueOf(mLocation.getLatitude()) };

		return new CursorLoader(getActivity(), uri, Wheelmap.POIs.PROJECTION,
				query, whereValues, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Wgs84GeoCoordinates location = new Wgs84GeoCoordinates(
				mLocation.getLongitude(), mLocation.getLatitude());

		Cursor wrappingCursor = new POIsCursorWrapper(cursor, location);
		Log.d(TAG, "cursorloader - new cursor - cursor size = "
				+ wrappingCursor.getCount());
		mCursor = wrappingCursor;
		update();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		Log.d(TAG, "onLoaderReset - why is that?");
	}

	@Override
	public void onSearch(Bundle bundle) {
		requestSearch(bundle);
	}

	public void update() {
		if (mDisplayFragment != null)
			mDisplayFragment.onUpdate(this);
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
	public void requestUpdate(Bundle bundle) {
		final Intent intent = new Intent(Intent.ACTION_SYNC, null,
				getActivity(), SyncService.class);
		intent.putExtra(Extra.WHAT, What.RETRIEVE_NODES);
		intent.putExtra(Extra.STATUS_RECEIVER, mReceiver);
		intent.putExtra(Extra.LOCATION, mLocation);
		intent.putExtra(Extra.DISTANCE_LIMIT, mDistance);
		getActivity().startService(intent);
	}

	@Override
	public void requestSearch(Bundle bundle) {
		if (!bundle.containsKey(SearchManager.QUERY)
				&& !bundle.containsKey(Extra.CATEGORY)
				&& !bundle.containsKey(Extra.NODETYPE)
				&& !bundle.containsKey(Extra.WHEELCHAIR_STATE))
			return;

		if (bundle.getInt(Extra.CATEGORY) == -1)
			bundle.remove(Extra.CATEGORY);

		if (!bundle.containsKey(Extra.WHAT)) {
			int what;
			if (bundle.containsKey(Extra.CATEGORY)
					|| bundle.containsKey(Extra.NODETYPE))
				what = What.RETRIEVE_NODES;
			else
				what = What.SEARCH_NODES;

			bundle.putInt(Extra.WHAT, what);
		}

		if (bundle.containsKey(Extra.DISTANCE_LIMIT))
			bundle.putParcelable(Extra.LOCATION, mLocation);

		bundle.putParcelable(Extra.STATUS_RECEIVER, mReceiver);

		final Intent intent = new Intent(Intent.ACTION_SYNC, null,
				getActivity(), SyncService.class);
		intent.putExtras(bundle);
		getActivity().startService(intent);
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
		return false;
	}

	@Override
	public void setSearchMode(boolean isSearchMode) {
	}
}
