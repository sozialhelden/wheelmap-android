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
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Extra.What;
import org.wheelmap.android.model.UserQueryHelper;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.service.RestService;
import org.wheelmap.android.service.RestServiceException;
import org.wheelmap.android.service.RestServiceHelper;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.DetachableResultReceiver.Receiver;

import android.app.SearchManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import de.akquinet.android.androlog.Log;
import de.greenrobot.event.EventBus;

public class POIsMapWorkerFragment extends Fragment implements
        WorkerFragment, Receiver, LoaderCallbacks<Cursor> {

    public final static String TAG = POIsMapWorkerFragment.class
            .getSimpleName();

    private final static int LOADER_ID = 0;

    private DisplayFragment mDisplayFragment;

    private WorkerFragmentListener mListener;

    private DetachableResultReceiver mReceiver;

    private Cursor mCursor;

    boolean isSearchMode;

    private boolean mRefreshStatus;

    private EventBus mBus;

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

        mReceiver = new DetachableResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        mBus = EventBus.getDefault();
        setQueryFromStickyEvent();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.registerSticky(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mBus.unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mReceiver.clearReceiver();
    }

    private void setQueryFromStickyEvent() {
        mQuery = ((UserQueryHelper.UserQueryUpdateEvent) mBus
                .getStickyEvent(UserQueryHelper.UserQueryUpdateEvent.class)).query;
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
                if (mListener != null) {
                    mListener.onError(e);
                }
                break;
            }
        }
    }

    private void setRefreshStatus(boolean refreshState) {
        mRefreshStatus = refreshState;
    }

    private void update() {
        if (mDisplayFragment != null) {
            mDisplayFragment.onUpdate(this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        Log.d(TAG, "onCreateLoader");
        Uri uri = POIs.CONTENT_URI_RETRIEVED;
        return new CursorLoader(getActivity(), uri, POIs.PROJECTION,
                mQuery, null, null);
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
        Log.d(TAG, "onLoaderReset - need to set it to null");

        mCursor = null;
        update();
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
                what = What.SEARCH_NODES_IN_BOX;
            }

            bundle.putInt(Extra.WHAT, what);
        }

        bundle.putParcelable(Extra.STATUS_RECEIVER, mReceiver);
        RestServiceHelper.executeRequest(getActivity(), bundle);
        setSearchModeInt(true);
    }

    @Override
    public void requestUpdate(Bundle bundle) {
        if (isSearchMode || getActivity() == null) {
            return;
        }

        bundle.putInt(Extra.WHAT, What.RETRIEVE_NODES);
        bundle.putParcelable(Extra.STATUS_RECEIVER, mReceiver);
        RestServiceHelper.executeRequest(getActivity(), bundle);
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
    public Cursor getCursor(int id) {
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

    private void setSearchModeInt(boolean searchMode) {
        Log.d(TAG, "setSearchMode: " + searchMode);
        isSearchMode = searchMode;
        if (mListener != null) {
            mListener.onSearchModeChange(isSearchMode);
        }
    }

    @Override
    public void setSearchMode(boolean searchMode) {
        Log.d(TAG, "setSearchMode: " + isSearchMode);
        isSearchMode = searchMode;
    }

    public void onEventMainThread(UserQueryHelper.UserQueryUpdateEvent e) {
        Log.d(TAG, "onUserQueryChanged: received event");
        mQuery = e.query;
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }
}
