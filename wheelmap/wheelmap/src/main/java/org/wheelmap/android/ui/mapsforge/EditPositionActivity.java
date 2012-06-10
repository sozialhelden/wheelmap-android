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
package org.wheelmap.android.ui.mapsforge;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapController;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.ItemizedOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.wheelmap.android.online.R;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class EditPositionActivity extends MapActivity implements OnTouchListener {
	private final static String TAG = "editposition";
	
	private MapController mMapController;
	private MapView mMapView;
	private POILocationEditableOverlay mMapOverlay;

	private int mCrrLatitude;
	private int mCrrLongitude;

	public final static String EXTRA_LATITUDE = "org.wheelmap.android.ui.mapsforge.LATITUDE";
	public final static String EXTRA_LONGITUDE = "org.wheelmap.android.ui.mapsforge.LONGITUDE";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_position_edit);
		
		mCrrLatitude = getIntent().getIntExtra(EXTRA_LATITUDE, -1);
		mCrrLongitude = getIntent().getIntExtra(EXTRA_LONGITUDE, -1);
		Log.d( TAG, "mCrrLatitude = " + mCrrLatitude + " mCrrLongitude = " + mCrrLongitude);

		mMapView = (MapView) findViewById(R.id.map);

		mMapView.setBuiltInZoomControls(true);
		ConfigureMapView.pickAppropriateMap(this, mMapView);
		mMapController = mMapView.getController();
		mMapController.setZoom(18);
		mMapController.setCenter( new GeoPoint( mCrrLatitude, mCrrLongitude));
		mMapOverlay = new POILocationEditableOverlay( mCrrLatitude, mCrrLongitude);
		mMapOverlay.enableLowDrawQuality( true );
		mMapOverlay.enableUseOnlyOneBitmap( true );
		mMapView.getOverlays().add(mMapOverlay);
		mMapView.setOnTouchListener( this );
	}

	public void onSaveClick(View v) {
		Intent intent = new Intent();
		intent.putExtra(EXTRA_LATITUDE, mCrrLatitude);
		intent.putExtra(EXTRA_LONGITUDE, mCrrLongitude);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch( event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			GeoPoint geoPoint = mMapView.getProjection().fromPixels( (int) event.getX(), (int) event.getY());
			mCrrLatitude = geoPoint.getLatitudeE6();
			mCrrLongitude = geoPoint.getLongitudeE6();
			mMapOverlay.setPosition( geoPoint );
			return true;
		}
		return false;
	}

	private class POILocationEditableOverlay extends
			ItemizedOverlay<OverlayItem> {
		private OverlayItem item;
		Drawable marker;

		private int items;

		public POILocationEditableOverlay( int latitude, int longitude) {
			super(null);
			items = 0;
			marker = EditPositionActivity.this.getResources().getDrawable(
					R.drawable.position_pin);
			ItemizedOverlay.boundCenterBottom(marker);
			item = new OverlayItem();
			item.setMarker(marker);
			item.setPoint(new GeoPoint(latitude, longitude));
			items = 1;
		}

		public void setPosition(GeoPoint geoPoint) {
			item.setPoint(geoPoint);
			populate();
		}

		@Override
		public int size() {
			return items;
		}

		@Override
		protected OverlayItem createItem(int index) {
			if (index > 0)
				return null;
			return item;
		}
	}
}
