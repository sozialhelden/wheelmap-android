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
import org.wheelmap.android.model.Support.CategoriesContent;
import org.wheelmap.android.model.Support.LocalesContent;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.model.Wheelmap.POIs;

import wheelmap.org.WheelchairState;
import wheelmap.org.domain.categories.Categories;
import wheelmap.org.domain.categories.Category;
import wheelmap.org.domain.locale.Locales;
import wheelmap.org.domain.node.Node;
import wheelmap.org.domain.node.Nodes;
import wheelmap.org.domain.node.SingleNode;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import de.akquinet.android.androlog.Log;

public class PrepareDatabaseHelper {
	private static final String TAG = PrepareDatabaseHelper.class
			.getSimpleName();

	private static final long TIME_TO_DELETE_FOR_PENDING = 10 * 60 * 1000;

	private PrepareDatabaseHelper() {

	}

	protected static void copyAllPendingDataToRetrievedData(
			ContentResolver resolver) {
		String whereClause = "( " + Wheelmap.POIs.UPDATE_TAG + " = ? ) OR ( "
				+ Wheelmap.POIs.UPDATE_TAG + " = ? )";
		String[] whereValues = new String[] {
				Integer.toString(Wheelmap.UPDATE_PENDING_STATE_ONLY),
				Integer.toString(Wheelmap.UPDATE_PENDING_FIELDS_ALL) };

		String whereClauseTarget = "( " + Wheelmap.POIs.WM_ID + " = ? )";
		String[] whereValuesTarget = new String[1];

		Cursor c = resolver.query(Wheelmap.POIs.CONTENT_URI,
				Wheelmap.POIs.PROJECTION, whereClause, whereValues, null);
		if (c == null)
			return;

		c.moveToFirst();
		ContentValues values = new ContentValues();
		while (!c.isAfterLast()) {
			String wmId = POIHelper.getWMId(c);

			values.clear();
			int updateTag = POIHelper.getUpdateTag(c);
			if (updateTag == Wheelmap.UPDATE_PENDING_STATE_ONLY) {
				int wheelchairState = POIHelper.getWheelchair(c).getId();
				values.put(Wheelmap.POIs.WHEELCHAIR, wheelchairState);
			} else if (updateTag == Wheelmap.UPDATE_PENDING_FIELDS_ALL) {
				POIHelper.copyItemToValues(c, values);

			} else
				continue;

			whereValuesTarget[0] = wmId;
			resolver.update(Wheelmap.POIs.CONTENT_URI, values,
					whereClauseTarget, whereValuesTarget);
			c.moveToNext();
		}

		c.close();
	}

	protected static void copyNodeToValues(Node node, ContentValues values) {
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

	protected static void deleteAllOldPending(ContentResolver resolver) {
		long now = System.currentTimeMillis();
		String whereClause = "(( " + Wheelmap.POIs.UPDATE_TAG + " == ? ) OR ( "
				+ Wheelmap.POIs.UPDATE_TAG + " == ? )) AND ( "
				+ Wheelmap.POIs.UPDATE_TIMESTAMP + " < ?)";
		String[] whereValues = {
				Integer.toString(Wheelmap.UPDATE_PENDING_STATE_ONLY),
				Integer.toString(Wheelmap.UPDATE_PENDING_FIELDS_ALL),
				Long.toString(now - TIME_TO_DELETE_FOR_PENDING) };

		Uri uri = Wheelmap.POIs.CONTENT_URI
				.buildUpon()
				.appendQueryParameter(Wheelmap.QUERY_DELETE_NOTIFY_PARAM,
						"false").build();

		resolver.delete(uri, whereClause, whereValues);
	}

	public static void insertOrUpdateContentValues(ContentResolver resolver,
			Uri contentUri, String[] projection, String whereClause,
			String[] whereValues, ContentValues values) {
		Cursor c = resolver.query(contentUri, projection, whereClause,
				whereValues, null);
		if (c == null)
			return;

		int cursorCount = c.getCount();
		if (cursorCount == 0)
			resolver.insert(contentUri, values);
		else if (cursorCount > 0) {
			resolver.update(contentUri, values, whereClause, whereValues);
		} else {
			// do nothing, as more than one file would be updated
		}
		c.close();
	}

	protected static void deleteRetrievedData(ContentResolver resolver) {
		String whereClause = "( " + Wheelmap.POIs.UPDATE_TAG + " = ? )";
		String[] whereValues = new String[] { String
				.valueOf(Wheelmap.UPDATE_NO) };
		Uri uri = Wheelmap.POIs.CONTENT_URI
				.buildUpon()
				.appendQueryParameter(Wheelmap.QUERY_DELETE_NOTIFY_PARAM,
						"false").build();
		resolver.delete(uri, whereClause, whereValues);
	}

	protected static void insert(ContentResolver resolver, SingleNode node) {
		ContentValues values = new ContentValues();
		PrepareDatabaseHelper.copyNodeToValues(node.getNode(), values);
		String whereClause = "( " + POIs.WM_ID + " = ? )";
		String whereValues[] = { node.getNode().getId().toString() };

		PrepareDatabaseHelper.insertOrUpdateContentValues(resolver,
				Wheelmap.POIs.CONTENT_URI, Wheelmap.POIs.PROJECTION,
				whereClause, whereValues, values);
	}

	protected static void bulkInsert(ContentResolver resolver, Nodes nodes) {
		int size = nodes.getMeta().getItemCount().intValue();
		ContentValues[] contentValuesArray = new ContentValues[size];
		for (int i = 0; i < size; i++) {
			ContentValues values = new ContentValues();
			copyNodeToValues(nodes.getNodes().get(i), values);

			contentValuesArray[i] = values;
		}
		int count = resolver.bulkInsert(Wheelmap.POIs.CONTENT_URI,
				contentValuesArray);
		Log.d(TAG, "Inserted records count = " + count);
	}

	protected static void bulkInsert(ContentResolver resolver,
			Categories categories) {
		int size = categories.getCategories().size();
		ContentValues[] contentValuesArray = new ContentValues[size];
		int i;
		for (i = 0; i < size; i++) {
			ContentValues values = new ContentValues();
			copyCategoryToValues(categories.getCategories().get(i), values);
			contentValuesArray[i] = values;
		}

		resolver.bulkInsert(CategoriesContent.CONTENT_URI, contentValuesArray);
	}

	private static void copyCategoryToValues(Category category,
			ContentValues values) {
		values.clear();
		values.put(CategoriesContent.CATEGORY_ID, category.getId().intValue());
		values.put(CategoriesContent.LOCALIZED_NAME,
				category.getLocalizedName());
		values.put(CategoriesContent.IDENTIFIER, category.getIdentifier());
		values.put(CategoriesContent.SELECTED, CategoriesContent.SELECTED_YES);
	}

	protected static void bulkInsert(ContentResolver resolver, Locales locales) {
		int size = locales.getLocales().size();

		ContentValues[] contentValuesArray = new ContentValues[size];
		int i = 0;
		for (String localeId : locales.getLocales().keySet()) {
			ContentValues values = new ContentValues();
			copyLocaleToValues(localeId, locales.getLocales().get(localeId),
					values);
			contentValuesArray[i] = values;
			i++;
		}

		resolver.bulkInsert(LocalesContent.CONTENT_URI, contentValuesArray);
	}

	private static void copyLocaleToValues(String localeId, String localeName,
			ContentValues values) {
		values.clear();
		values.put(LocalesContent.LOCALE_ID, localeId);
		values.put(LocalesContent.LOCALIZED_NAME, localeName);
	}

	private static final String whereClauseToUpdate = "( "
			+ Wheelmap.POIs.UPDATE_TAG + " = ? ) AND ( "
			+ Wheelmap.POIs.UPDATE_TAG + " = ? ) ";
	private static final String[] whereValueToUpdate = new String[] {
			Integer.toString(Wheelmap.UPDATE_WHEELCHAIR_STATE),
			Integer.toString(Wheelmap.UPDATE_ALL_FIELDS) };

	protected static void copyAllUpdatedToPending(ContentResolver resolver) {
		long now = System.currentTimeMillis();

		Cursor c = resolver.query(Wheelmap.POIs.CONTENT_URI,
				Wheelmap.POIs.PROJECTION, whereClauseToUpdate,
				whereValueToUpdate, null);
		if (c == null)
			return;

		c.moveToFirst();
		ContentValues values = new ContentValues();
		while (!c.isAfterLast()) {
			String wmId = POIHelper.getWMId(c);
			String whereClauseDest = " ( " + Wheelmap.POIs.UPDATE_TAG
					+ " = ? ) AND ( " + Wheelmap.POIs.WM_ID + " = ? )";
			String[] whereValuesDest = new String[2];
			whereValuesDest[1] = wmId;

			int updateTag = POIHelper.getUpdateTag(c);
			values.clear();

			if (updateTag == Wheelmap.UPDATE_WHEELCHAIR_STATE) {
				preparePendingWheelchairUpdate(c, values, whereValuesDest);
			} else if (updateTag == Wheelmap.UPDATE_ALL_FIELDS) {
				preparePendingAllUpdate(c, values, whereValuesDest);
			}

			values.put(Wheelmap.POIs.UPDATE_TIMESTAMP, now);
			insertOrUpdateContentValues(resolver, Wheelmap.POIs.CONTENT_URI,
					Wheelmap.POIs.PROJECTION, whereClauseDest, whereValuesDest,
					values);

			c.moveToNext();
		}

		c.close();
	}

	private static void preparePendingWheelchairUpdate(Cursor c,
			ContentValues values, String[] whereValues) {
		whereValues[0] = Integer.toString(Wheelmap.UPDATE_PENDING_STATE_ONLY);
		values.put(Wheelmap.POIs.WM_ID, POIHelper.getWMId(c));
		values.put(Wheelmap.POIs.WHEELCHAIR, POIHelper.getWheelchair(c).getId());
		values.put(Wheelmap.POIs.UPDATE_TAG, Wheelmap.UPDATE_PENDING_STATE_ONLY);
	}

	private static void preparePendingAllUpdate(Cursor c, ContentValues values,
			String[] whereValues) {
		whereValues[0] = Integer.toString(Wheelmap.UPDATE_PENDING_FIELDS_ALL);
		POIHelper.copyItemToValues(c, values);
		values.put(Wheelmap.POIs.UPDATE_TAG, Wheelmap.UPDATE_PENDING_FIELDS_ALL);
	}

	protected static Cursor queryToUpdate(ContentResolver resolver) {
		return resolver.query(Wheelmap.POIs.CONTENT_URI,
				Wheelmap.POIs.PROJECTION, whereClauseToUpdate,
				whereValueToUpdate, null);
	}

	protected static void resetUpdateTagOfPending(ContentResolver resolver) {
		ContentValues values = new ContentValues();
		values.put(Wheelmap.POIs.UPDATE_TAG, Wheelmap.UPDATE_NO);
		resolver.update(Wheelmap.POIs.CONTENT_URI, values, whereClauseToUpdate,
				whereValueToUpdate);
	}

	private static String whereClauseTemporaryStore = "( "
			+ Wheelmap.POIs.UPDATE_TAG + " = ? )";
	private static String[] whereValuesTemporaryStore = new String[] { Integer
			.toString(Wheelmap.UPDATE_TEMPORARY_STORE) };

	public static void storeTemporary(ContentResolver resolver,
			ContentValues values) {
		values.put(Wheelmap.POIs.UPDATE_TAG, Wheelmap.UPDATE_TEMPORARY_STORE);
		insertOrUpdateContentValues(resolver, Wheelmap.POIs.CONTENT_URI,
				Wheelmap.POIs.PROJECTION, whereClauseTemporaryStore,
				whereValuesTemporaryStore, values);
	}

}
