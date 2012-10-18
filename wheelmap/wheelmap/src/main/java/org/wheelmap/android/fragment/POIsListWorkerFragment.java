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

import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.fragment.SearchDialogFragment.OnSearchDialogListener;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Extra.What;
import org.wheelmap.android.model.POIsCursorWrapper;
import org.wheelmap.android.model.PrefKey;
import org.wheelmap.android.model.UserQueryHelper;
import org.wheelmap.android.model.UserQueryUpdateEvent;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.service.SyncServiceHelper;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.DetachableResultReceiver.Receiver;

import android.app.Activity;
import android.app.SearchManager;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import de.akquinet.android.androlog.Log;

public class POIsListWorkerFragment extends LocationFragment implements
		WorkerFragment, Receiver, LoaderCallbacks<Cursor>,
		OnSearchDialogListener {
	public static final String TAG = POIsListWorkerFragment.class
			.getSimpleName();
	private final static int LOADER_ID_LIST = 0;
	private final static double QUERY_DISTANCE_DEFAULT = 0.8;
	private DisplayFragment mDisplayFragment;

	private WorkerFragmentListener mListener;
	private DetachableResultReceiver mReceiver;
	private boolean mRefreshStatus = false;

	private float mDistance;
	private Cursor mCursor;
	private Bus mBus;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof WorkerFragmentListener)
			mListener = (WorkerFragmentListener) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setRetainInstance(true);

		mBus = WheelmapApp.getBus();
		mBus.register(this);
		mReceiver = new DetachableResultReceiver(new Handler());
		mReceiver.setReceiver(this);
		mDistance = getDistanceFromPreferences();
		requestUpdate(null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(LOADER_ID_LIST, null, this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mReceiver.clearReceiver();
		mBus.unregister(this);
	}

	/** {@inheritDoc} */
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Log.d(TAG, "onReceiveResult resultCode = " + resultCode);
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
		}
	}

	protected void updateLocation() {
		resetCursorLoaderUri();
	}

	private void setRefreshStatus(boolean refreshState) {
		mRefreshStatus = refreshState;
		update();
	}

	private void resetCursorLoaderUri() {
		Loader<Cursor> loader = getLoaderManager().getLoader(LOADER_ID_LIST);
		if (loader == null)
			return;

		CursorLoader cl = (CursorLoader) loader;
		cl.setUri(POIs.createUriSorted(getLocation()));
		loader.forceLoad();
	}

	private float getDistanceFromPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity()
						.getApplicationContext());

		String prefDist = prefs.getString(PrefKey.LIST_DISTANCE,
				String.valueOf(QUERY_DISTANCE_DEFAULT));
		return Float.valueOf(prefDist);

	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader");
		String query = UserQueryHelper.getUserQuery();
		return new CursorLoader(getActivity(),
				POIs.createUriSorted(getLocation()), POIs.PROJECTION, query,
				null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Cursor wrappingCursor = new POIsCursorWrapper(cursor, getLocation());
		Log.d(TAG, "cursorloader - new cursor - cursor size = "
				+ wrappingCursor.getCount());
		mCursor = wrappingCursor;
		update();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		Log.d(TAG, "onLoaderReset - need to set it to null");

		mCursor = null;
		update();
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
		SyncServiceHelper.retrieveNodesByDistance(getActivity(), getLocation(),
				mDistance, mReceiver);
	}

	@Override
	public void requestSearch(Bundle bundle) {
		if (!bundle.containsKey(SearchManager.QUERY)
				&& !bundle.containsKey(Extra.CATEGORY)
				&& !bundle.containsKey(Extra.NODETYPE)
				&& !bundle.containsKey(Extra.WHEELCHAIR_STATE))
			return;

		if (bundle.getInt(Extra.CATEGORY) == Extra.UNKNOWN)
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
			bundle.putParcelable(Extra.LOCATION, getLocation());

		bundle.putParcelable(Extra.STATUS_RECEIVER, mReceiver);
		SyncServiceHelper.executeRequest(getActivity(), bundle);
	}

	@Override
	public Cursor getCursor(int id) {
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

	@Subscribe
	public void onUserQueryChanged(UserQueryUpdateEvent e) {
		Log.d(TAG, "onUserQueryChanged: received event");
		getLoaderManager().restartLoader(LOADER_ID_LIST, null, this);
	}
}
