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

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.MapViewMode;
import org.wheelmap.android.model.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;

public class ConfigureMapView {
	private final static MapViewMode defaultViewMode = MapViewMode.MAPNIK_TILE_DOWNLOAD;
	private final static int MAP_CACHE_IN_MB = 10;
	private final static int MB_TO_BYTES_MULTIPLIER = 1000000;
	

	// private final static MapViewMode defaultViewMode =
	// MapViewMode.OSMARENDER_TILE_DOWNLOAD;

	public static void pickAppropriateMap(Context context, MapView mapView) {
		int tileSizeInBytes = MapView.getTileSizeInBytes();
		int tileNum = MAP_CACHE_IN_MB * MB_TO_BYTES_MULTIPLIER / tileSizeInBytes;
//		Log.d( "mapsforge", "tileSizeInBytes = " + tileSizeInBytes + " tileNum = " + tileNum );
		
		mapView.setMemoryCardCacheSize( tileNum );
		mapView.setMemoryCardCachePersistence( true );
		
		ProviderInfo info = context.getPackageManager().resolveContentProvider(
				Map.AUTHORITY, 0);
		
		if (info == null) {
			mapView.setMapViewMode(defaultViewMode);
			return;
		}
		
		ContentResolver resolver = context.getContentResolver();
		Cursor c;
		try {
			 c = resolver.query(Map.CONTENT_URI_SELECTED,
					Map.selectedPROJECTION, null, null, null);
		} catch (RuntimeException e) {
			mapView.setMapViewMode(defaultViewMode);
			return;
		}
		if ( c == null ) {
			mapView.setMapViewMode(defaultViewMode);
			return;
		}
					
		String mapFile;
		if (c.getCount() == 1) {
			c.moveToFirst();
			mapFile = Map.createPath(Map.getParentName(c), Map.getName(c));
			mapView.setMapFile(mapFile);
			if (mapView.hasValidMapFile())
				mapView.setMapViewMode(MapViewMode.CANVAS_RENDERER);
			else
				mapView.setMapViewMode(defaultViewMode);
		} else
			mapView.setMapViewMode(defaultViewMode);

		c.close();
	}
}
