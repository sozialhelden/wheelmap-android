package org.wheelmap.android.fragment;

import java.util.HashSet;
import java.util.Set;

import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.fragment.SearchDialogFragment.OnSearchDialogListener;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Extra.What;
import org.wheelmap.android.model.POIsCursorWrapper;
import org.wheelmap.android.model.UserQueryHelper;
import org.wheelmap.android.model.UserQueryUpdateEvent;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.service.SyncServiceHelper;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.DetachableResultReceiver.Receiver;

import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;
import android.app.Activity;
import android.app.SearchManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import de.akquinet.android.androlog.Log;

public class CombinedWorkerFragment extends LocationFragment implements
		WorkerFragment, Receiver, LoaderCallbacks<Cursor>,
		OnSearchDialogListener {
	public final static String TAG = CombinedWorkerFragment.class
			.getSimpleName();
	Set<DisplayFragment> mListener = new HashSet<DisplayFragment>();

	public final static int LOADER_LIST_ID = 0;
	public final static int LOADER_MAP_ID = 1;

	private final static float QUERY_DISTANCE_DEFAULT = 0.8f;

	private WorkerFragmentListener mFragmentListener;
	private DetachableResultReceiver mReceiver;

	private Cursor mListCursor;
	private Cursor mMapCursor;

	boolean isSearchMode;
	private boolean mRefreshStatus;
	private Bus mBus;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof WorkerFragmentListener)
			mFragmentListener = (WorkerFragmentListener) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		mBus = WheelmapApp.getBus();
		mBus.register(this);

		mReceiver = new DetachableResultReceiver(new Handler());
		mReceiver.setReceiver(this);

		requestUpdate(null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "starting both loaders");
		getLoaderManager().initLoader(LOADER_LIST_ID, null, this);
		getLoaderManager().initLoader(LOADER_MAP_ID, null, this);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
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

	@Override
	public void registerDisplayFragment(DisplayFragment fragment) {
		mListener.add(fragment);
	}

	@Override
	public void unregisterDisplayFragment(DisplayFragment fragment) {
		mListener.remove(fragment);
	}

	@Override
	protected void updateLocation() {
		Log.d(TAG, "updateLocation");
		updateDisplayLocation();
		resetCursorLoaderUri();
	}

	private void updateDisplayLocation() {
		Log.d(TAG, "updateDisplayLocation fragments = " + mListener.size());
		for (DisplayFragment fragment : mListener) {
			Log.d(TAG, "updateDisplayLocation setCurrentLocation on "
					+ fragment.getTag());
			fragment.setCurrentLocation(getLocation());
		}
	}

	private void setRefreshStatus(boolean refreshState) {
		mRefreshStatus = refreshState;
		update();
	}

	public void update() {
		for (DisplayFragment fragment : mListener) {
			fragment.onUpdate(this);
		}
	}

	@Override
	public void requestUpdate(Bundle bundle) {
		if (isSearchMode)
			return;

		if (bundle == null) {
			SyncServiceHelper.retrieveNodesByDistance(getActivity(),
					getLocation(), QUERY_DISTANCE_DEFAULT, mReceiver);
		} else {
			bundle.putInt(Extra.WHAT, What.RETRIEVE_NODES);
			bundle.putParcelable(Extra.STATUS_RECEIVER, mReceiver);
			SyncServiceHelper.executeRequest(getActivity(), bundle);
		}
	}

	@Override
	public void onSearch(Bundle bundle) {
		Log.d(TAG, "requestSearch with bundle " + bundle.toString());
		if (bundle.containsKey(Extra.ENABLE_BOUNDING_BOX)) {
			Fragment f = getFragmentManager().findFragmentByTag(
					POIsMapsforgeFragment.TAG);
			((OnSearchDialogListener) f).onSearch(bundle);

		}
		requestSearch(bundle);
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

		if (bundle.containsKey(Extra.BOUNDING_BOX)) {
			// noop
		} else if (bundle.containsKey(Extra.DISTANCE_LIMIT)) {
			bundle.putParcelable(Extra.LOCATION, getLocation());
			bundle.remove(Extra.BOUNDING_BOX);
		}

		bundle.putParcelable(Extra.STATUS_RECEIVER, mReceiver);
		SyncServiceHelper.executeRequest(getActivity(), bundle);
		setSearchModeInt(true);
	}

	@Override
	public Cursor getCursor(int id) {
		if (id == LIST_CURSOR)
			return mListCursor;
		else if (id == MAP_CURSOR)
			return mMapCursor;
		else
			throw new IllegalArgumentException("Cursor id not available ");
	}

	@Override
	public boolean isRefreshing() {
		return mRefreshStatus;
	}

	@Override
	public boolean isSearchMode() {
		return isSearchMode;
	}

	private void setSearchModeInt(boolean searchMode) {
		Log.d(TAG, "setSearchMode: " + searchMode);
		isSearchMode = searchMode;
		if (mFragmentListener != null)
			mFragmentListener.onSearchModeChange(isSearchMode);
		update();
	}

	@Override
	public void setSearchMode(boolean searchMode) {
		Log.d(TAG, "setSearchMode: " + isSearchMode);
		isSearchMode = searchMode;
		update();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == LOADER_MAP_ID) {
			Uri uri = POIs.CONTENT_URI_RETRIEVED;
			return new CursorLoader(getActivity(), uri, POIs.PROJECTION,
					UserQueryHelper.getUserQuery(), null, null);
		} else {
			String query = UserQueryHelper.getUserQuery();
			return new CursorLoader(getActivity(),
					POIs.createUriSorted(getLocation()), POIs.PROJECTION,
					query, null, null);
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader.getId() == LOADER_MAP_ID) {
			mMapCursor = cursor;
		} else {
			Wgs84GeoCoordinates location = new Wgs84GeoCoordinates(
					getLocation());
			Cursor wrappingCursor = new POIsCursorWrapper(cursor, location);
			Log.d(TAG, "cursorloader - new cursor - cursor size = "
					+ wrappingCursor.getCount());
			mListCursor = wrappingCursor;
		}
		update();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d(TAG, "onLoaderReset - why is that?");
	}

	private void resetCursorLoaderUri() {
		Loader<Cursor> loader = getLoaderManager().getLoader(LOADER_LIST_ID);
		if (loader == null)
			return;

		CursorLoader cl = (CursorLoader) loader;
		cl.setUri(POIs.createUriSorted(getLocation()));
		loader.forceLoad();
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
			SyncServiceException e = resultData.getParcelable(Extra.EXCEPTION);
			if (mFragmentListener != null)
				mFragmentListener.onError(e);
			break;
		}

		}
	}

	@Subscribe
	public void onUserQueryChanged(UserQueryUpdateEvent e) {
		Log.d(TAG, "onUserQueryChanged: received event");
		getLoaderManager().restartLoader(LOADER_LIST_ID, null, this);
		getLoaderManager().restartLoader(LOADER_MAP_ID, null, this);
	}

}
