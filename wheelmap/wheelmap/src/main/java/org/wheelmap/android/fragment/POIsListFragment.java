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

import org.wheelmap.android.adapter.POIsListCursorAdapter;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.fragment.SearchDialogFragment.OnSearchDialogListener;
import org.wheelmap.android.manager.SupportManager.DistanceUnitChangedEvent;
import org.wheelmap.android.model.DirectionCursorWrapper;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.UtilsMisc;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import de.akquinet.android.androlog.Log;

public class POIsListFragment extends SherlockListFragment implements
		DisplayFragment, OnSearchDialogListener, OnRefreshListener<ListView>,
		OnExecuteBundle {
	public static final String TAG = POIsListFragment.class.getSimpleName();
	private WorkerFragment mWorkerFragment;
	private PullToRefreshListView mPullToRefreshListView;
	private int mFirstVisiblePosition = 0;
	private int mCheckedItem;
	private boolean mFirstStart;
	private boolean mRefreshDisabled;

	private DisplayFragmentListener mListener;
	private POIsListCursorAdapter mAdapter;
	private Cursor mCursor;
	private Bus mBus;

	private SensorManager mSensorManager;
	private boolean mOrientationAvailable;
	private Sensor mSensor;
	private DirectionCursorWrapper mDirectionCursorWrapper;
	private static final float MIN_DIRECTION_DELTA = 3;
	private float mLastDirection;

	public static POIsListFragment newInstance(boolean createWorker,
			boolean disableSearch) {
		POIsListFragment f = new POIsListFragment();
		Bundle b = new Bundle();
		b.putBoolean(Extra.CREATE_WORKER_FRAGMENT, createWorker);
		b.putBoolean(Extra.DISABLE_SEARCH, disableSearch);

		f.setArguments(b);
		return f;
	}

	public POIsListFragment() {
		super();
		Log.d(TAG, "constructor called " + hashCode());
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof DisplayFragmentListener)
			mListener = (DisplayFragmentListener) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate " + hashCode());
		setHasOptionsMenu(true);

		mSensorManager = (SensorManager) getActivity().getSystemService(
				Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		mOrientationAvailable = mSensor != null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView " + hashCode());

		View v = (LinearLayout) inflater.inflate(R.layout.fragment_list,
				container, false);
		mPullToRefreshListView = (PullToRefreshListView) v
				.findViewById(R.id.pull_to_refresh_listview);
		mPullToRefreshListView.setOnRefreshListener(this);
		mAdapter = new POIsListCursorAdapter(getActivity(), null, false);
		mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);

		mBus = WheelmapApp.getBus();
		mBus.register(this);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated: started " + hashCode());

		executeSavedInstanceState(savedInstanceState);

		Fragment fragment = null;
		if (getArguments() == null
				|| getArguments()
						.getBoolean(Extra.CREATE_WORKER_FRAGMENT, true)) {
			FragmentManager fm = getFragmentManager();
			fragment = fm.findFragmentByTag(POIsListWorkerFragment.TAG);
			Log.d(TAG, "Looking for Worker Fragment:" + fragment);
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

		if (UtilsMisc.isTablet(getActivity()))
			mPullToRefreshListView.getRefreshableView().setChoiceMode(
					ListView.CHOICE_MODE_SINGLE);
	}

	private void executeSavedInstanceState(Bundle savedInstanceState) {
		mFirstStart = (savedInstanceState == null);
		if (mFirstStart)
			return;

		mFirstVisiblePosition = savedInstanceState.getInt(
				Extra.FIRST_VISIBLE_POSITION, 0);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mOrientationAvailable)
			mSensorManager.registerListener(mSensorEventListener, mSensor,
					SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOrientationAvailable)
			mSensorManager.unregisterListener(mSensorEventListener);
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mWorkerFragment.unregisterDisplayFragment(this);
		mBus.unregister(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy " + hashCode());
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.d(TAG, "onDetach " + hashCode());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		mFirstVisiblePosition = mPullToRefreshListView.getRefreshableView()
				.getFirstVisiblePosition();
		outState.putInt(Extra.FIRST_VISIBLE_POSITION, mFirstVisiblePosition);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void executeBundle(Bundle bundle) {

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.ab_list_fragment, menu);
		if (getArguments().containsKey(Extra.DISABLE_SEARCH))
			menu.removeItem(R.id.menu_search);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
		case R.id.menu_search:
			showSearch();
			return true;
		default:
			// noop
		}

		return false;
	}

	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		Log.d(TAG, "onRefresh pulled");
		mFirstVisiblePosition = 0;
		if (mWorkerFragment != null)
			mWorkerFragment.requestUpdate(null);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Cursor cursor = (Cursor) l.getAdapter().getItem(position);
		if (cursor == null)
			return;

		ContentValues values = new ContentValues();
		DatabaseUtils.cursorRowToContentValues(cursor, values);
		markItem(values, false);
		if (mListener != null)
			mListener.onShowDetail(this, values);

	}

	private void setCursor(Cursor cursor) {
		Log.d(TAG, "setCursor cursor "
				+ ((cursor != null) ? cursor.hashCode() : "null") + " count = "
				+ ((cursor != null) ? cursor.getCount() : "null")
				+ " isNewCursor = " + (cursor != mCursor));
		if (cursor == mCursor)
			return;

		mCursor = cursor;
		mDirectionCursorWrapper = new DirectionCursorWrapper(mCursor);
		mAdapter.swapCursor(mDirectionCursorWrapper);
		markItemClear();
		refreshListPosition();
	}

	private void setRefreshStatus(boolean isRefreshing) {
		if (isRefreshing)
			mPullToRefreshListView.setRefreshing();
		else
			mPullToRefreshListView.onRefreshComplete();

		if (mListener != null)
			mListener.onRefreshing(isRefreshing);
	}

	private void refreshListPosition() {
		if (mFirstVisiblePosition != 0) {
			if (mFirstVisiblePosition >= mAdapter.getCount())
				mFirstVisiblePosition = mAdapter.getCount();
			mPullToRefreshListView.getRefreshableView().setSelection(
					mFirstVisiblePosition);
		}
	}

	private void showSearch() {
		FragmentManager fm = getFragmentManager();
		SearchDialogFragment searchDialog = SearchDialogFragment.newInstance(
				true, false);

		searchDialog.setTargetFragment(this, 0);
		searchDialog.show(fm, SearchDialogFragment.TAG);
	}

	@Override
	public void onSearch(Bundle bundle) {
		mWorkerFragment.requestSearch(bundle);
	}

	@Override
	public void onUpdate(WorkerFragment fragment) {
		setCursor(fragment.getCursor(WorkerFragment.LIST_CURSOR));
		setRefreshStatus(fragment.isRefreshing());
		setRefreshEnabled(fragment.isSearchMode());
	}

	private void setRefreshEnabled(boolean refreshDisabled) {
		if (refreshDisabled == mRefreshDisabled)
			return;
		mRefreshDisabled = refreshDisabled;
		Mode mode;
		if (refreshDisabled)
			mode = Mode.DISABLED;
		else
			mode = Mode.PULL_DOWN_TO_REFRESH;

		mPullToRefreshListView.setMode(mode);
	}

	@Override
	public void setCurrentLocation(Location location) {

	}

	@Subscribe
	public void onDistanceUnitChanged(DistanceUnitChangedEvent e) {
		Log.d(TAG, "onDistanceUnitChanged");
		mAdapter.changeAdapter();
	}

	public void markItem(ContentValues values, boolean centerToItem) {
		Long id = values.getAsLong(POIs.POI_ID);

		int pos;
		for (pos = 0; pos < mAdapter.getCount(); pos++) {
			Cursor c = (Cursor) mAdapter.getItem(pos);
			if (POIHelper.getId(c) == id) {
				mCheckedItem = pos + 1;
				mPullToRefreshListView.getRefreshableView().setItemChecked(
						mCheckedItem, true);

				break;
			}
		}

		mPullToRefreshListView.getRefreshableView().setSelection(
				mPullToRefreshListView.getRefreshableView()
						.getCheckedItemPosition());
	}

	public void markItemClear() {
		mPullToRefreshListView.getRefreshableView().setItemChecked(
				mCheckedItem, false);
	}

	private SensorEventListener mSensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			float direction = event.values[0];
			if (direction > 180)
				direction -= 360;

			if (isAdded())
				direction += UtilsMisc.calcRotationOffset(getActivity()
						.getWindowManager().getDefaultDisplay());

			updateDirection(direction);
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};

	private void updateDirection(float direction) {
		// if (Math.abs(direction - mLastDirection) < MIN_DIRECTION_DELTA)
		// return;
		if (mDirectionCursorWrapper == null)
			return;

		mDirectionCursorWrapper.setDeviceDirection(direction);
		mLastDirection = direction;
		mAdapter.notifyDataSetChanged();
	}

}
