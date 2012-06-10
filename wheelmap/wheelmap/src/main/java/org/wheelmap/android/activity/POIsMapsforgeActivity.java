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
package org.wheelmap.android.activity;

import org.wheelmap.android.fragment.ErrorDialogFragment;
import org.wheelmap.android.fragment.POIsMapsforgeFragment;
import org.wheelmap.android.fragment.POIsMapsforgeWorkerFragment;
import org.wheelmap.android.fragment.POIsMapsforgeWorkerFragment.OnPOIsMapsforgeWorkerFragmentListener;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.ui.InfoActivity;
import org.wheelmap.android.ui.SearchActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class POIsMapsforgeActivity extends MapsforgeMapActivity implements
		OnClickListener, OnPOIsMapsforgeWorkerFragmentListener {
	private final static String TAG = "mapsforge";
	private ProgressBar mProgressBar;
	private ImageButton mSearchButton;

	private POIsMapsforgeFragment mFragment;
	private POIsMapsforgeWorkerFragment mWorkerFragment;
	private boolean isSearchMode;
	private boolean isShowingDialog;
	private boolean isInForeground;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "Activity onCreate");
		setContentView(R.layout.activity_mapsforge_fragments);
		mProgressBar = (ProgressBar) findViewById(R.id.progressbar_map);
		mSearchButton = (ImageButton) findViewById(R.id.btn_title_search);
		TextView listView = (TextView) findViewById(R.id.switch_list);
		listView.setOnClickListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		FragmentManager fm = getSupportFragmentManager();

		mFragment = (POIsMapsforgeFragment) fm
				.findFragmentById(R.id.map_fragment);
		mWorkerFragment = (POIsMapsforgeWorkerFragment) fm
				.findFragmentByTag(POIsMapsforgeWorkerFragment.TAG);

		Log.d(TAG, "Fragment: " + mFragment);
	}

	@Override
	public void onResume() {
		super.onResume();
		isInForeground = true;
		updateSearchStatus();
	}

	@Override
	public void onPause() {
		super.onPause();
		isInForeground = false;
	}

	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.switch_list) {
			Intent intent = new Intent(POIsMapsforgeActivity.this,
					org.wheelmap.android.activity.POIsListActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			overridePendingTransition(0, 0);
		}
	}

	public void onSearchClick(View v) {
		isSearchMode = !isSearchMode;
		updateSearchStatus();
		sendSearchStatus();
		
		if (isSearchMode) {
			final Intent intent = new Intent(POIsMapsforgeActivity.this,
					SearchActivity.class);
			intent.putExtra(SearchActivity.EXTRA_SHOW_MAP_HINT, true);
			startActivityForResult(intent, SearchActivity.PERFORM_SEARCH);
		}
	}

	// @Override
	// public boolean onPrepareOptionsMenu(Menu menu) {
	// startActivity(new Intent(this, NewSettingsActivity.class));
	// return super.onPrepareOptionsMenu(menu);
	// }

	// public void onListClick(View v) {
	// Intent intent = new Intent(this, POIsListActivity.class);
	// intent.putExtra(POIsListActivity.EXTRA_IS_RECREATED, false);
	// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
	// | Intent.FLAG_ACTIVITY_NO_ANIMATION);
	// startActivity(intent);
	// overridePendingTransition(0, 0);
	//
	// }

	public void onCenterClick(View v) {
		mFragment.navigateToLocation();
	}

	public void onInfoClick(View v) {
		Intent intent = new Intent(this, InfoActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onSearchRequested() {
		Bundle extras = new Bundle();
		startSearch(null, false, extras, false);
		return true;
	}

	@Override
	public void onRefreshStatusChange(boolean refreshStatus) {
		if (refreshStatus)
			mProgressBar.setVisibility(View.VISIBLE);
		else
			mProgressBar.setVisibility(View.GONE);
	}

	private void updateSearchStatus() {
		mSearchButton.setSelected(isSearchMode);
	}

	@Override
	public void onSearchModeChange(boolean isSearchMode) {
		Log.d(TAG, "activity isSearchMode = " + this.isSearchMode);
		this.isSearchMode = isSearchMode;
		updateSearchStatus();
	}

	private void sendSearchStatus() {
		if ( mWorkerFragment != null )
			mWorkerFragment.setSearchMode( isSearchMode );
	}

	/**
	 * This method is called when the sending activity has finished, with the
	 * result it supplied.
	 * 
	 * @param requestCode
	 *            The original request code as given to startActivity().
	 * @param resultCode
	 *            From sending activity as per setResult().
	 * @param data
	 *            From sending activity as per setResult().
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// You can use the requestCode to select between multiple child
		// activities you may have started. Here there is only one thing
		// we launch.
		if (requestCode == SearchActivity.PERFORM_SEARCH) {
			// This is a standard resultCode that is sent back if the
			// activity doesn't supply an explicit result. It will also
			// be returned if the activity failed to launch.
			if (resultCode == RESULT_OK) {
				if (data != null && data.getExtras() != null) {
					Bundle bundle = data.getExtras();
					mFragment.startSearch(bundle);
				}
			}
		}
	}

	private void showErrorDialog(SyncServiceException e) {		
		if (!isInForeground)
			return;
		
		FragmentManager fm = getSupportFragmentManager();
		ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance( e );
		if ( errorDialog == null )
			return;
		
		errorDialog.show( fm, ErrorDialogFragment.TAG );
	
	}
	
	@Override
	public void onError(SyncServiceException e) {
		showErrorDialog(e);
	}

}
