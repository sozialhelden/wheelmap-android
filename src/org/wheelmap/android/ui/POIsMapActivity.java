package org.wheelmap.android.ui;

import android.os.Bundle;

//import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
//import com.google.android.maps.MapView;

public class POIsMapActivity extends MapActivity {
	
	//private MapView map = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		/*double lat=getIntent().getDoubleExtra("52,524577", 0);
		double lon=getIntent().getDoubleExtra("13,403320", 0);
		
		map=(MapView)findViewById(R.id.map);
		
		map.getController().setZoom(17);
		
		GeoPoint status=new GeoPoint((int)(lat*1000000.0),
																	(int)(lon*1000000.0));
		
		map.getController().setCenter(status);
		map.setBuiltInZoomControls(true);
		
		//Drawable marker=getResources().getDrawable(R.drawable.marker);
		
		//marker.setBounds(0, 0, marker.getIntrinsicWidth(),											marker.getIntrinsicHeight());
		
		//map
	//		.getOverlays()
		//	.add(new RestaurantOverlay(marker, status,
			//														getIntent().getStringExtra(EXTRA_NAME)));
			 * */
			 
	}
	

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
