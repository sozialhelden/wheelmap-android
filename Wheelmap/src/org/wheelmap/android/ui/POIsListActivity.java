package org.wheelmap.android.ui;

import org.wheelmap.android.R;
import org.wheelmap.android.model.POIsCursorAdapter;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.ui.map.POIsMapActivity;
import org.wheelmap.android.utils.DetachableResultReceiver;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class POIsListActivity extends ListActivity implements DetachableResultReceiver.Receiver {

	private Cursor mCursor;
	private State mState;
	


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		// Run query
		Uri uri = Wheelmap.POIs.CONTENT_URI;

		mCursor = managedQuery(uri, Wheelmap.POIs.PROJECTION, null, null, Wheelmap.POIs.DEFAULT_SORT_ORDER);
		startManagingCursor(mCursor);

		POIsCursorAdapter adapter = new POIsCursorAdapter(this, mCursor);
		setListAdapter(adapter);

		getListView().setTextFilterEnabled(true);

		mState = (State) getLastNonConfigurationInstance();
		final boolean previousState = mState != null;

		if (previousState) {
			// Start listening for SyncService updates again
			mState.mReceiver.setReceiver(this);
			updateRefreshStatus();
		} else {
			mState = new State();
			mState.mReceiver.setReceiver(this);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Clear any strong references to this Activity, we'll reattach to
		// handle events on the other side.
		mState.mReceiver.clearReceiver();
		return mState;
	}

	public void onHomeClick(View v) {
		final Intent intent = new Intent(this, WheelmapHomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(intent);
	}

	public void onMapClick(View v) {
		startActivity(new Intent(this, POIsMapActivity.class));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Intent i=new Intent(POIsListActivity.this, POIDetailActivity.class);

		i.putExtra(Wheelmap.POIs.EXTRAS_POI_ID, String.valueOf(id));
		startActivity(i);
	}

	private void updateRefreshStatus() {
		findViewById(R.id.btn_title_refresh).setVisibility(
				mState.mSyncing ? View.GONE : View.VISIBLE);
		findViewById(R.id.title_refresh_progress).setVisibility(
				mState.mSyncing ? View.VISIBLE : View.GONE);
	}	


	/** {@inheritDoc} */
	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch (resultCode) {
		case SyncService.STATUS_RUNNING: {
			mState.mSyncing = true;
			updateRefreshStatus();
			break;
		}
		case SyncService.STATUS_FINISHED: {
			mState.mSyncing = false;
			updateRefreshStatus();
			//FillPOIsOverlay();
			break;
		}
		case SyncService.STATUS_ERROR: {
			// Error happened down in SyncService, show as toast.
			mState.mSyncing = false;
			updateRefreshStatus();
			final String errorText = getString(R.string.toast_sync_error, resultData
					.getString(Intent.EXTRA_TEXT));
			Toast.makeText(POIsListActivity.this, errorText, Toast.LENGTH_LONG).show();
			break;
		}
		}
	}

	/**
	 * State specific to {@link HomeActivity} that is held between configuration
	 * changes. Any strong {@link Activity} references <strong>must</strong> be
	 * cleared before {@link #onRetainNonConfigurationInstance()}, and this
	 * class should remain {@code static class}.
	 */
	private static class State {
		public DetachableResultReceiver mReceiver;
		public boolean mSyncing = false;

		private State() {
			mReceiver = new DetachableResultReceiver(new Handler());
		}
	}
	
	public void onRefreshClick(View v) {
		// start service for sync
		final Intent intent = new Intent(Intent.ACTION_SYNC, null, POIsListActivity.this, SyncService.class);
		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, POIsListActivity.this.mState.mReceiver);
		startService(intent);
	}

	public void onSearchClick(View v) {
		Toast.makeText(this, "Searching..", Toast.LENGTH_SHORT).show();
	}

}
