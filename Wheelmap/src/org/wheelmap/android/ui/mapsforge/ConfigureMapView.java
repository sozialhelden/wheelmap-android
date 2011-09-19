package org.wheelmap.android.ui.mapsforge;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.MapViewMode;

import android.content.Context;

public class ConfigureMapView {
	public static void pickAppropriateMap( Context context, MapView mapView ) {
	  // mapView.setMapViewMode(MapViewMode.OSMARENDER_TILE_DOWNLOAD);
		mapView.setMapViewMode(MapViewMode.MAPNIK_TILE_DOWNLOAD);
	}
}
