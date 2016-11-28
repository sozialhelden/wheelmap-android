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

import org.wheelmap.android.activity.MainSinglePaneActivity;
import org.wheelmap.android.adapter.POIsListCursorAdapter;
import org.wheelmap.android.fragment.SearchDialogFragment.OnSearchDialogListener;
import org.wheelmap.android.manager.SupportManager.DistanceUnitChangedEvent;
import org.wheelmap.android.model.DirectionCursorWrapper;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.UtilsMisc;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import de.akquinet.android.androlog.Log;
import de.greenrobot.event.EventBus;

public class POIsListFragment extends Fragment implements
        DisplayFragment, OnSearchDialogListener, OnExecuteBundle, AdapterView.OnItemClickListener {

    public static final String TAG = POIsListFragment.class.getSimpleName();

    private WorkerFragment mWorkerFragment;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView mListView;

    private int mFirstVisiblePosition = 0;

    private int mCheckedItem;

    private boolean mFirstStart;

    private boolean mRefreshDisabled;

    public DisplayFragmentListener mListener;

    private POIsListCursorAdapter mAdapter;

    private Cursor mCursor;

    private EventBus mBus;

    private DirectionCursorWrapper mDirectionCursorWrapper;

    private boolean mUseAngloDistanceUnit;

    private String selectedItem_wmID = null;

    private boolean isRefreshing;

    public POIsListFragment() {
        super();
        Log.d(TAG, "constructor called " + hashCode());
    }

    public static POIsListFragment newInstance(boolean createWorker,
                                               boolean disableSearch) {
        createWorker = false;
        POIsListFragment f = new POIsListFragment();
        Bundle b = new Bundle();
        b.putBoolean(Extra.CREATE_WORKER_FRAGMENT, createWorker);
        b.putBoolean(Extra.DISABLE_SEARCH, disableSearch);

        f.setArguments(b);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof DisplayFragmentListener) {
            mListener = (DisplayFragmentListener) activity;
        }

        Log.d(TAG, "onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate " + hashCode());
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView " + hashCode());

        View v = inflater.inflate(R.layout.fragment_list, container, false);
        mListView = (ListView) v.findViewById(android.R.id.list);
        mAdapter = new POIsListCursorAdapter(getActivity(), null, false, mUseAngloDistanceUnit);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        if (UtilsMisc.isTablet(getActivity())) {
            mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }

        if (mListener != null) {
            mListener.refreshRegisterList(mListView);
        }
        attachWorkerFragment();

        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mWorkerFragment != null) {
                    mWorkerFragment.requestUpdate(null);
                    swipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        if (getActivity().getIntent().hasExtra(SearchManager.QUERY)) {
            //showSearch();
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Bundle bundle = new Bundle();

                    String keyword = getActivity().getIntent().getStringExtra(SearchManager.QUERY);
                    if (keyword.length() > 0) {
                        bundle.putString(SearchManager.QUERY, keyword);
                    }
                    bundle.putInt(Extra.CATEGORY, Extra.UNKNOWN);

                    bundle.putBoolean(Extra.ENABLE_BOUNDING_BOX, true);

                    if (getActivity() instanceof MainSinglePaneActivity) {
                        ((MainSinglePaneActivity) getActivity()).onSearchModeChange(true);
                    }

                    onSearch(bundle);
                }
            }, 1000);

        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: started " + hashCode());

        executeSavedInstanceState(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus = EventBus.getDefault();
        mBus.registerSticky(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mBus.unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mWorkerFragment.unregisterDisplayFragment(this);
    }

    private void attachWorkerFragment() {
        Fragment fragment = null;
        if (getArguments() == null
                || getArguments()
                .getBoolean(Extra.CREATE_WORKER_FRAGMENT, true)) {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fragment = fm.findFragmentByTag(POIsListWorkerFragment.TAG);
            Log.d(TAG, "Found worker fragment:" + fragment);
            if (fragment == null) {
                fragment = new POIsListWorkerFragment();
                fm.beginTransaction().add(fragment, POIsListWorkerFragment.TAG)
                        .commit();
            }

        } else if (!getArguments().getBoolean(Extra.CREATE_WORKER_FRAGMENT,
                false)) {
            Log.d(TAG, "Connecting to Combined Worker Fragment");
            FragmentManager fm = getFragmentManager();
            fragment = fm.findFragmentByTag(CombinedWorkerFragment.TAG);
        }

        mWorkerFragment = (WorkerFragment) fragment;
        mWorkerFragment.registerDisplayFragment(this);
        Log.d(TAG, "result mWorkerFragment = " + mWorkerFragment);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        mFirstVisiblePosition = mListView.getFirstVisiblePosition();
        outState.putInt(Extra.FIRST_VISIBLE_POSITION, mFirstVisiblePosition);
        super.onSaveInstanceState(outState);
    }

    private void executeSavedInstanceState(Bundle savedInstanceState) {
        mFirstStart = (savedInstanceState == null);
        if (mFirstStart) {
            return;
        }

        mFirstVisiblePosition = savedInstanceState.getInt(
                Extra.FIRST_VISIBLE_POSITION, 0);
    }

    @Override
    public void executeBundle(Bundle bundle) {
        Log.d(TAG, "executeBundle: fragment is visible = " + isVisible());
        if (bundle == null) {
            return;
        }

        if (bundle.getBoolean(Extra.REQUEST, false)) {
            mWorkerFragment.requestUpdate(null);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!UtilsMisc.isTablet(getActivity())) {
            inflater.inflate(R.menu.ab_list_fragment, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_search:
                showSearch();
                return true;
        }

        return false;
    }

    public void onRefreshStarted() {
        Log.d(TAG, "onRefreshStarted pulled");
        mFirstVisiblePosition = 0;
        if (mWorkerFragment != null) {
            mWorkerFragment.requestUpdate(null);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) parent.getAdapter().getItem(position);
        if (cursor == null) {
            return;
        }
        selectedItem_wmID = POIHelper.getWMId(cursor);
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);

        // save id from values here - - - - - - - -
        //idOfSelectedListItem = Long.parseLong((String) values.get("_id"));

        if (mListener != null) {
            mListener.onShowDetail(this, values);
        }
    }

    private void setCursor(Cursor cursor) {
        //UtilsMisc.dumpCursorCompare(TAG, mCursor, cursor);
        if (cursor == mCursor) {
            return;
        }

        mCursor = cursor;
        if (mCursor != null) {
            mDirectionCursorWrapper = new DirectionCursorWrapper(mCursor);
        } else {
            mDirectionCursorWrapper = null;
        }
        mAdapter.swapCursor(mDirectionCursorWrapper);
        //markItemClear();
        //refreshListPosition();
        if (selectedItem_wmID != null) {
            markItem(selectedItem_wmID);
        }
    }

    private void setRefreshStatus(boolean isRefreshing) {
        this.isRefreshing = isRefreshing;
        Log.d(TAG, "setRefreshStates: isRefreshing = " + isRefreshing);

        if (mListener != null) {
            mListener.onRefreshing(isRefreshing);
        }

        if (!isRefreshing) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void refreshListPosition() {
        if (mFirstVisiblePosition != 0) {
            if (mFirstVisiblePosition >= mAdapter.getCount()) {
                mFirstVisiblePosition = mAdapter.getCount();
            }
            mListView.setSelection(mFirstVisiblePosition);
        }
    }

    private void showSearch() {
        FragmentManager fm = getFragmentManager();
        SearchDialogFragment searchDialog = SearchDialogFragment.newInstance(
                true, false);

        searchDialog.setTargetFragment(this, 0);
        searchDialog.show(fm, getTag());
    }

    @Override
    public void onSearch(Bundle bundle) {
        mWorkerFragment.requestSearch(bundle);
    }

    @Override
    public void onUpdate(WorkerFragment fragment) {
        Log.d(TAG, "onUpdate");
        setCursor(fragment.getCursor(WorkerFragment.LIST_CURSOR));
        setRefreshStatus(fragment.isRefreshing());
        setRefreshEnabled(fragment.isSearchMode());
        if (selectedItem_wmID != null) {
            markItem(selectedItem_wmID);
        }

        // set selected list item here - - - - - - - -
        //if(idOfSelectedListItem != 0)
        //markItem(idOfSelectedListItem);
    }

    private void setRefreshEnabled(boolean refreshDisabled) {
        if (mListener != null) {
            mListener.onRefreshEnabled(!refreshDisabled);
        }

    }

    public void onEventMainThread(DistanceUnitChangedEvent e) {
        Log.d(TAG, "onDistanceUnitChanged");
        mUseAngloDistanceUnit = e.useAngloDistanceUnit;
        if (mAdapter != null) {
            mAdapter.changeAdapter(e.useAngloDistanceUnit);
        }
    }

    public void markItem(String wmID) {
        boolean marked = false;
        int pos;
        for (pos = 0; pos < mAdapter.getCount(); pos++) {
            Cursor c = (Cursor) mAdapter.getItem(pos);
            if (POIHelper.getWMId(c).equals(wmID)) {
                mCheckedItem = pos;
                mListView.setItemChecked(
                        mCheckedItem, true);
                marked = true;
                break;
            }
        }

        if (!marked) {
            Log.d(TAG, "[markItem] deselect all items");
            mListView.clearChoices();
            mListView.requestLayout();
        } else {
            Log.d(TAG, "[markItem] Mark item: " + wmID);
            mListView.setSelection(mListView.getCheckedItemPosition());
            mListView.requestLayout();
        }
    }

    @Override
    public void markItem(ContentValues values, boolean centerToItem) {
        Long id = values.getAsLong(POIs.POI_ID);
        String wmID = values.getAsString(POIs.WM_ID);
        selectedItem_wmID = wmID;
        markItem(selectedItem_wmID);
    }

    public void markItemClear() {
        mListView.setItemChecked(mCheckedItem, false);
    }

}
