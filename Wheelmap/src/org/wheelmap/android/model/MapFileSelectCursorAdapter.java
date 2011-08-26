package org.wheelmap.android.model;

import org.wheelmap.android.ui.MapFileSelectActivity;
import org.wheelmap.android.ui.MapFileSelectItemView;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class MapFileSelectCursorAdapter extends CursorAdapter {

	public MapFileSelectCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public void bindView(View view, Context ctx, Cursor cursor) {
		MapFileSelectItemView mfsiv = (MapFileSelectItemView) view;

		String screenName = MapFileInfo.getScreenName(cursor);
		String fileName = MapFileInfo.getName( cursor );
		String directory = MapFileInfo.getParentName(cursor);

		String directoryName = directory.replace("/", " ");
		mfsiv.setName(screenName);
		mfsiv.setDirectory(directoryName);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);

		String prefName = prefs.getString(
				MapFileSelectActivity.PREF_KEY_MAP_SELECTED_NAME, "");
		String prefDir = prefs.getString(
				MapFileSelectActivity.PREF_KEY_MAP_SELECTED_DIR, "");

		if (fileName.equals(prefName)
				&& directory.equals(prefDir))
			mfsiv.setCheckboxChecked(true);
		else
			mfsiv.setCheckboxChecked(false);
	}

	@Override
	public View newView(Context ctx, Cursor cursor, ViewGroup parent) {
		return new MapFileSelectItemView(ctx);
	}

}
