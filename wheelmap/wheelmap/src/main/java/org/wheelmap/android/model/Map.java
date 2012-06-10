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
