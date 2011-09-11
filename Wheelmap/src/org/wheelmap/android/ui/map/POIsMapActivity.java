package org.wheelmap.android.ui.map;

import java.util.List;

import org.wheelmap.android.R;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.service.SyncService;

import org.wheelmap.android.ui.WheelmapHomeActivity;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.ParceableBoundingBox;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

public class POIsMapActivity extends MapActivity  implements DetachableResultReceiver.Receiver {

	/** State held between configuration changes. */
	private State mState;

	private Cursor mCursor;

	private MapController mapController;
	private MapView mapView;
	private LocationManager locationManager;
	private POIsPaintedOverlay poisItemizedOverlay;
	private  MyLocationOverlay mCurrLocationOverlay;
	List<Overlay> mapOverlays;

	private GeoPoint mLastGeoPointE6;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);		
		mapView=(MapView)findViewById(R.id.map);

		mapView.setBuiltInZoomControls(true);
		mapView.setStreetView(true);
		mapController = mapView.getController();
		mapController.setZoom(14); // Zoon 1 is world view
		
		// Run query
		Uri uri = Wheelmap.POIs.CONTENT_URI;

		mCursor = getContentResolver().query(uri, Wheelmap.POIs.PROJECTION, null, null, Wheelmap.POIs.DEFAULT_SORT_ORDER);

		// overlays
		mapOverlays = mapView.getOverlays(); 
		poisItemizedOverlay = new POIsPaintedOverlay(this, mCursor);
		mapOverlays.add(poisItemizedOverlay);

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
		}
		
		findViewById(R.id.btn_title_gps).setVisibility(View.GONE);
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
			mapView.invalidate();
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
	
	@Override
	protected void onPause() {
		mCurrLocationOverlay.disableMyLocation();
		mCursor.deactivate();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mCursor.requery();
		mCurrLocationOverlay.enableMyLocation();
		super.onResume();
	}            

	private void updateRefreshStatus() {
		findViewById(R.id.btn_title_refresh).setVisibility(
				mState.mSyncing ? View.GONE : View.VISIBLE);
		findViewById(R.id.title_refresh_progress).setVisibility(
				mState.mSyncing ? View.VISIBLE : View.GONE);
	}

	public void  onCenterOnCurrentLocationClick(View v) {
		if (mLastGeoPointE6 != null) {
			mapController.animateTo(mLastGeoPointE6); 
			mapController.setCenter(mLastGeoPointE6);
		}
	}	

	public void onHomeClick(View v) {
		final Intent intent = new Intent(this, WheelmapHomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(intent);
	}	

	private void fillExtrasWithBoundingRect(Bundle bundle) {
		int latSpan = mapView.getLatitudeSpan();
		int lonSpan = mapView.getLongitudeSpan();
		GeoPoint center = mapView.getMapCenter();
		ParceableBoundingBox boundingBox =  new ParceableBoundingBox(
				center.getLatitudeE6() + (latSpan / 2), 
				center.getLongitudeE6() + (lonSpan / 2),
				center.getLatitudeE6() - (latSpan / 2),
				center.getLongitudeE6() - (lonSpan / 2));
		bundle.putSerializable(SyncService.EXTRA_BOUNDING_BOX, boundingBox);

	}

	public void onRefreshClick(View v) {

		// get bounding box from current view
		Bundle extras = new Bundle();
		// 
		fillExtrasWithBoundingRect(extras);

		// trigger off background sync
		final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, SyncService.class);
		intent.putExtras(extras);
		intent.putExtra(SyncService.EXTRA_WHAT, SyncService.WHAT_RETRIEVE_NODES );
		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mState.mReceiver);
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
			// we got the first time current position so center map on it
			if (mLastGeoPointE6 == null) {
				findViewById(R.id.btn_title_gps).setVisibility(View.VISIBLE);
				mLastGeoPointE6 = new GeoPoint(lat, lng);
				mapController.animateTo(mLastGeoPointE6); 
				mapController.setCenter(mLastGeoPointE6);
			}
			else 
				mLastGeoPointE6 = new GeoPoint(lat, lng);			
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
