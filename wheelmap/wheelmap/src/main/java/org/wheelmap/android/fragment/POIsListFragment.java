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
import org.wheelmap.android.fragment.SearchDialogFragment.OnSearchDialogListener;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.POIsListCursorAdapter;
import org.wheelmap.android.online.R;

import android.app.Activity;
import android.database.Cursor;
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
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import de.akquinet.android.androlog.Log;

public class POIsListFragment extends SherlockListFragment implements
		DisplayFragment, OnSearchDialogListener, OnRefreshListener,
		OnExecuteBundle {
	public static final String TAG = POIsListFragment.class.getSimpleName();
	public final static String EXTRA_FIRST_VISIBLE_POSITION = "org.wheelmap.android.FIRST_VISIBLE_POSITION";
	public final static String EXTRA_CREATE_WORKER_FRAGMENT = "org.wheelmap.android.CREATE_WORKER_FRAGMENT";

	private WorkerFragment mWorkerFragment;

	private PullToRefreshListView mPullToRefreshListView;
	private int mFirstVisiblePosition = 0;

	private OnPOIsListListener mListener;
	private POIsListCursorAdapter mAdapter;
	private Cursor mCursor;

	public interface OnPOIsListListener {
		public void onShowDetail(long id);

		public void onRefreshing(boolean isRefreshing);
	}

	public POIsListFragment() {
		super();
		Log.d(TAG, "constructor called " + hashCode());
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof OnPOIsListListener)
			mListener = (OnPOIsListListener) activity;
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

		View v = (LinearLayout) inflater.inflate(R.layout.fragment_list,
				container, false);
		mPullToRefreshListView = (PullToRefreshListView) v
				.findViewById(R.id.pull_to_refresh_listview);
		mPullToRefreshListView.setOnRefreshListener(this);
		mAdapter = new POIsListCursorAdapter(getActivity(), null, false);
		mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated: started " + hashCode());

		if (savedInstanceState != null)
			executeSavedInstanceState(savedInstanceState);

		if (getArguments() == null
				|| getArguments()
						.getBoolean(EXTRA_CREATE_WORKER_FRAGMENT, true)) {
			Log.d(TAG, "onActivityCreated: checking workerfragment");
			FragmentManager fm = getFragmentManager();
			Fragment fragment = (POIsListWorkerFragment) fm
					.findFragmentByTag(POIsListWorkerFragment.TAG);
			if (fragment == null) {
				fragment = new POIsListWorkerFragment();
				fm.beginTransaction().add(fragment, POIsListWorkerFragment.TAG)
						.commit();
				fragment.setTargetFragment(this, 0);
			}

			mWorkerFragment = (WorkerFragment) fragment;
			mWorkerFragment.registerDisplayFragment(this);
		}

	}

	private void executeSavedInstanceState(Bundle savedInstanceState) {
		mFirstVisiblePosition = savedInstanceState.getInt(
				EXTRA_FIRST_VISIBLE_POSITION, 0);
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
	public void onDestroyView() {
		super.onDestroyView();
		mWorkerFragment.unregisterDisplayFragment(this);
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
		outState.putInt(EXTRA_FIRST_VISIBLE_POSITION, mFirstVisiblePosition);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void executeBundle(Bundle bundle) {

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.ab_list_fragment, menu);
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
	public void onRefresh() {
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

		long poiId = POIHelper.getId(cursor);
		if (mListener != null)
			mListener.onShowDetail(poiId);

	}

	private void setCursor(Cursor cursor) {
		Log.d(TAG, "setCursor cursor "
				+ ((cursor != null) ? cursor.hashCode() : "null") + " count = "
				+ ((cursor != null) ? cursor.getCount() : "null")
				+ " isNewCursor = " + (cursor != mCursor));
		if (cursor == mCursor)
			return;

		mCursor = cursor;
		mAdapter.swapCursor(cursor);
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
		FragmentManager fm = getActivity().getSupportFragmentManager();
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
		setCursor(fragment.getCursor());
		setRefreshStatus(fragment.isRefreshing());
	}

	@Override
	public void setCurrentLocation(GeoPoint point, Location location) {

	}

}
