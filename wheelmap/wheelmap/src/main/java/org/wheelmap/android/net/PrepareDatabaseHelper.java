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
package org.wheelmap.android.net;

import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.Wheelmap;

import wheelmap.org.WheelchairState;
import wheelmap.org.domain.node.Node;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class PrepareDatabaseHelper {
	private static final String TAG = "PrepareDatabase";

	private static final long TIME_TO_DELETE_FOR_PENDING = 10 * 60 * 1000;

	private final ContentResolver mResolver;

	PrepareDatabaseHelper(ContentResolver resolver) {
		mResolver = resolver;
	}

	protected ContentResolver getResolver() {
		return mResolver;
	}

	protected void copyAllPendingDataToRetrievedData() {
		String whereClause = "( " + Wheelmap.POIs.UPDATE_TAG + " = ? ) OR ( "
				+ Wheelmap.POIs.UPDATE_TAG + " = ? )";
		String[] whereValues = new String[] {
				Integer.toString(Wheelmap.UPDATE_PENDING_STATE_ONLY),
				Integer.toString(Wheelmap.UPDATE_PENDING_FIELDS_ALL) };

		String whereClauseTarget = "( " + Wheelmap.POIs.WM_ID + " = ? )";
		String[] whereValuesTarget = new String[1];

		Cursor c = getResolver().query(Wheelmap.POIs.CONTENT_URI,
				Wheelmap.POIs.PROJECTION, whereClause, whereValues, null);
		if (c == null)
			return;

		c.moveToFirst();
		ContentValues values = new ContentValues();
		while (!c.isAfterLast()) {
			String wmId = POIHelper.getWMId(c);

			values.clear();
			int updateTag = POIHelper.getUpdateTag(c);
			if (updateTag == Wheelmap.UPDATE_PENDING_STATE_ONLY)
				copyPendingWheelchairState(c, values);
			else if (updateTag == Wheelmap.UPDATE_PENDING_FIELDS_ALL)
				copyPendingAllValues(c, values);
			else
				continue;

			whereValuesTarget[0] = wmId;
			getResolver().update(Wheelmap.POIs.CONTENT_URI, values,
					whereClauseTarget, whereValuesTarget);
			c.moveToNext();
		}

		c.close();
	}

	private void copyPendingWheelchairState(Cursor c, ContentValues values) {
		int wheelchairState = POIHelper.getWheelchair(c).getId();
		values.put(Wheelmap.POIs.WHEELCHAIR, wheelchairState);
	}

	private void copyPendingAllValues(Cursor c, ContentValues values) {
		POIHelper.copyItemToValues(c, values);
	}

	protected void copyNodeToValues(Node node, ContentValues values) {
		values.clear();
		values.put(Wheelmap.POIs.WM_ID, node.getId().longValue());
		values.put(Wheelmap.POIs.NAME, node.getName());
		values.put(Wheelmap.POIs.COORD_LAT,
				Math.ceil(node.getLat().doubleValue() * 1E6));
		values.put(Wheelmap.POIs.COORD_LON,
				Math.ceil(node.getLon().doubleValue() * 1E6));
		values.put(Wheelmap.POIs.STREET, node.getStreet());
		values.put(Wheelmap.POIs.HOUSE_NUM, node.getHousenumber());
		values.put(Wheelmap.POIs.POSTCODE, node.getPostcode());
		values.put(Wheelmap.POIs.CITY, node.getCity());
		values.put(Wheelmap.POIs.PHONE, node.getPhone());
		values.put(Wheelmap.POIs.WEBSITE, node.getWebsite());
		values.put(Wheelmap.POIs.WHEELCHAIR,
				WheelchairState.myValueOf(node.getWheelchair()).getId());
		values.put(Wheelmap.POIs.WHEELCHAIR_DESC,
				node.getWheelchairDescription());
		values.put(Wheelmap.POIs.CATEGORY_ID, node.getCategory().getId()
				.intValue());
		values.put(Wheelmap.POIs.CATEGORY_IDENTIFIER, node.getCategory()
				.getIdentifier());
		values.put(Wheelmap.POIs.NODETYPE_ID, node.getNodeType().getId()
				.intValue());
		values.put(Wheelmap.POIs.NODETYPE_IDENTIFIER, node.getNodeType()
				.getIdentifier());
		values.put(Wheelmap.POIs.UPDATE_TAG, Wheelmap.UPDATE_NO);
	}

	protected void deleteAllOldPending() {
		long now = System.currentTimeMillis();
		String whereClause = "(( " + Wheelmap.POIs.UPDATE_TAG + " == ? ) OR ( "
				+ Wheelmap.POIs.UPDATE_TAG + " == ? )) AND ( "
				+ Wheelmap.POIs.UPDATE_TIMESTAMP + " < ?)";
		String[] whereValues = {
				Integer.toString(Wheelmap.UPDATE_PENDING_STATE_ONLY),
				Integer.toString(Wheelmap.UPDATE_PENDING_FIELDS_ALL),
				Long.toString(now - TIME_TO_DELETE_FOR_PENDING) };

		Uri uri = Wheelmap.POIs.CONTENT_URI.buildUpon()
				.appendQueryParameter(Wheelmap.QUERY_DELETE_NOTIFY_PARAM, "false")
				.build();

		getResolver().delete(uri, whereClause, whereValues);
	}

	protected void insertOrUpdateContentValues(Uri contentUri,
			String[] projection, String whereClause, String[] whereValues,
			ContentValues values) {
		Cursor c = getResolver().query(contentUri, projection, whereClause,
				whereValues, null);
		if (c == null)
			return;

		int cursorCount = c.getCount();
		if (cursorCount == 0)
			getResolver().insert(contentUri, values);
		else if (cursorCount > 0) {
			getResolver().update(contentUri, values, whereClause, whereValues);
		} else {
			// do nothing, as more than one file would be updated
		}
		c.close();
	}

}
