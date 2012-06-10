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

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapContext;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.TileRAMCache;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class MapsforgeMapActivity extends SherlockFragmentActivity implements MapContext {

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
                    MapView currentMapView;
                    while (!this.mapViews.isEmpty()) {
                            currentMapView = this.mapViews.get(0);
                            currentMapView.destroy();
                    }
                    currentMapView = null;
                    this.mapViews.clear();
                    this.mapViews = null;
            }
    }

    @Override
	public void onDestroy() {
            super.onDestroy();
            destroyMapViews();
    }

    @Override
	public void onPause() {
            super.onPause();
            Editor editor = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE).edit();
            for (int i = 0, n = this.mapViews.size(); i < n; ++i) {
                    MapView currentMapView = this.mapViews.get(i);
                    currentMapView.onPause();

                    editor.clear();
                    if (currentMapView.hasValidCenter()) {
                            if (!currentMapView.getMapViewMode().requiresInternetConnection()
                                            && currentMapView.hasValidMapFile()) {
                                    // save the map file
                                    editor.putString("mapFile", currentMapView.getMapFile());
                            }
                            // save the map position and zoom level
                            GeoPoint mapCenter = currentMapView.getMapCenter();
                            editor.putInt("latitude", mapCenter.getLatitudeE6());
                            editor.putInt("longitude", mapCenter.getLongitudeE6());
                            editor.putInt("zoomLevel", currentMapView.getZoomLevel());
                    }
                    editor.commit();
            }
    }

    @Override
	public void onResume() {
            super.onResume();
            for (int i = 0, n = this.mapViews.size(); i < n; ++i) {
                    this.mapViews.get(i).onResume();
            }
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
            if (this.mapViews != null) {
                    this.mapViews.add(mapView);

                    SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
                    // restore the position
                    if (preferences.contains("latitude") && preferences.contains("longitude")
                                    && preferences.contains("zoomLevel")) {
                            if (!mapView.getMapViewMode().requiresInternetConnection()
                                            && preferences.contains("mapFile")) {
                                    // get and set the map file
                                    mapView.setMapFileFromPreferences(preferences.getString("mapFile", null));
                            }

                            // get and set the map position and zoom level
                            GeoPoint defaultStartPoint = mapView.getDefaultStartPoint();
                            mapView.setCenterAndZoom(new GeoPoint(preferences.getInt("latitude",
                                            defaultStartPoint.getLatitudeE6()), preferences.getInt("longitude",
                                            defaultStartPoint.getLongitudeE6())), (byte) preferences.getInt(
                                            "zoomLevel", mapView.getDefaultZoomLevel()));
                    }
            }
    }

    /**
     * This method is called once by each MapView when it gets destroyed.
     * 
     * @param mapView
     *            the calling MapView.
     */
    @Override
    public void unregisterMapView(MapView mapView) {
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
