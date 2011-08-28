package org.wheelmap.android.ui;

import org.wheelmap.android.R;
import org.wheelmap.android.model.POIsCursorWrapper;
import org.wheelmap.android.model.POIsListCursorAdapter;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.ui.map.POIsMapActivity;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.GeocoordinatesMath;
import org.wheelmap.android.utils.GeocoordinatesMath.DistanceUnit;

import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class POIsListActivity extends ListActivity implements
		DetachableResultReceiver.Receiver, OnItemSelectedListener {

	private final static String TAG = "poislist";
	private final static String PREF_KEY_LIST_DISTANCE = "listDistance";

	private State mState;
	private float mDistance;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		
		// Run query
		Uri uri = Wheelmap.POIs.CONTENT_URI_POI_SORTED;

		Cursor cursor = managedQuery(uri, Wheelmap.POIs.PROJECTION, null, null,
				Wheelmap.POIs.DEFAULT_SORT_ORDER);
		Cursor wrappingCursor = createCursorWrapper( cursor );
		startManagingCursor(wrappingCursor);

		POIsListCursorAdapter adapter = new POIsListCursorAdapter( this, wrappingCursor );
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
		
		TextView spinnerDesc = (TextView) findViewById(R.id.spinner_description_text);
		int textRes = GeocoordinatesMath.DISTANCE_UNIT == DistanceUnit.KILOMETRES ? R.string.spinner_description_distance_km
				: R.string.spinner_description_distance_miles;
		spinnerDesc.setText(textRes);

		Spinner spinner = (Spinner) findViewById(R.id.spinner_distance);
		ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter
				.createFromResource(this, R.array.distance_array_values,
						android.R.layout.simple_spinner_item);
		spinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerAdapter);
		spinner.setOnItemSelectedListener(this);
		spinner.setPromptId( textRes );
		spinner.setSelection( getSelectionFromPreferences() );
		
	}
	
	public Cursor createCursorWrapper( Cursor cursor ) {
		LocationManager lm = (LocationManager)getApplicationContext().getSystemService( LOCATION_SERVICE );
		Location loc = lm.getLastKnownLocation( LocationManager.GPS_PROVIDER );
		if ( loc == null )
			loc = lm.getLastKnownLocation( LocationManager.NETWORK_PROVIDER );
		if ( loc == null ) {
			loc = new Location(LocationManager.GPS_PROVIDER);
            loc.setLatitude(52.5);
            loc.setLongitude(13.4);
		}
		
		Wgs84GeoCoordinates location = new Wgs84GeoCoordinates(loc.getLongitude(), loc.getLatitude());
		
		return new POIsCursorWrapper(cursor, location);
	}
	
	public int getSelectionFromPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		
		String prefDist = prefs.getString( PREF_KEY_LIST_DISTANCE, "1");
		mDistance = Float.valueOf( prefDist );
		
		String[] values = getResources().getStringArray( R.array.distance_array_values );
		int i;
		for( i = 0; i < values.length; i++ ) {
			if ( Float.valueOf( values[i] ) == mDistance )
				return i;
		}
		return 0;	
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

		Intent i = new Intent(POIsListActivity.this, POIDetailActivity.class);

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
			break;
		}
		case SyncService.STATUS_ERROR: {
			// Error happened down in SyncService, show as toast.
			mState.mSyncing = false;
			updateRefreshStatus();
			final String errorText = getString(R.string.toast_sync_error,
					resultData.getString(Intent.EXTRA_TEXT));
			Toast.makeText(POIsListActivity.this, errorText, Toast.LENGTH_LONG)
					.show();
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
		final Intent intent = new Intent(Intent.ACTION_SYNC, null,
				POIsListActivity.this, SyncService.class);
		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mState.mReceiver);
		intent.putExtra(SyncService.EXTRA_STATUS_DISTANCE_LIMIT, mDistance);
		startService(intent);
	}

	public void onSearchClick(View v) {
		Toast.makeText(this, "Searching..", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view,
			int position, long id) {
		String distance = (String) adapterView.getItemAtPosition(position);
		mDistance = Float.valueOf( distance );
		onRefreshClick( view );
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}

}
