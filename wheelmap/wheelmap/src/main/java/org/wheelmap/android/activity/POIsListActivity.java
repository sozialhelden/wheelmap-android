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
import org.wheelmap.android.fragment.POIsListWorkerFragment;
import org.wheelmap.android.fragment.POIsListFragment.OnListFragmentListener;
import org.wheelmap.android.fragment.POIsListWorkerFragment.OnListWorkerFragmentListener;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.ui.InfoActivity;
import org.wheelmap.android.ui.POIDetailActivity;
import org.wheelmap.android.ui.POIDetailActivityEditable;
import org.wheelmap.android.ui.SearchActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class POIsListActivity extends SherlockFragmentActivity implements
		OnListFragmentListener, OnListWorkerFragmentListener, OnClickListener {
	private final static String TAG = "poislist";
	private boolean isInForeground;
	private boolean isShowingDialog;

	GoogleAnalyticsTracker tracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.activity_list_fragments);

		TextView mapButton = (TextView) findViewById(R.id.switch_maps);
		mapButton.setOnClickListener(this);

		// GA
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession("UA-25843648-1", 20, this);
		tracker.setAnonymizeIp(true);
		tracker.trackPageView("/ListActivity");

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		isInForeground = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		isInForeground = false;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		tracker.stopSession();

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d(TAG, "onNewIntent");
		if (intent.getExtras() != null) {
			POIsListWorkerFragment fragment = (POIsListWorkerFragment) getSupportFragmentManager()
					.findFragmentByTag(POIsListWorkerFragment.TAG);
			fragment.executeSearch(intent.getExtras());

		}
	}

	public void onInfoClick(View v) {
		Intent intent = new Intent(this, InfoActivity.class);
		startActivity(intent);
	}

	public void onNewPOIClick(View v) {

		long poiId = 0; // = list worker fragment creates one
		Intent i = new Intent(POIsListActivity.this,
				POIDetailActivityEditable.class);
		i.putExtra(Wheelmap.POIs.EXTRAS_POI_ID, poiId);
		startActivity(i);
	}

	public void onSearchClick(View v) {
		final Intent intent = new Intent(POIsListActivity.this,
				SearchActivity.class);
		intent.putExtra(SearchActivity.EXTRA_SHOW_DISTANCE, true);
		startActivityForResult(intent, SearchActivity.PERFORM_SEARCH);
	}

	// @Override
	// public boolean onPrepareOptionsMenu(Menu menu) {
	// startActivity(new Intent(this, NewSettingsActivity.class));
	// return super.onPrepareOptionsMenu(menu);
	// }

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
		Log.d(TAG, "onActivityResult: requestCode = " + requestCode
				+ " resultCode = " + resultCode + " data = " + data);
		if (requestCode == SearchActivity.PERFORM_SEARCH) {
			// This is a standard resultCode that is sent back if the
			// activity doesn't supply an explicit result. It will also
			// be returned if the activity failed to launch.
			if (resultCode == RESULT_OK) {
				if (data != null && data.getExtras() != null) {
					POIsListWorkerFragment fragment = (POIsListWorkerFragment) getSupportFragmentManager()
							.findFragmentByTag(POIsListWorkerFragment.TAG);
					fragment.executeSearch(data.getExtras());
				}
			}
		}
	}

	@Override
	public void onWheelmapPOIClicked(long id) {

		Intent i = new Intent(POIsListActivity.this, POIDetailActivity.class);
		i.putExtra(Wheelmap.POIs.EXTRAS_POI_ID, id);
		startActivity(i);
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
	public void onUpdateRefresh(boolean refresh) {

	}

	@Override
	public void onError(SyncServiceException e) {
		showErrorDialog(e);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		if (id == R.id.switch_maps) {

			Intent intent = new Intent(POIsListActivity.this,
					POIsMapsforgeActivity.class);
			// intent.putExtra(POIsMapsforgeActivity.EXTRA_NO_RETRIEVAL, false);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			overridePendingTransition(0, 0);
			tracker.trackEvent("Clicks", // Category
					"Button", // Action
					"SwitchMaps", // Label
					0); // Value

		}
	}

}
