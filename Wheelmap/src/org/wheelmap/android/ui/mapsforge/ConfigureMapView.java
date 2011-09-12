package org.wheelmap.android.ui.mapsforge;

import java.io.File;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.MapViewMode;
import org.wheelmap.android.R;
import org.wheelmap.android.model.MapFileInfo;
import org.wheelmap.android.model.MapFileInfo.MapFileInfos;
import org.wheelmap.android.service.MapFileService;
import org.wheelmap.android.ui.MapFileSelectActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class ConfigureMapView {
	public static void pickAppropriateMap( Context context, MapView mapView ) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String mapName = prefs.getString(
				MapFileSelectActivity.PREF_KEY_MAP_SELECTED_NAME, "");
		String mapDir = prefs.getString(
				MapFileSelectActivity.PREF_KEY_MAP_SELECTED_DIR, "");

		if (mapName.equals("") || mapDir.equals("")) {
			Uri uri = MapFileInfos.CONTENT_URI_FILES;
			String whereClause = "( " + MapFileInfos.LOCAL_AVAILABLE + " = ? )";
			String[] whereValues = new String[] { String
					.valueOf(MapFileInfo.FILE_COMPLETE) };

			Cursor cursor = context.getContentResolver()
					.query(uri, MapFileInfos.filePROJECTION, whereClause,
							whereValues, null);
			if (cursor.getCount() == 1) {
				cursor.moveToFirst();
				mapName = MapFileInfo.getName(cursor);
				mapDir = MapFileInfo.getParentName(cursor);
				prefs.edit().putString(
						MapFileSelectActivity.PREF_KEY_MAP_SELECTED_NAME,
						mapName).commit();
				prefs.edit()
						.putString(
								MapFileSelectActivity.PREF_KEY_MAP_SELECTED_DIR,
								mapDir).commit();
			}

		}

		String mapFile = MapFileService.LOCAL_BASE_PATH_DIR + File.separator
				+ mapDir + File.separator + mapName;
		mapView.setMapFile(mapFile);

		if (!mapView.hasValidMapFile()) {
			prefs.edit().putString(
					MapFileSelectActivity.PREF_KEY_MAP_SELECTED_NAME, "");
			prefs.edit().putString(
					MapFileSelectActivity.PREF_KEY_MAP_SELECTED_DIR, "");

			mapView.setMapViewMode(MapViewMode.OSMARENDER_TILE_DOWNLOAD);
			final String errorText = context.getString(R.string.error_no_mapfilefound);
			Toast.makeText(context, errorText, Toast.LENGTH_LONG).show();
		} else
			mapView.setMapViewMode(MapViewMode.CANVAS_RENDERER);
	}
}
