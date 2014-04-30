/*
 * #%L
 * Wheelmap - App
 * %%
 * Copyright (C) 2011 - 2012 Michal Harakal - Michael Kroez - Sozialhelden e.V.
 * %%
 * Wheelmap App based on the Wheelmap Service by Sozialhelden e.V.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use file except in compliance with the License.
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

import org.holoeverywhere.app.Activity;
import org.mapsforge.android.maps.MapContext;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.TileRAMCache;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapView;
import org.osmdroid.mapsforge.wrapper.MFMapView;
import org.osmdroid.util.GeoPoint;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.modules.BundlePreferences;
import org.wheelmap.android.modules.IBundlePreferences;
import org.wheelmap.android.overlays.ConfigureMapView;

import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import de.akquinet.android.androlog.Log;

public class MapActivity extends Activity implements MapContext {

    private final static String TAG = MapActivity.class.getSimpleName();

    private int lastMapViewId;

    private List<IMapView> mapViews = new ArrayList<IMapView>(2);

    //@Inject
    private IBundlePreferences bprefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bprefs = new BundlePreferences(this);
    }

    /**
     * method sets the size of the shared RAM cache in TileRAMCache
     *
     * @param capacity number of tiles
     */

    public static void setSharedRAMCacheCapacity(int capacity) {
        TileRAMCache.SHARED_CAPACITY = capacity;
    }

    public void destroyMapViews() {
        if (mapViews != null) {
            IMapView mapView;
            while (!mapViews.isEmpty()) {
                int last = mapViews.size() - 1;
                mapView = mapViews.get(last);
                destroyMapViewSingle(mapView);
                mapViews.remove(last);
            }
            mapViews.clear();
            mapViews = null;
        }
    }

    private void destroyMapViewSingle(IMapView mapView) {
        if (mapView instanceof MFMapView) {
            ((MFMapView) mapView).destroy();
        }
    }

    public void destroyMapView(MapView mapView) {
        int i;
        for (i = 0; i < mapViews.size(); i++) {
            if (mapViews.get(i) == mapView) {
                mapViews.remove(i);
            }
        }

        mapView.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        for (int i = 0, n = mapViews.size(); i < n; ++i) {
            IMapView mapView = mapViews.get(i);
            if (mapView instanceof MFMapView) {
                ((MFMapView) mapView).onPause();
            }
            storeMapView(mapView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (int i = 0, n = mapViews.size(); i < n; ++i) {
            IMapView mapView = mapViews.get(i);
            if (mapView instanceof MFMapView) {
                ((MFMapView) mapView).onResume();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyMapViews();
        System.gc();
    }


    /**
     * Returns a unique MFMapView ID on each call.
     *
     * @return the new MFMapView ID.
     */
    @Override
    public int getMapViewId() {
        return ++lastMapViewId;
    }

    @Override
    public void registerMapView(MapView mapView) {
        MFMapView mv = new MFMapView(mapView);
        registerMapView(mv);
        ConfigureMapView.pickAppropriateMap(getApplicationContext(),
                mv.getWrappedMapView());
        loadPreferences(mv);
    }

    /**
     * method is called once by each MFMapView during its setup process.
     *
     * @param mapView the calling MFMapView.
     */
    public void registerMapView(IMapView mapView) {
        mapViews.add(mapView);
    }

    public int getId(IMapView mapView) {
        if (mapView instanceof MFMapView) {
            return ((MFMapView) mapView).getId();
        } else {
            return ((View) mapView).getId();
        }
    }

    public boolean loadPreferences(IMapView mapView) {

        if (mapViews == null) {
            return false;
        }

        int id = getId(mapView);
        Log.d(TAG, "loadPreferences: mapView " + mapView + " id = " + id);
        String mapId = String.valueOf(id) + "_map";
        if (bprefs.contains(mapId)) {
            Bundle b = bprefs.retrieve(mapId);

            String fileName = b.getString("mapFile");
            if (fileName != null) {
                initMapsforgeFile((MFMapView) mapView, fileName);
            }

            int latitudeE6 = b.getInt(Extra.LATITUDE, Integer.MAX_VALUE);
            int longitudeE6 = b.getInt(Extra.LONGITUDE, Integer.MAX_VALUE);
            int zoom = b.getInt(Extra.ZOOM_LEVEL, -1);
            Log.d(TAG,
                    "loadPreferences: latitudeE6 = " + latitudeE6 + " longitudeE6 = " + longitudeE6
                            + " zoom = " + zoom);

            if (latitudeE6 != Integer.MAX_VALUE) {
                mapView.getController().setCenter(new GeoPoint(latitudeE6, longitudeE6));
            }

            if (zoom != -1) {
                mapView.getController().setZoom(zoom);
            }

            return true;
        }

        return false;
    }

    private void initMapsforgeFile(MFMapView mapView, String fileName) {
        if (!mapView.getMapViewMode().requiresInternetConnection() && fileName != null) {
            mapView.setMapFileFromPreferences(fileName);
        }
    }

    private void storeMapsforgeFile(MFMapView mapView, Bundle b) {
        if (!mapView.getMapViewMode().requiresInternetConnection()
                && mapView.hasValidMapFile()) {

        }
    }

    private void storeMapView(IMapView mapView) {

        if (mapView instanceof MFMapView && !((MFMapView) mapView).hasValidCenter()) {
            return;
        }

        Bundle b = new Bundle();
        if (mapView instanceof MFMapView) {
            storeMapsforgeFile((MFMapView) mapView, b);
        }

        IGeoPoint mapCenter = mapView.getMapCenter();
        b.putInt(Extra.LATITUDE, mapCenter.getLatitudeE6());
        b.putInt(Extra.LONGITUDE, mapCenter.getLongitudeE6());
        b.putInt(Extra.ZOOM_LEVEL, mapView.getZoomLevel());

        int id = getId( mapView );
        String mapId = String.valueOf(id) + "_map";
        bprefs.store(mapId, b);
    }

    /**
     * method is called once by each MFMapView when it gets destroyed.
     *
     * @param mapView the calling MFMapView.
     */
    @Override
    public void unregisterMapView(MapView mapView) {
        unregisterMapView(new MFMapView(mapView));
    }

    public void unregisterMapView(IMapView mapView) {
        storeMapView(mapView);
        if (mapViews != null) {
            mapViews.remove(mapView);
        }

        if(mapView instanceof org.osmdroid.views.MapView){
            ((org.osmdroid.views.MapView)mapView).getTileProvider().clearTileCache();
            System.gc();
        }

    }

}
