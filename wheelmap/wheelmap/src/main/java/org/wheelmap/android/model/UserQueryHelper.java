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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.manager.SupportManager.WheelchairAttributes;
import org.wheelmap.android.model.Support.CategoriesContent;
import org.wheelmap.android.model.Wheelmap.POIs;

import wheelmap.org.WheelchairState;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.squareup.otto.Bus;

import de.akquinet.android.androlog.Log;

public class UserQueryHelper {
	private static final String TAG = UserQueryHelper.class.getSimpleName();

	private static UserQueryHelper INSTANCE;
	private Context mContext;
	private String mCategoriesQuery;
	private String mWheelchairQuery;
	private static String mQuery;
	private Bus mBus;

	private UserQueryHelper(Context context) {
		mContext = context;
		mBus = WheelmapApp.getBus();
		mBus.register(this);
		initCategoriesQuery();
		initWheelstateQuery();
		update(false);
	}

	public static void init(Context context) {
		if (INSTANCE == null)
			INSTANCE = new UserQueryHelper(context);
	}

	public static UserQueryHelper get() {
		return INSTANCE;
	}

	private void update(boolean post) {
		mQuery = concatenateWhere(mCategoriesQuery, mWheelchairQuery);
		Log.d(TAG, "update query = " + mQuery);

		if (post) {
			Log.d(TAG, "update: posting UserQueryUpdateEvent on bus");
			mBus.post(new UserQueryUpdateEvent());
		}
	}

	public static String getUserQuery() {
		return mQuery;
	}

	private OnSharedPreferenceChangeListener prefsListener = new OnSharedPreferenceChangeListener() {

		@Override
		public void onSharedPreferenceChanged(SharedPreferences prefs,
				String key) {
			calcWheelchairStateQuery(prefs);
			update(true);
		}
	};

	private void initWheelstateQuery() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		prefs.registerOnSharedPreferenceChangeListener(prefsListener);
		calcWheelchairStateQuery(prefs);
	}

	private List<WheelchairState> getWheelchairStateFromPrefs(
			SharedPreferences prefs) {
		ArrayList<WheelchairState> list = new ArrayList<WheelchairState>();

		Map<WheelchairState, WheelchairAttributes> wsAttributes = SupportManager.wsAttributes;
		for (WheelchairState state : wsAttributes.keySet()) {
			boolean prefState = prefs.getBoolean(
					wsAttributes.get(state).prefsKey, true);
			if (prefState)
				list.add(state);
		}

		return list;
	}

	private void calcWheelchairStateQuery(SharedPreferences prefs) {
		Log.d(TAG, "calcWheelchairStateQuery starting");

		List<WheelchairState> list = getWheelchairStateFromPrefs(prefs);

		StringBuilder wheelchair = new StringBuilder("");

		for (WheelchairState state : list) {
			if (wheelchair.length() > 0)
				wheelchair.append(" OR wheelchair=");
			else
				wheelchair.append(" wheelchair=");
			wheelchair.append(state.getId());
		}

		if (wheelchair.toString().length() == 0) {
			for (WheelchairState state : WheelchairState.values()) {
				if (wheelchair.length() > 0)
					wheelchair.append(" AND NOT wheelchair=");
				else
					wheelchair.append(" NOT wheelchair=");
				wheelchair.append(state.getId());
			}
		}

		Log.d(TAG, "query result = " + wheelchair.toString());
		mWheelchairQuery = wheelchair.toString();
	}

	private void initCategoriesQuery() {
		Log.d(TAG, "initCategoriesQuery: starting");
		calcCategoriesQuery();
		mContext.getContentResolver().registerContentObserver(
				CategoriesContent.CONTENT_URI, false, categoriesObserver);

	}

	private ContentObserver categoriesObserver = new ContentObserver(
			new Handler()) {

		@Override
		public boolean deliverSelfNotifications() {
			return false;
		}

		@Override
		public void onChange(boolean selfChange) {
			Log.d(TAG, "categoriesObserver onChanged fired");
			calcCategoriesQuery();
			update(true);
		}

	};

	private void calcCategoriesQuery() {
		Cursor cursor = mContext.getContentResolver().query(
				CategoriesContent.CONTENT_URI, null, null, null, null);
		if (cursor == null)
			return;

		StringBuilder categories = new StringBuilder("");

		int selectedCount = 0;
		if (cursor.moveToFirst()) {
			do {
				int id = CategoriesContent.getCategoryId(cursor);
				if (CategoriesContent.getSelected(cursor)) {
					selectedCount++;
					if (categories.length() > 0)
						categories.append(" OR ").append(POIs.CATEGORY_ID)
								.append("=");
					else
						categories.append(POIs.CATEGORY_ID).append("=");
					categories.append(Integer.valueOf(id));

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

					categories.append(Integer.valueOf(id));

				} while (cursor.moveToNext());
			}

		}

		cursor.close();
		Log.d(TAG, "calcCategoriesQuery: result = " + categories.toString());
		mCategoriesQuery = categories.toString();
	}

	private static String concatenateWhere(String a, String b) {
		if (TextUtils.isEmpty(a)) {
			return b;
		}
		if (TextUtils.isEmpty(b)) {
			return a;
		}

		return "(" + a + ") AND (" + b + ")";
	}

}
