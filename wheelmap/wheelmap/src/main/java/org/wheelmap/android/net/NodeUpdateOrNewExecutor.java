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

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriUtils;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.UserCredentials;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.service.SyncServiceException;

import wheelmap.org.WheelchairState;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.NodeUpdateOrNewAllRequestBuilder;
import wheelmap.org.request.RequestBuilder;
import wheelmap.org.request.WheelchairUpdateRequestBuilder;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class NodeUpdateOrNewExecutor extends AbstractExecutor {
	private Context mContext;
	private Cursor mCursor;
	private static final String whereClauseToUpdate = "( "
			+ Wheelmap.POIs.UPDATE_TAG + " != ? ) AND ( "
			+ Wheelmap.POIs.UPDATE_TAG + " != ? ) AND ( "
			+ Wheelmap.POIs.UPDATE_TAG + " != ? )";
	private static final String[] whereValueToUpdate = new String[] {
			Integer.toString(Wheelmap.UPDATE_NO),
			Integer.toString(Wheelmap.UPDATE_PENDING_STATE_ONLY),
			Integer.toString(Wheelmap.UPDATE_PENDING_FIELDS_ALL) };

	public NodeUpdateOrNewExecutor(Context context, ContentResolver resolver) {
		super(resolver, null);
		mContext = context;
	}

	public void prepareContent() {
		mCursor = getResolver().query(Wheelmap.POIs.CONTENT_URI,
				Wheelmap.POIs.PROJECTION, whereClauseToUpdate,
				whereValueToUpdate, null);
		if ( mCursor == null )
			return;
		
		mCursor.moveToFirst();
	}

	public void execute() throws SyncServiceException {
		if (mCursor == null)
			throw new SyncServiceException(
					SyncServiceException.ERROR_INTERNAL_ERROR,
					new NullPointerException("Cursor is null"));

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
			case Wheelmap.UPDATE_ALL_FIELDS:
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
					mRequestProcessor
							.post(new URI(request), null, String.class);
				} else {
					Log.d(TAG, "putRequest = *" + request + "*");
					mRequestProcessor.put(new URI(request), null);
				}
			} catch (HttpClientErrorException e) {
				HttpStatus status = e.getStatusCode();
				if (status.value() == statusAuthRequired) {
					Log.d(TAG, "authorization required");
					throw new SyncServiceException(
							SyncServiceException.ERROR_AUTHORIZATION_REQUIRED,
							e);
				} else if (status.value() == statusRequestForbidden) {
					Log.d(TAG, "request forbidden");
					throw new SyncServiceException(
							SyncServiceException.ERROR_REQUEST_FORBIDDEN, e);
				}
			} catch (Exception e) {
				throw new SyncServiceException(
						SyncServiceException.ERROR_NETWORK_FAILURE, e);
			}

			mCursor.moveToNext();
		}

		mCursor.close();

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
		String id = POIHelper.getWMId(mCursor);
		WheelchairState state = POIHelper.getWheelchair(mCursor);
		return new WheelchairUpdateRequestBuilder(SERVER, apiKey,
				AcceptType.JSON, id, state);
	}

	private RequestBuilder updateOrNewRequestBuilder(String apiKey) {
		String id = POIHelper.getWMId(mCursor);

		boolean update = false;
		if ( id.equals( "0" ))
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

		return new NodeUpdateOrNewAllRequestBuilder(SERVER_STAGING, apiKey,
				AcceptType.JSON, id, name, category, type, latitude, longitude,
				state, comment, street, housenumber, city, postcode, website,
				phone, update);
	}

	private String getEditApiKey() {
		UserCredentials credentials = new UserCredentials(mContext);
		return credentials.getApiKey();
	}

	private void copyAllUpdatedToPending() {
		long now = System.currentTimeMillis();

		PrepareDatabaseHelper prebDbHelp = new PrepareDatabaseHelper(
				getResolver());

		Cursor c = getResolver().query(Wheelmap.POIs.CONTENT_URI,
				Wheelmap.POIs.PROJECTION, whereClauseToUpdate,
				whereValueToUpdate, null);
		if ( c == null )
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
			prebDbHelp.insertOrUpdateContentValues(Wheelmap.POIs.CONTENT_URI,
					Wheelmap.POIs.PROJECTION, whereClauseDest, whereValuesDest,
					values);

			c.moveToNext();
		}

		c.close();
	}

	private void preparePendingWheelchairUpdate(Cursor c, ContentValues values,
			String[] whereValues) {
		whereValues[0] = Integer.toString(Wheelmap.UPDATE_PENDING_STATE_ONLY);
		values.put(Wheelmap.POIs.WM_ID, POIHelper.getWMId(c));
		values.put(Wheelmap.POIs.WHEELCHAIR, POIHelper.getWheelchair(c).getId());
		values.put(Wheelmap.POIs.UPDATE_TAG, Wheelmap.UPDATE_PENDING_STATE_ONLY);
	}

	private void preparePendingAllUpdate(Cursor c, ContentValues values,
			String[] whereValues) {
		whereValues[0] = Integer.toString(Wheelmap.UPDATE_PENDING_FIELDS_ALL);

		POIHelper.copyItemToValues(c, values);

		values.put(Wheelmap.POIs.UPDATE_TAG, Wheelmap.UPDATE_ALL_FIELDS);
	}

}
