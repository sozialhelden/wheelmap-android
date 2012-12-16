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
package org.wheelmap.android.activity;

import java.util.ArrayList;
import java.util.List;

import de.akquinet.android.androlog.Log;
import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapContext;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.TileRAMCache;
import org.wheelmap.android.model.Extra;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import org.wheelmap.android.overlays.ConfigureMapView;

public class MapsforgeMapActivity extends HoloRoboSherlockFragmentActivity
		implements MapContext {

	private final static String TAG = MapsforgeMapActivity.class.getSimpleName();

	/**
	 * Name of the file where the map position and other settings are stored.
	 */
	private static final String PREFERENCES_FILE = "MapActivity";

	/**
	 * Counter to store the last ID given to a MapView.
	 */
	private int lastMapViewId;

	/**
	 * Internal list which contains references to all running MapView objects.
	 */
	private List<MapView> mapViews = new ArrayList<MapView>(2);

	private void destroyMapViews() {
		if (this.mapViews != null) {
			MapView mapView;
			while (!this.mapViews.isEmpty()) {
				mapView = this.mapViews.get(0);
				mapView.destroy();
			}
			mapView = null;
			this.mapViews.clear();
			this.mapViews = null;
		}
	}

	public void destroyMapView(MapView mapView) {
		int i;
		for( i = 0; i < this.mapViews.size(); i++ ) {
			if ( mapViews.get(i) == mapView )
				mapViews.remove( i );
		}

		mapView.destroy();
		mapView = null;
	}

	@Override
	protected void onPause() {
		super.onPause();

		for (int i = 0, n = this.mapViews.size(); i < n; ++i) {
			MapView mapView = this.mapViews.get(i);
			mapView.onPause();
			storeMapView( mapView );
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		for (int i = 0, n = this.mapViews.size(); i < n; ++i) {
			this.mapViews.get(i).onResume();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		destroyMapViews();
	}

	/**
	 * Returns a unique MapView ID on each call.
	 * 
	 * @return the new MapView ID.
	 */
	@Override
	public int getMapViewId() {
		return ++this.lastMapViewId;
	}

	/**
	 * This method is called once by each MapView during its setup process.
	 * 
	 * @param mapView
	 *            the calling MapView.
	 */
	@Override
	public void registerMapView(MapView mapView) {
		ConfigureMapView.pickAppropriateMap(this
				.getApplicationContext(), mapView);
		if (this.mapViews != null) {
			this.mapViews.add(mapView);

			SharedPreferences preferences = getSharedPreferences(
					PREFERENCES_FILE + mapView.getId(), MODE_PRIVATE);
			// restore the position
			if (preferences.contains(Extra.LATITUDE)
					&& preferences.contains(Extra.LONGITUDE)
					&& preferences.contains(Extra.ZOOM_LEVEL)) {
				if (!mapView.getMapViewMode().requiresInternetConnection()
						&& preferences.contains("mapFile")) {
					// get and set the map file
					mapView.setMapFileFromPreferences(preferences.getString(
							"mapFile", null));
				}

				// get and set the map position and zoom level
				GeoPoint defaultStartPoint = mapView.getDefaultStartPoint();
				mapView.setCenterAndZoom(
						new GeoPoint(preferences.getInt(Extra.LATITUDE,
								defaultStartPoint.getLatitudeE6()), preferences
								.getInt(Extra.LONGITUDE,
										defaultStartPoint.getLongitudeE6())),
						(byte) preferences.getInt(Extra.ZOOM_LEVEL,
								mapView.getDefaultZoomLevel()));
			}
		}
	}
	
	private void storeMapView(MapView mapView ) {
		Editor editor = getSharedPreferences(PREFERENCES_FILE + mapView.getId(), MODE_PRIVATE)
				.edit();

		editor.clear();
		if (mapView.hasValidCenter()) {
			if (!mapView.getMapViewMode()
					.requiresInternetConnection()
					&& mapView.hasValidMapFile()) {
				// save the map file
				editor.putString("mapFile", mapView.getMapFile());
			}
			// save the map position and zoom level
			GeoPoint mapCenter = mapView.getMapCenter();
			editor.putInt(Extra.LATITUDE, mapCenter.getLatitudeE6());
			editor.putInt(Extra.LONGITUDE, mapCenter.getLongitudeE6());
			editor.putInt(Extra.ZOOM_LEVEL, mapView.getZoomLevel());
		}
		editor.commit();
	}

	/**
	 * This method is called once by each MapView when it gets destroyed.
	 * 
	 * @param mapView
	 *            the calling MapView.
	 */
	@Override
	public void unregisterMapView(MapView mapView) {
		storeMapView(mapView);
		if (this.mapViews != null) {
			this.mapViews.remove(mapView);
		}
	}

	/**
	 * This method sets the size of the shared RAM cache in TileRAMCache
	 * 
	 * @param capacity
	 *            number of tiles
	 */

	public static void setSharedRAMCacheCapacity(int capacity) {
		TileRAMCache.SHARED_CAPACITY = capacity;
	}

}
