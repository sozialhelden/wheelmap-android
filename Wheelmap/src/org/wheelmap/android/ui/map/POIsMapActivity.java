package org.wheelmap.android.ui.map;

import java.util.List;

import org.wheelmap.android.R;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.service.SyncService;

import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.ParceableBoundingBox;

import wheelmap.org.WheelchairState;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class POIsMapActivity extends MapActivity  implements DetachableResultReceiver.Receiver {

	/** State held between configuration changes. */
	private State mState;

	private Cursor mCursor;

	private MapController mapController;
	private MapView mapView;
	private LocationManager locationManager;
	private POIsItemizedOverlay poisItemizedOverlay;
	private  MyLocationOverlay mCurrLocationOverlay;
	List<Overlay> mapOverlays;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);		
		mapView=(MapView)findViewById(R.id.map);

		mapView.setBuiltInZoomControls(true);
		mapView.setStreetView(true);
		mapController = mapView.getController();
		mapController.setZoom(14); // Zoon 1 is world view

		// overlays
		mapOverlays = mapView.getOverlays(); 
		Drawable drawable = this.getResources().getDrawable(R.drawable.marker_red);
		poisItemizedOverlay = new POIsItemizedOverlay(drawable);

		mCurrLocationOverlay = new MyLocationOverlay(this, mapView);
		mCurrLocationOverlay.enableMyLocation();
		mapOverlays.add(mCurrLocationOverlay);

		// location manager		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, new GeoUpdateHandler());

		mState = (State) getLastNonConfigurationInstance();
		final boolean previousState = mState != null;

		if (previousState) {
			// Start listening for SyncService updates again
			mState.mReceiver.setReceiver(this);
			updateRefreshStatus();

		} else {
			mState = new State();
			mState.mReceiver.setReceiver(this);
			onRefreshClick(null);
		}

		FillPOIsOverlay();
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
			FillPOIsOverlay();
			break;
		}
		case SyncService.STATUS_ERROR: {
			// Error happened down in SyncService, show as toast.
			mState.mSyncing = false;
			updateRefreshStatus();
			final String errorText = getString(R.string.toast_sync_error, resultData
					.getString(Intent.EXTRA_TEXT));
			Toast.makeText(POIsMapActivity.this, errorText, Toast.LENGTH_LONG).show();
			break;
		}
		}
	}


	private void FillPOIsOverlay() {
		// Run query
		Uri uri = Wheelmap.POIs.CONTENT_URI;

		mCursor = managedQuery(uri, Wheelmap.POIs.PROJECTION, null, null, Wheelmap.POIs.DEFAULT_SORT_ORDER);
		startManagingCursor(mCursor);

		if (mCursor.moveToFirst()) {
			// insert overlay into the map with the first data
			mapOverlays.add(poisItemizedOverlay);


			// current location overlay
			GeoPoint point;
			String name; 
			int lat;
			int lon; 

			int nameColumn = mCursor.getColumnIndex(Wheelmap.POIs.NAME); 
			int latColumn = mCursor.getColumnIndex(Wheelmap.POIs.COORD_LAT);
			int lonColumn = mCursor.getColumnIndex(Wheelmap.POIs.COORD_LON);

			do {
				// Get the field values
				name = mCursor.getString(nameColumn);
				lat = mCursor.getInt(latColumn);
				lon = mCursor.getInt(lonColumn);

				point = new GeoPoint(lat, lon); 

				poisItemizedOverlay.addOverlay(new OverlayItem(point, name, ""));
			} while (mCursor.moveToNext());

		}
	}  

	@Override
	protected void onPause() {
		mCurrLocationOverlay.disableMyLocation();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mCurrLocationOverlay.enableMyLocation();
		super.onResume();
	}            

	private void updateRefreshStatus() {
		findViewById(R.id.btn_title_refresh).setVisibility(
				mState.mSyncing ? View.GONE : View.VISIBLE);
		findViewById(R.id.title_refresh_progress).setVisibility(
				mState.mSyncing ? View.VISIBLE : View.GONE);
	}




	public void onHomeClick(View v) {
		final Intent intent = new Intent(this, POIsMapActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(intent);
	}

	private void fillExtrasWithBoundingRect(Bundle bundle) {
		int latSpan = mapView.getLatitudeSpan();
		int lonSpan = mapView.getLongitudeSpan();
		GeoPoint center = mapView.getMapCenter();
		ParceableBoundingBox bouncingBox =  new ParceableBoundingBox(
				center.getLatitudeE6() + (latSpan / 2), 
				center.getLongitudeE6() + (lonSpan / 2),
				center.getLatitudeE6() - (latSpan / 2),
				center.getLongitudeE6() - (lonSpan / 2));
		bundle.putSerializable(SyncService.EXTRA_STATUS_RECEIVER_BOUNCING_BOX, bouncingBox);

	}
	
	private void fillExtrasWithOtherParameters( Bundle bundle ) {
		bundle.putInt( SyncService.EXTRA_STATUS_RECEIVER_WHEELMAP_STATUS, WheelchairState.UNKNOWN.getId() );
	}


	public void onRefreshClick(View v) {

		// get bounding box from current view
		Bundle extras = new Bundle();
		// 
		fillExtrasWithBoundingRect(extras);
		fillExtrasWithOtherParameters(extras);



		// trigger off background sync
		final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, SyncService.class);
		intent.putExtras(extras);
		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mState.mReceiver);




		// insert here bounding rectangle as data

		startService(intent);
	}

	public void onSearchClick(View v) {
		Toast.makeText(this, "Searching..", Toast.LENGTH_SHORT).show();
	}


	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
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

	private class GeoUpdateHandler implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			int lat = (int) (location.getLatitude() * 1E6);
			int lng = (int) (location.getLongitude() * 1E6);
			GeoPoint point = new GeoPoint(lat, lng);
			mapController.animateTo(point); //	mapController.setCenter(point);			
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}
}
