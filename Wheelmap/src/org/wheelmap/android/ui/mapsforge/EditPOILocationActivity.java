/*
Copyright (C) 2011 Michal Harakal and Michael Kroez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS-IS" BASIS
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
        
*/

package org.wheelmap.android.ui.mapsforge;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.overlay.ItemizedOverlay;
import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapController;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.wheelmap.android.R;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class EditPOILocationActivity extends MapActivity implements OnClickListener {
	private MapController mMapController;
	private MapView mMapView;
	private POILocationEditableOverlay mMapOverlay;
	private Button mButtonSave;
	private Button mButtonCancel;
	
	private int mCrrLatitude;
	private int mCrrLongitude;
	
	public final static String EXTRA_LATITUDE = "org.wheelmap.android.ui.mapsforge.LATITUDE";
	public final static String EXTRA_LONGITUDE = "org.wheelmap.android.ui.mapsforge.LONGITUDE";
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_poilocation_edit);
		mButtonSave = (Button) findViewById( R.id.btn_save);
		mButtonSave.setOnClickListener( this );

		mButtonCancel = (Button) findViewById( R.id.btn_cancel);
		mButtonCancel.setOnClickListener( this );
		
		mMapView = (MapView) findViewById(R.id.map);

		mMapView.setClickable(false);
		mMapView.setBuiltInZoomControls(true);
		ConfigureMapView.pickAppropriateMap( this, mMapView );
		mMapController = mMapView.getController();
		mMapController.setZoom(18);
		mMapOverlay = new POILocationEditableOverlay();
		mMapView.getOverlays().add( mMapOverlay );
		
		int latitude = getIntent().getIntExtra( EXTRA_LATITUDE, -1 );
		int longitude = getIntent().getIntExtra( EXTRA_LONGITUDE, -1 );
		
		if ( latitude == -1 || longitude == -1 ) {
			setResult( RESULT_CANCELED );
			finish();
		}
		
		mMapOverlay.setItem( latitude, longitude );
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch( id ) {
		case R.id.btn_save:
			Intent intent = new Intent();
			intent.putExtra( EXTRA_LATITUDE, mCrrLatitude );
			intent.putExtra( EXTRA_LONGITUDE, mCrrLongitude );
			setResult( RESULT_OK, intent );
			break;
		case R.id.btn_cancel:
			setResult( RESULT_CANCELED );
			break;
		}
		finish();
	}
	
	private class POILocationEditableOverlay extends ItemizedOverlay<OverlayItem> {
		private OverlayItem item;
		Drawable marker;
		
		private int items;
		
		public POILocationEditableOverlay () {
			super(null);
			items = 0;
			marker = EditPOILocationActivity.this.getResources().getDrawable( R.drawable.wheelchair_state_unknown );
		}
	
		@Override
		public boolean onTap(GeoPoint geoPoint, MapView mapView) {
			mCrrLatitude = geoPoint.getLatitudeE6();
			mCrrLongitude = geoPoint.getLongitudeE6();
			
			if ( item == null ) {
				item = new OverlayItem();
				item.setMarker( marker );
			}
			item.setPoint( geoPoint );
			populate();
			return true;
		}

		public void setItem( int latitude, int longitude ) {
			mCrrLatitude = latitude;
			mCrrLongitude = longitude;
			
			item = new OverlayItem();
			item.setMarker( marker );
			item.setPoint( new GeoPoint( latitude, longitude));
			items = 1;
			
			populate();
		}

		@Override
		public int size() {
			return items;
		}

		@Override
		protected OverlayItem createItem(int index) {
			if ( index > 0 )
				return null;
			return item;
		}	
	}
}
