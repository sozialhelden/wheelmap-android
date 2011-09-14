package org.wheelmap.android.ui;

import org.wheelmap.android.R;
import org.wheelmap.android.model.MapFileInfo;
import org.wheelmap.android.model.MapFileInfo.MapFileInfos;
import org.wheelmap.android.model.MapFileSelectCursorAdapter;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;

public class MapFileSelectActivity extends ListActivity {

	private final static String TAG = "mapfileselect";

	public static final String PREF_KEY_MAP_SELECTED_NAME = "pref_key_map_selected_name";
	public static final String PREF_KEY_MAP_SELECTED_DIR = "pref_key_map_selected_dir";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mapfile_select);

		Uri uri = MapFileInfos.CONTENT_URI_FILES;
		String whereClause = "( " + MapFileInfos.LOCAL_AVAILABLE + " = ? )";
		String[] whereValues = new String[] { String
				.valueOf(MapFileInfo.FILE_COMPLETE) };

		Cursor cursor = managedQuery(uri, MapFileInfos.filePROJECTION,
				whereClause, whereValues, null);
		startManagingCursor(cursor);

		MapFileSelectCursorAdapter adapter = new MapFileSelectCursorAdapter(
				this, cursor);
		setListAdapter(adapter);

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Cursor cursor = (Cursor) l.getItemAtPosition(position);

		String fileName = MapFileInfo.getName(cursor);
		String directory = MapFileInfo.getParentName(cursor);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		prefs.edit()
				.putString(MapFileSelectActivity.PREF_KEY_MAP_SELECTED_NAME,
						fileName).commit();
		prefs.edit()
				.putString(MapFileSelectActivity.PREF_KEY_MAP_SELECTED_DIR,
						directory).commit();

		((MapFileSelectCursorAdapter) l.getAdapter())
				.notifyDataSetChanged();		
	}

}
