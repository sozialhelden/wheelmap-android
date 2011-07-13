package org.wheelmap.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class POIsMapActivity extends MapActivity {
	
	private MapView map = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		
		
		map=(MapView)findViewById(R.id.map);
		
		map.getController().setZoom(17);
		
		GeoPoint status=new GeoPoint((int)(52.524577*1000000.0),(int)(13.403320*1000000.0));
		
		map.getController().setCenter(status);
		map.setBuiltInZoomControls(true);
		
		//Drawable marker=getResources().getDrawable(R.drawable.marker);
		
		//marker.setBounds(0, 0, marker.getIntrinsicWidth(),											marker.getIntrinsicHeight());
		
		//map
	//		.getOverlays()
		//	.add(new RestaurantOverlay(marker, status,
			//														getIntent().getStringExtra(EXTRA_NAME)));
			 
			 
	}
	
	public void onHomeClick(View v) {
		 final Intent intent = new Intent(this, WheelmapHomeActivity.class);
	        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        this.startActivity(intent);
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
