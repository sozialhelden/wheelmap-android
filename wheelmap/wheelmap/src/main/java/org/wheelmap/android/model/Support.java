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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class Support {

	public static final String TAG = "wheelmapsupport";
	public static final String AUTHORITY = "org.wheelmap.android.support";

	// This class cannot be instantiated
	private Support() {
	}

	public static interface LastUpdateColumns {
		public static final String DATE = "date";
	}

	public static interface LocaleColumns {
		public static final String LOCALE_ID = "locale_id";
		public static final String LOCALIZED_NAME = "localized_name";
	}

	public static interface CategoryColumns {
		public static final String CATEGORY_ID = "category_id";
		public static final String LOCALIZED_NAME = "localized_name";
		public static final String IDENTIFIER = "identifier";
		public static final String SELECTED = "selected";
	}

	public static interface NodeTypeColumns {
		public static final String NODETYPE_ID = "nodetype_id";
		public static final String IDENTIFIER = "identifier";
		public static final String ICON_URL = "icon_url";
		public static final String LOCALIZED_NAME = "localized_name";
		public static final String CATEGORY_ID = "category_id";
		public static final String CATEGORY_IDENTIFIER = "category_identifier";
	}

	public static final class LastUpdateContent implements BaseColumns,
			LastUpdateColumns {
		private LastUpdateContent() {
		};

		private static final String ISO_8601_DATEFORMAT = "yyyy-MM-dd HH:mm:ss";

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/lastupdate");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.wheelmap.lastupdate";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.wheelmap.lastupdate";

		public static final String[] PROJECTION = new String[] { _ID, DATE };

		public static String getDate(Cursor c) {
			return c.getString(c.getColumnIndexOrThrow(DATE));
		}

		public static String formatDate(Date date) {
			SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_DATEFORMAT);
			return sdf.format(date);
		}

		public static Date parseDate(String iso8601_date) throws ParseException {
			SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_DATEFORMAT);
			return sdf.parse(iso8601_date);
		}
	}

	public static final class LocalesContent implements BaseColumns,
			LocaleColumns {
		private LocalesContent() {
		}

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/locales");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.wheelmap.locales";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.wheelmap.locales";

		public static final String[] PROJECTION = new String[] { _ID,
				LOCALE_ID, LOCALIZED_NAME };

		public static String getId(Cursor c) {
			return c.getString(c.getColumnIndexOrThrow(LOCALE_ID));
		}

		public static String getLocalizedName(Cursor c) {
			return c.getString(c.getColumnIndexOrThrow(LOCALIZED_NAME));
		}
	}

	public static final class CategoriesContent implements BaseColumns,
			CategoryColumns {
		private CategoriesContent() {
		};

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/categories");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.wheelmap.categories";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.wheelmap.categories";
		public static final String DEFAULT_SORT_ORDER = LOCALIZED_NAME + " ASC";

		public static final String[] PROJECTION = new String[] { _ID,
				CATEGORY_ID, LOCALIZED_NAME, IDENTIFIER, SELECTED };

		public static final int SELECTED_NO = 0;
		public static final int SELECTED_YES = 1;

		public static int getCategoryId(Cursor c) {
			return c.getInt(c.getColumnIndexOrThrow(CATEGORY_ID));
		}

		public static String getLocalizedName(Cursor c) {
			return c.getString(c.getColumnIndexOrThrow(LOCALIZED_NAME));
		}

		public static String getIdentifier(Cursor c) {
			return c.getString(c.getColumnIndexOrThrow(IDENTIFIER));
		}

		public static Boolean getSelected(Cursor c) {
			int value = c.getInt(c.getColumnIndexOrThrow(SELECTED));
			if (value == SELECTED_YES)
				return true;
			else
				return false;
		}
	}

	public static final class NodeTypesContent implements BaseColumns,
			NodeTypeColumns {
		private NodeTypesContent() {
		};

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/nodetypes");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.wheelmap.nodetypes";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.wheelmap.nodetypes";

		public static final String[] PROJECTION = new String[] { _ID,
				NODETYPE_ID, IDENTIFIER, ICON_URL, LOCALIZED_NAME,
				CATEGORY_ID, CATEGORY_IDENTIFIER };

		public static int getNodeTypeId(Cursor c) {
			return c.getInt(c.getColumnIndexOrThrow(NODETYPE_ID));
		}

		public static String getIdentifier(Cursor c) {
			return c.getString(c.getColumnIndexOrThrow(IDENTIFIER));
		}

		public static String getIconURL(Cursor c) {
			return c.getString(c.getColumnIndexOrThrow(ICON_URL));
		}

		public static String getLocalizedName(Cursor c) {
			return c.getString(c.getColumnIndexOrThrow(LOCALIZED_NAME));
		}

		public static int getCategoryId(Cursor c) {
			return c.getInt(c.getColumnIndexOrThrow(CATEGORY_ID));
		}

		public static String getCategoryIdentifier(Cursor c) {
			return c.getString(c.getColumnIndexOrThrow(CATEGORY_IDENTIFIER));
		}
	}
}
