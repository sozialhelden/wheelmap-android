package org.wheelmap.android.fragment;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.wheelmap.android.fragment.SearchDialogFragment.OnSearchDialogListener;
import org.wheelmap.android.manager.MyLocationManager;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Extra.What;
import org.wheelmap.android.model.POIsCursorWrapper;
import org.wheelmap.android.model.UserQueryHelper;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.service.RestService;
import org.wheelmap.android.service.RestServiceException;
import org.wheelmap.android.service.RestServiceHelper;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.DetachableResultReceiver.Receiver;

import android.app.SearchManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import java.util.HashSet;
import java.util.Set;

import de.akquinet.android.androlog.Log;
import de.greenrobot.event.EventBus;

public class CombinedWorkerFragment extends Fragment implements
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

    private EventBus mBus;

    private Location mLocation;

    private String mUserQuery;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof WorkerFragmentListener) {
            mFragmentListener = (WorkerFragmentListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mReceiver = new DetachableResultReceiver(new Handler());
        mReceiver.setReceiver(this);

        requestUpdate(null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "starting both loaders");
        mBus = EventBus.getDefault();
        mBus.register(this);
        getLoaderManager().initLoader(LOADER_LIST_ID, null, this);
        getLoaderManager().initLoader(LOADER_MAP_ID, null, this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mBus.post(new MyLocationManager.UnregisterEvent());
        mBus.unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mReceiver.clearReceiver();
    }

    @Override
    public void registerDisplayFragment(DisplayFragment fragment) {
        mListener.add(fragment);
    }

    @Override
    public void unregisterDisplayFragment(DisplayFragment fragment) {
        mListener.remove(fragment);
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
        if (isSearchMode) {
            return;
        }

        if (bundle == null) {
            RestServiceHelper.retrieveNodesByDistance(getActivity(),
                    mLocation, QUERY_DISTANCE_DEFAULT, mReceiver);
        } else {
            bundle.putInt(Extra.WHAT, What.RETRIEVE_NODES);
            bundle.putParcelable(Extra.STATUS_RECEIVER, mReceiver);
            RestServiceHelper.executeRequest(getActivity(), bundle);
        }
    }

    @Override
    public void onSearch(Bundle bundle) {
        Log.d(TAG, "requestSearch with bundle " + bundle.toString());
        if (bundle.containsKey(Extra.ENABLE_BOUNDING_BOX)) {
            Fragment f = (Fragment) getFragmentManager().findFragmentByTag(
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
                && !bundle.containsKey(Extra.WHEELCHAIR_STATE)) {
            return;
        }

        if (bundle.getInt(Extra.CATEGORY) == Extra.UNKNOWN) {
            bundle.remove(Extra.CATEGORY);
        }

        if (!bundle.containsKey(Extra.WHAT)) {
            int what;
            if (bundle.containsKey(Extra.CATEGORY)
                    || bundle.containsKey(Extra.NODETYPE)) {
                what = What.RETRIEVE_NODES;
            } else {
                what = What.SEARCH_NODES;
            }

            bundle.putInt(Extra.WHAT, what);
        }

        if (bundle.containsKey(Extra.BOUNDING_BOX)) {
            // noop
        } else if (bundle.containsKey(Extra.DISTANCE_LIMIT)) {
            bundle.putParcelable(Extra.LOCATION, mLocation);
            bundle.remove(Extra.BOUNDING_BOX);
        }

        bundle.putParcelable(Extra.STATUS_RECEIVER, mReceiver);
        RestServiceHelper.executeRequest(getActivity(), bundle);
        setSearchModeInt(true);
    }

    @Override
    public Cursor getCursor(int id) {
        if (id == LIST_CURSOR) {
            return mListCursor;
        } else if (id == MAP_CURSOR) {
            return mMapCursor;
        } else {
            throw new IllegalArgumentException("Cursor id not available ");
        }
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
        if (mFragmentListener != null) {
            mFragmentListener.onSearchModeChange(isSearchMode);
        }
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
                    mUserQuery, null, null);
        } else {
            return new CursorLoader(getActivity(),
                    POIs.createUriSorted(mLocation), POIs.PROJECTION,
                    mUserQuery, null, null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == LOADER_MAP_ID) {
            mMapCursor = cursor;
        } else {
            Cursor wrappingCursor = new POIsCursorWrapper(cursor, mLocation);
            Log.d(TAG, "cursorloader - new cursor - cursor size = "
                    + wrappingCursor.getCount());
            mListCursor = wrappingCursor;
        }

        update();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset - need to set it to null");

        if (loader.getId() == LOADER_MAP_ID) {
            mMapCursor = null;
        } else {
            mListCursor = null;
        }

        update();
    }

    private void resetCursorLoaderUri() {
        Loader<Cursor> loader = getLoaderManager().getLoader(LOADER_LIST_ID);
        if (loader == null) {
            return;
        }

        CursorLoader cl = (CursorLoader) loader;
        cl.setUri(POIs.createUriSorted(mLocation));
        loader.forceLoad();
    }

    /**
     * {@inheritDoc}
     */
    public void onReceiveResult(int resultCode, Bundle resultData) {
        Log.d(TAG, "onReceiveResult resultCode = " + resultCode);
        switch (resultCode) {
            case RestService.STATUS_RUNNING: {
                setRefreshStatus(true);
                break;
            }
            case RestService.STATUS_FINISHED: {
                setRefreshStatus(false);
                break;
            }
            case RestService.STATUS_ERROR: {
                setRefreshStatus(false);
                RestServiceException e = resultData.getParcelable(Extra.EXCEPTION);
                if (mFragmentListener != null) {
                    mFragmentListener.onError(e);
                }
                break;
            }

        }
    }

    public void onEventMainThread(MyLocationManager.LocationEvent locationEvent) {
        Log.d(TAG, "updateLocation");
        mLocation = locationEvent.location;
        resetCursorLoaderUri();
    }

    public void onEventMainThread(UserQueryHelper.UserQueryUpdateEvent e) {
        Log.d(TAG, "onUserQueryChanged: received event");
        mUserQuery = e.query;
        getLoaderManager().restartLoader(LOADER_LIST_ID, null, this);
        getLoaderManager().restartLoader(LOADER_MAP_ID, null, this);
    }

}
