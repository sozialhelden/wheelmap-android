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

import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.POIsListCursorAdapter;
import org.wheelmap.android.online.R;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class POIsListFragment extends SherlockListFragment implements OnRefreshListener {
	public final static String TAG = "poislist";
	public final static String EXTRA_FIRST_VISIBLE_POSITION = "org.wheelmap.android.FIRST_VISIBLE_POSITION";
	public final static String EXTRA_CREATE_WORKER_FRAGMENT = "org.wheelmap.android.CREATE_WORKER_FRAGMENT";
	
	private POIsListWorkerFragment mWorkerFragment;
	private OnListFragmentListener mListener;

	private PullToRefreshListView mPullToRefreshListView;
	private int mFirstVisiblePosition = 0;
	private boolean mIsRecreated = false;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		if ( activity instanceof OnListFragmentListener )
			mListener = (OnListFragmentListener)activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d( TAG, "onCreate" );
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = (LinearLayout) inflater.inflate( R.layout.fragment_list,container, false );		
		mPullToRefreshListView = (PullToRefreshListView) v.findViewById(R.id.pull_to_refresh_listview);
		mPullToRefreshListView.setOnRefreshListener( this );
		
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if ( getArguments() == null || getArguments().getBoolean( EXTRA_CREATE_WORKER_FRAGMENT, true )) {
			FragmentManager fm = getFragmentManager();
			mWorkerFragment = (POIsListWorkerFragment) fm.findFragmentByTag( POIsListWorkerFragment.TAG );
			if ( mWorkerFragment == null ) {
				mWorkerFragment = new POIsListWorkerFragment();
				fm.beginTransaction().add( mWorkerFragment, POIsListWorkerFragment.TAG ).commit();
				mWorkerFragment.setTargetFragment( this, 0 );
			} else {
				mIsRecreated = true;
				mFirstVisiblePosition = savedInstanceState.getInt( EXTRA_FIRST_VISIBLE_POSITION, 0 );
			}
		
			Log.d(TAG,  "fragment is recreated - requesting persistent data");
			mWorkerFragment.setPersistentValues();
			getListView().setSelection(mFirstVisiblePosition);
		}

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
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		saveListPosition();
		outState.putInt(EXTRA_FIRST_VISIBLE_POSITION, mFirstVisiblePosition);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onRefresh() {
		if ( mWorkerFragment != null )
			mWorkerFragment.requestData();		
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		saveListPosition();
		Cursor cursor = (Cursor) l.getAdapter().getItem(position);
		if (cursor == null)
			return;

		long poiId = POIHelper.getId(cursor);
		mListener.onWheelmapPOIClicked( poiId );
		
	}
	
	public void setAdapter( POIsListCursorAdapter adapter ) {
		getListView().setAdapter( adapter );
	}
	
	private void saveListPosition() {
		mFirstVisiblePosition = getListView().getFirstVisiblePosition();
	}
	
	public void updateRefreshStatus( boolean status ) {
		if ( status )
			mPullToRefreshListView.setRefreshing();
		else
			mPullToRefreshListView.onRefreshComplete();
	}
	
	public interface OnListFragmentListener {
		public void onWheelmapPOIClicked( long id );
	}
}
