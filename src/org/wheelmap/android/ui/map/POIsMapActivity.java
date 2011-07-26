package org.wheelmap.android.ui.map;

import java.util.List;

import org.wheelmap.android.R;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.ui.WheelmapHomeActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class POIsMapActivity extends MapActivity {

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

		FillPOIsOverlay();
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
	public boolean onCreateOptionsMenu(Menu menu) {
			new MenuInflater(this).inflate(R.menu.menu, menu);

			return(super.onCreateOptionsMenu(menu));
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// insert new POI in Sobrance to test
		

		ContentValues values = new ContentValues();

		
		values.put(Wheelmap.POIs.NAME, "Lidl");
		values.put(Wheelmap.POIs.COORD_LAT, 48745111);
		values.put(Wheelmap.POIs.COORD_LON, 22182770);


		getContentResolver().insert(Wheelmap.POIs.CONTENT_URI, values);

		return true;
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



	public void onHomeClick(View v) {
		final Intent intent = new Intent(this, WheelmapHomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(intent);
	}


	public void onRefreshClick(View v) {
		Toast.makeText(this, "Refreshing..", Toast.LENGTH_SHORT).show();
	}

	public void onSearchClick(View v) {
		Toast.makeText(this, "Searching..", Toast.LENGTH_SHORT).show();
	}


	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
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
