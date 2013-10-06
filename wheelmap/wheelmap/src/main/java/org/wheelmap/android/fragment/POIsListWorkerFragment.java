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

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.wheelmap.android.fragment.SearchDialogFragment.OnSearchDialogListener;
import org.wheelmap.android.manager.MyLocationManager;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Extra.What;
import org.wheelmap.android.model.POIsCursorWrapper;
import org.wheelmap.android.model.PrefKey;
import org.wheelmap.android.model.UserQueryHelper;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.service.RestService;
import org.wheelmap.android.service.RestServiceException;
import org.wheelmap.android.service.RestServiceHelper;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.DetachableResultReceiver.Receiver;
import org.wheelmap.android.utils.GeoMath;

import android.app.SearchManager;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import de.akquinet.android.androlog.Log;
import de.greenrobot.event.EventBus;

public class POIsListWorkerFragment extends Fragment implements
        WorkerFragment, Receiver, LoaderCallbacks<Cursor>,
        OnSearchDialogListener {

    public static final String TAG = POIsListWorkerFragment.class
            .getSimpleName();

    private final static int LOADER_ID_LIST = 0;

    private final static float QUERY_DISTANCE_DEFAULT = 0.8f;

    private final static float DISTANCE_TO_RELOAD = 1l;

    private DisplayFragment mDisplayFragment;

    private WorkerFragmentListener mListener;

    private DetachableResultReceiver mReceiver;

    private boolean mRefreshStatus = false;

    private float mDistance;

    private Cursor mCursor;

    private EventBus mBus;

    private Location mLocation;

    private Location mLastLocation;

    private String mQuery;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof WorkerFragmentListener) {
            mListener = (WorkerFragmentListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setRetainInstance(true);
        mDistance = getDistanceFromPreferences();
        mReceiver = new DetachableResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        mBus = EventBus.getDefault();
        retrieveInitialLocation();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID_LIST, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
        mBus.post(MyLocationManager.RegisterEvent.INSTANCE);
    }

    @Override
    public void onStop() {
        super.onStop();
        mBus.post(MyLocationManager.UnregisterEvent.INSTANCE);
        mBus.unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mReceiver.clearReceiver();
    }

    private void retrieveInitialLocation() {
        MyLocationManager.LocationEvent event = (MyLocationManager.LocationEvent) mBus
                .getStickyEvent(MyLocationManager.LocationEvent.class);
        mLocation = event.location;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(Extra.IS_RESTARTED, true);
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
                final RestServiceException e = resultData
                        .getParcelable(Extra.EXCEPTION);
                if (mListener != null) {
                    mListener.onError(e);
                }
                break;
            }
        }
    }

    private void setRefreshStatus(boolean refreshState) {
        mRefreshStatus = refreshState;
        update();
    }

    private void resetCursorLoaderUri() {
        Loader<Cursor> loader = getLoaderManager().getLoader(LOADER_ID_LIST);
        if (loader == null) {
            return;
        }

        CursorLoader cl = (CursorLoader) loader;
        cl.setUri(POIs.createUriSorted(mLocation));
        loader.forceLoad();
    }

    private float getDistanceFromPreferences() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getSupportActivity().getApplicationContext());

        float prefDist = prefs.getFloat(PrefKey.LIST_DISTANCE,
                QUERY_DISTANCE_DEFAULT);
        return prefDist;

    }

    private boolean isNewDistanceFar() {
        float distance = GeoMath.calculateDistance(mLastLocation, mLocation);

        if (distance > DISTANCE_TO_RELOAD) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");

        return new CursorLoader(getActivity(),
                POIs.createUriSorted(mLocation), POIs.PROJECTION, mQuery,
                null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Cursor wrappingCursor = new POIsCursorWrapper(cursor, mLocation);
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
        if (mDisplayFragment != null) {
            mDisplayFragment.onUpdate(this);
        }
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
        Log.d(TAG, "requestUpdate mLocation = " + mLocation + " mDistance = " + mDistance);
        RestServiceHelper.retrieveNodesByDistance(getActivity(), mLocation,
                mDistance, mReceiver);
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

        if (bundle.containsKey(Extra.DISTANCE_LIMIT)) {
            bundle.putParcelable(Extra.LOCATION, mLocation);
        }

        bundle.putParcelable(Extra.STATUS_RECEIVER, mReceiver);
        RestServiceHelper.executeRequest(getActivity(), bundle);
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

    public void onEventMainThread(UserQueryHelper.UserQueryUpdateEvent e) {
        Log.d(TAG, "onUserQueryChanged: received event");
        mQuery = e.query;
        if (mLocation != null) {
            getLoaderManager().restartLoader(LOADER_ID_LIST, null, this);
        }
    }

    public void onEventMainThread(MyLocationManager.LocationEvent event) {
        Log.d(TAG, "updateLocation: location = " + event.location);
        mLastLocation = mLocation;
        mLocation = event.location;
        resetCursorLoaderUri();

        if (isNewDistanceFar()) {
            Log.d(TAG, "updateLocation: isNewDistanceFar results true");
            requestUpdate(null);
        }
    }
}
