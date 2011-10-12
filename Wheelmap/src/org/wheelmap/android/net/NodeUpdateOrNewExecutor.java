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

package org.wheelmap.android.net;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.springframework.web.util.UriUtils;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;

import wheelmap.org.WheelchairState;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.NodeUpdateOrNewAllRequestBuilder;
import wheelmap.org.request.RequestBuilder;
import wheelmap.org.request.WheelchairUpdateRequestBuilder;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class NodeUpdateOrNewExecutor extends AbstractExecutor {
	private Context mContext;
	private Cursor mCursor;
	private static final String whereClauseToUpdate = "( "
			+ Wheelmap.POIs.UPDATE_TAG + " != ? ) AND ( "
			+ Wheelmap.POIs.UPDATE_TAG + " != ? )";
	private static final String[] whereValueToUpdate = new String[] {
			Integer.toString(Wheelmap.UPDATE_NO),
			Integer.toString(Wheelmap.UPDATE_PENDING) };

	public NodeUpdateOrNewExecutor(Context context, ContentResolver resolver) {
		super(resolver, null);
		mContext = context;
	}

	public void prepareContent() {
		mCursor = getResolver().query(Wheelmap.POIs.CONTENT_URI,
				Wheelmap.POIs.PROJECTION, whereClauseToUpdate,
				whereValueToUpdate, null);
		mCursor.moveToFirst();
	}

	public void execute() throws SyncServiceException {
		while (!mCursor.isAfterLast()) {
			int updateWay = POIHelper.getUpdateTag(mCursor);
			String editApiKey = getEditApiKey();
			if (editApiKey.length() == 0)
				throw new SyncServiceException(
						SyncServiceException.ERROR_AUTHORIZATION_ERROR,
						new RuntimeException("No apikey to edit available"));

			RequestBuilder requestBuilder = null;
			switch (updateWay) {
			case Wheelmap.UPDATE_WHEELCHAIR_STATE:
				requestBuilder = wheelchairUpdateRequestBuilder(editApiKey);
				break;
			case Wheelmap.UPDATE_ALL_NEW:
				requestBuilder = updateOrNewRequestBuilder(editApiKey);
				break;
			default:
				throw new SyncServiceException(
						SyncServiceException.ERROR_INTERNAL_ERROR,
						new RuntimeException(
								"Cant find matching RequestBuilder for update request"));
			}

			String request;
			try {
				request = UriUtils.encodeQuery(
						requestBuilder.buildRequestUri(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				throw new SyncServiceException(
						SyncServiceException.ERROR_INTERNAL_ERROR, e);
			}
			try {
				if (requestBuilder.getRequestType() == RequestBuilder.REQUEST_POST) {
					Log.d(TAG, "postRequest = *" + request + "*");
					mRequestProcessor.post(new URI(request), null);
				} else {
					Log.d(TAG, "putRequest = *" + request + "*");
					mRequestProcessor.put(new URI(request), null);
				}
			} catch (Exception e) {
				throw new SyncServiceException(
						SyncServiceException.ERROR_NETWORK_FAILURE, e);
			}

			mCursor.moveToNext();
		}

	}

	public void prepareDatabase() {
		copyAllUpdatedToPending();
		ContentValues values = new ContentValues();
		values.clear();
		values.put(Wheelmap.POIs.UPDATE_TAG, Wheelmap.UPDATE_NO);
		getResolver().update(Wheelmap.POIs.CONTENT_URI, values,
				whereClauseToUpdate, whereValueToUpdate);

	}

	private RequestBuilder wheelchairUpdateRequestBuilder(String apiKey) {
		long id = POIHelper.getWMId(mCursor);
		WheelchairState state = POIHelper.getWheelchair(mCursor);
		return new WheelchairUpdateRequestBuilder(SERVER, apiKey,
				AcceptType.JSON, (int) id, state);
	}

	private RequestBuilder updateOrNewRequestBuilder(String apiKey) {
		long id = POIHelper.getWMId(mCursor);

		boolean update = false;
		if (id != 0)
			update = true;

		String name = POIHelper.getName(mCursor);
		String category = POIHelper.getCategoryIdentifier(mCursor);
		String type = POIHelper.getNodeTypeIdentifier(mCursor);
		double latitude = POIHelper.getLatitude(mCursor);
		double longitude = POIHelper.getLongitude(mCursor);
		WheelchairState state = POIHelper.getWheelchair(mCursor);
		String comment = POIHelper.getComment(mCursor);
		String street = POIHelper.getStreet(mCursor);
		String housenumber = POIHelper.getHouseNumber(mCursor);
		String city = POIHelper.getCity(mCursor);
		String postcode = POIHelper.getPostcode(mCursor);
		String website = POIHelper.getWebsite(mCursor);
		String phone = POIHelper.getPhone(mCursor);

		return new NodeUpdateOrNewAllRequestBuilder(SERVER, apiKey,
				AcceptType.JSON, id, name, category, type, latitude, longitude,
				state, comment, street, housenumber, city, postcode, website,
				phone, update);
	}

	private String getEditApiKey() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		return prefs.getString(SyncService.PREFS_API_KEY, getApiKey());
	}

	private void copyAllUpdatedToPending() {
		long now = System.currentTimeMillis();

		Cursor c = getResolver().query(Wheelmap.POIs.CONTENT_URI,
				Wheelmap.POIs.PROJECTION, whereClauseToUpdate,
				whereValueToUpdate, null);
		c.moveToFirst();
		ContentValues values = new ContentValues();
		while (!c.isAfterLast()) {
			long wmId = POIHelper.getWMId(c);
			String whereClauseDest = " ( " + Wheelmap.POIs.UPDATE_TAG
					+ " = ? ) AND ( " + Wheelmap.POIs.WM_ID + " = ? )";
			String[] whereValuesDest = {
					Integer.toString(Wheelmap.UPDATE_PENDING),
					Long.toString(wmId) };

			values.put(Wheelmap.POIs.WM_ID, wmId);
			values.put(Wheelmap.POIs.WHEELCHAIR, POIHelper.getWheelchair(c)
					.getId());
			values.put(Wheelmap.POIs.UPDATE_TAG, Wheelmap.UPDATE_PENDING);
			values.put(Wheelmap.POIs.UPDATE_TIMESTAMP, now);
			insertOrUpdateContentValues(Wheelmap.POIs.CONTENT_URI,
					Wheelmap.POIs.PROJECTION, whereClauseDest, whereValuesDest,
					values);
			c.moveToNext();
		}
	}

	private void insertOrUpdateContentValues(Uri contentUri,
			String[] projection, String whereClause, String[] whereValues,
			ContentValues values) {
		Cursor c = getResolver().query(contentUri, projection, whereClause,
				whereValues, null);
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
