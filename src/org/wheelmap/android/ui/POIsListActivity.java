package org.wheelmap.android.ui;

import org.wheelmap.android.R;
import org.wheelmap.android.model.POIsCursorAdapter;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.utils.CurrentLocation;
import org.wheelmap.android.utils.CurrentLocation.LocationResult;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.GeocoordinatesMath;
import org.wheelmap.android.utils.ParceableBoundingBox;

import wheelmap.org.BoundingBox;
import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;
import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;


public class POIsListActivity extends ListActivity implements DetachableResultReceiver.Receiver {

	private Cursor mCursor;
	private CurrentLocation mCurrentLocation;
	private State mState;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		
		// current location
		mCurrentLocation = new CurrentLocation();
		
		// Run query
		Uri uri = Wheelmap.POIs.CONTENT_URI;

		mCursor = managedQuery(uri, Wheelmap.POIs.PROJECTION, null, null, Wheelmap.POIs.DEFAULT_SORT_ORDER);
		startManagingCursor(mCursor);

		POIsCursorAdapter adapter = new POIsCursorAdapter(this, mCursor);
		setListAdapter(adapter);

		getListView().setTextFilterEnabled(true);
	}
	
	public State getState() {
		return mState;
	}
	
	public void onHomeClick(View v) {
		 final Intent intent = new Intent(this, WheelmapHomeActivity.class);
	        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        this.startActivity(intent);
   }	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Uri uri = ContentUris.withAppendedId(POIs.CONTENT_URI, id);

		String action = getIntent().getAction();
		if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
			// The caller is waiting for us to return a note selected by
			// the user.  The have clicked on one, so return it now.
			setResult(RESULT_OK, new Intent().setData(uri));
		} else {
			// Launch activity to view/edit the currently selected item
			startActivity(new Intent(Intent.ACTION_EDIT, uri));
		}
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
//			FillPOIsOverlay();
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
	
	public LocationResult locationResult = new LocationResult(){
	    @Override
	    public void gotLocation(final Location location){
	     	int latE6 = (int)(location.getLatitude() * 1E6);
			int lonE6 = (int)(location.getLongitude() * 1E6);
			
			// calculate bounding box from current location around 2 km
			BoundingBox bb = GeocoordinatesMath.calculateBoundingBox(new Wgs84GeoCoordinates(lonE6, latE6), 2);
			
			ParceableBoundingBox boundingBox = new ParceableBoundingBox(bb);
			
			// get bounding box from current view
			Bundle extras = new Bundle();
			// trigger off background sync
			final Intent intent = new Intent(Intent.ACTION_SYNC, null, POIsListActivity.this, SyncService.class);
			extras.putSerializable(SyncService.EXTRA_STATUS_RECEIVER_BOUNCING_BOX, boundingBox);
			intent.putExtras(extras);
			intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, POIsListActivity.this.getState().mReceiver);
			startService(intent);
	    }
	};

	public void onRefreshClick(View v) {
		mCurrentLocation.getLocation(this, locationResult);
	}

	public void onSearchClick(View v) {
		Toast.makeText(this, "Searching..", Toast.LENGTH_SHORT).show();
	}

}
