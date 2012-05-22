/*
Copyright (C) 2011 Michal Harakal and Michael Kroez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS-IS" BASIS
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
        
 */

package org.wheelmap.android.model;

import java.util.ArrayList;
import java.util.List;

import org.wheelmap.android.model.Support.CategoriesContent;

import wheelmap.org.WheelchairState;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class QueriesBuilderHelper {

	public static final String PREF_KEY_WHEELCHAIR_STATE_FULL = "showFull";
	public static final String PREF_KEY_WHEELCHAIR_STATE_LIMITED = "showLimited";
	public static final String PREF_KEY_WHEELCHAIR_STATE_NO = "showNo";
	public static final String PREF_KEY_WHEELCHAIR_STATE_UNKNOWN = "showUnknown";

	private static final String ENTRY_NOT_UPDATE_PENDING = " ("
			+ Wheelmap.POIs.UPDATE_TAG + "!="
			+ Wheelmap.UPDATE_PENDING_STATE_ONLY + " ) AND ( "
			+ Wheelmap.POIs.UPDATE_TAG + "!=" + Wheelmap.UPDATE_ALL_FIELDS
			+ ") AND ( " + Wheelmap.POIs.UPDATE_TAG + "!="
			+ Wheelmap.UPDATE_WHEELCHAIR_STATE + ")";

	static private String categoriesFilter(Context context) {
		// categories id

		// Run query
		Uri uri = Support.CategoriesContent.CONTENT_URI;
		Cursor cursor = context.getContentResolver().query(uri, null, null,
				null, null);
		if (cursor == null)
			return null;

		StringBuilder categories = new StringBuilder("");

		int selectedCount = 0;
		if (cursor.moveToFirst()) {
			do {
				int id = CategoriesContent.getCategoryId(cursor);
				if (CategoriesContent.getSelected(cursor)) {
					selectedCount++;
					if (categories.length() > 0)
						categories.append(" OR category_id=");
					else
						categories.append(" category_id=");
					categories.append(new Integer(id).toString());

				}

			} while (cursor.moveToNext());
		}
		if (selectedCount == 0) {
			if (cursor.moveToFirst()) {
				do {
					int id = CategoriesContent.getCategoryId(cursor);
					if (categories.length() > 0)
						categories.append(" AND NOT category_id=");
					else
						categories.append(" NOT category_id=");

					categories.append(new Integer(id).toString());

				} while (cursor.moveToNext());
			}

		}

		cursor.close();
		// Log.d("QueriesBuilderHelper", categories.toString());
		return categories.toString();

	}

	static public List<WheelchairState> getWheelchairStateFromPreferences(
			Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		boolean prefStateFull = prefs.getBoolean(
				PREF_KEY_WHEELCHAIR_STATE_FULL, true);
		boolean prefStateLimited = prefs.getBoolean(
				PREF_KEY_WHEELCHAIR_STATE_LIMITED, true);
		boolean prefStateNo = prefs.getBoolean(PREF_KEY_WHEELCHAIR_STATE_NO,
				true);
		boolean prefStateUnknown = prefs.getBoolean(
				PREF_KEY_WHEELCHAIR_STATE_UNKNOWN, true);

		ArrayList<WheelchairState> list = new ArrayList<WheelchairState>();
		if (prefStateFull)
			list.add(WheelchairState.YES);
		if (prefStateLimited)
			list.add(WheelchairState.LIMITED);
		if (prefStateNo)
			list.add(WheelchairState.NO);
		if (prefStateUnknown)
			list.add(WheelchairState.UNKNOWN);

		return list;
	}

	static public String userSettingsFilter(Context context) {
		String result = categoriesFilter(context);

		List<WheelchairState> wheelChairState = getWheelchairStateFromPreferences(context);

		StringBuilder wheelchair = new StringBuilder("");

		for (WheelchairState state : wheelChairState) {
			if (wheelchair.length() > 0)
				wheelchair.append(" OR wheelchair=");
			else
				wheelchair.append(" wheelchair=");
			wheelchair.append(new Integer(state.getId()).toString());
		}

		if (wheelchair.toString().length() == 0) {
			for (WheelchairState state : WheelchairState.values()) {
				if (wheelchair.length() > 0)
					wheelchair.append(" AND NOT wheelchair=");
				else
					wheelchair.append(" NOT wheelchair=");
				wheelchair.append(new Integer(state.getId()).toString());
			}
		}

		if (result.length() > 0)
			result = "(" + result + ") AND  (" + wheelchair.toString()
					+ ") AND (" + ENTRY_NOT_UPDATE_PENDING + ")";
		else
			result = "(" + wheelchair.toString() + ") AND "
					+ ENTRY_NOT_UPDATE_PENDING;

		// Log.d("QueriesBuilderHelper userSettingsFilter", result);
		return result;

	}

}
