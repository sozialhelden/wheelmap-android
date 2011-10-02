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
