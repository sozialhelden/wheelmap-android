package org.wheelmap.android.ui.mapsforge;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.MapViewMode;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MyMapView extends MapView {
	private int mLastRequestedLatitude, mLastRequestedLongitude;
	private MapViewTouchMove listener;
	
	public MyMapView(Context context) {
		super(context);
	}

	public MyMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public MyMapView(Context context, MapViewMode mapViewMode) {
		super(context, mapViewMode);
	}

	public void registerListener( MapViewTouchMove listener ) {
		this.listener = listener;
	}
	
	public void setLastRequestedLocation( GeoPoint point ) {
		mLastRequestedLatitude = point.getLatitudeE6();
		mLastRequestedLongitude = point.getLongitudeE6();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = super.onTouchEvent( event );
		int minimalLatitudeSpan = getLatitudeSpan() / 3;
		int minimalLongitudeSpan = getLongitudeSpan() / 3;
			
		GeoPoint centerLocation = getMapCenter();
		if ( Math.abs( mLastRequestedLatitude - centerLocation.getLatitudeE6()) > minimalLatitudeSpan ||
				Math.abs( mLastRequestedLongitude - centerLocation.getLongitudeE6()) > minimalLongitudeSpan ) {
			mLastRequestedLatitude = centerLocation.getLatitudeE6();
			mLastRequestedLongitude = centerLocation.getLongitudeE6();
			
			if ( listener != null)
				listener.onMapViewTouchMoveEnough();
		}
		
		return result;
	}

	public interface MapViewTouchMove {
		public void onMapViewTouchMoveEnough();
	}
}
