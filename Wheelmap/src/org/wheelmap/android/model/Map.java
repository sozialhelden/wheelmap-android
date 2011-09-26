package org.wheelmap.android.model;

import java.io.File;

import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;

public class Map implements BaseColumns {
	
	private static String LOCAL_BASE_PATH_DIR;

	public static final String TAG = "org.wheelmap.android";
	public static final String AUTHORITY = "de.studiorutton.android.offlinemapaddon";
	public static final Uri CONTENT_URI_SELECTED = Uri.parse("content://"
			+ AUTHORITY + "/selected");

	public static final String NAME = "name";
	public static final String PARENT_NAME = "parent_name";

	public static String getName(Cursor c) {
		return c.getString(c.getColumnIndexOrThrow(NAME));
	}

	public static String getParentName(Cursor c) {
		return c.getString(c.getColumnIndexOrThrow(PARENT_NAME));
	}

	public static final String[] selectedPROJECTION = new String[] { NAME,
			PARENT_NAME };

	public static final String createPath( String dirName, String fileName ) {
		String mapFile = LOCAL_BASE_PATH_DIR + File.separator
				+ dirName + File.separator + fileName;
		
		return mapFile;
	}
	
	static {
		LOCAL_BASE_PATH_DIR = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separator
				+ "offlinemaps"
				+ File.separator + "maps";
	}
}
