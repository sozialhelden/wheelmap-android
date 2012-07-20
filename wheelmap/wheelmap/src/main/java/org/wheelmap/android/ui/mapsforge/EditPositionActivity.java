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
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.online.R;
import org.wheelmap.android.overlays.POILocationEditableOverlay;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class EditPositionActivity extends MapActivity implements
		OnTouchListener {
	private final static String TAG = "editposition";

	private MapController mMapController;
	private MapView mMapView;
	private POILocationEditableOverlay mMapOverlay;

	private int mCrrLatitude;
	private int mCrrLongitude;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_position_edit);

		mCrrLatitude = getIntent().getIntExtra(Extra.LATITUDE, -1);
		mCrrLongitude = getIntent().getIntExtra(Extra.LONGITUDE, -1);
		Log.d(TAG, "mCrrLatitude = " + mCrrLatitude + " mCrrLongitude = "
				+ mCrrLongitude);

		mMapView = (MapView) findViewById(R.id.map);

		mMapView.setBuiltInZoomControls(true);
		ConfigureMapView.pickAppropriateMap(this, mMapView);
		mMapController = mMapView.getController();
		mMapController.setZoom(18);
		mMapController.setCenter(new GeoPoint(mCrrLatitude, mCrrLongitude));
		mMapOverlay = new POILocationEditableOverlay(mCrrLatitude,
				mCrrLongitude, getResources().getDrawable(
						R.drawable.position_pin));
		mMapOverlay.enableLowDrawQuality(true);
		mMapOverlay.enableUseOnlyOneBitmap(true);
		mMapView.getOverlays().add(mMapOverlay);
		mMapView.setOnTouchListener(this);
	}

	public void onSaveClick(View v) {
		Intent intent = new Intent();
		intent.putExtra(Extra.LATITUDE, mCrrLatitude);
		intent.putExtra(Extra.LONGITUDE, mCrrLongitude);
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			GeoPoint geoPoint = mMapView.getProjection().fromPixels(
					(int) event.getX(), (int) event.getY());
			mCrrLatitude = geoPoint.getLatitudeE6();
			mCrrLongitude = geoPoint.getLongitudeE6();
			mMapOverlay.setPosition(geoPoint);
			return true;
		}
		return false;
	}
}
