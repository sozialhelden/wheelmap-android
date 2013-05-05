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

import com.google.inject.Inject;
import org.wheelmap.android.modules.ICredentials;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.PrepareDatabaseHelper;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.service.SyncServiceException;

import wheelmap.org.WheelchairState;
import wheelmap.org.domain.Message;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.NodeUpdateOrNewAllRequestBuilder;
import wheelmap.org.request.RequestBuilder;
import wheelmap.org.request.WheelchairUpdateRequestBuilder;
import android.content.Context;
import android.database.Cursor;
import de.akquinet.android.androlog.Log;

public class NodeUpdateOrNewExecutor extends AbstractExecutor<Message> {
	private final static String TAG = NodeUpdateOrNewExecutor.class
			.getSimpleName();

	private static final int MAX_RETRY_COUNT = 3;
	private Cursor mCursor;

	@Inject
	private ICredentials mCredentials;

	public NodeUpdateOrNewExecutor(Context context) {
		super(context, null, Message.class, MAX_RETRY_COUNT);
	}

	public void prepareContent() {
		mCursor = PrepareDatabaseHelper.queryDirty(getResolver());

		if (mCursor == null)
			return;

		mCursor.moveToFirst();
	}

	public void execute() throws SyncServiceException {
		if (mCursor == null)
			throw new SyncServiceException(
					SyncServiceException.ERROR_INTERNAL_ERROR,
					new NullPointerException("Cursor is null"));

		while (!mCursor.isAfterLast()) {

			String editApiKey = getApiKey();
			if (editApiKey.length() == 0)
				throw new SyncServiceException(
						SyncServiceException.ERROR_AUTHORIZATION_ERROR,
						new RuntimeException("No apikey to edit available"));

			RequestBuilder requestBuilder = null;

			int dirtyTag = POIHelper.getDirtyTag(mCursor);
			switch (dirtyTag) {
			case POIs.DIRTY_STATE:
				requestBuilder = wheelchairUpdateRequestBuilder(editApiKey);
				break;
			case POIs.DIRTY_ALL:
				requestBuilder = updateOrNewRequestBuilder(editApiKey);
				break;
			default:
				throw new SyncServiceException(
						SyncServiceException.ERROR_INTERNAL_ERROR,
						new RuntimeException(
								"Cant find matching RequestBuilder for update request"));
			}

			executeRequest(requestBuilder);
			long id = POIHelper.getId(mCursor);
			PrepareDatabaseHelper.markDirtyAsClean(getResolver(), id);
			mCursor.moveToNext();
		}

		mCursor.close();
	}

	public void prepareDatabase() {
		PrepareDatabaseHelper.replayChangedCopies(getResolver());
	}

	private RequestBuilder wheelchairUpdateRequestBuilder(String apiKey) {
		String id = POIHelper.getWMId(mCursor);
		WheelchairState state = POIHelper.getWheelchair(mCursor);
		return new WheelchairUpdateRequestBuilder(getServer(), apiKey,
				AcceptType.JSON, id, state);
	}

	private RequestBuilder updateOrNewRequestBuilder(String apiKey) {
		String id = POIHelper.getWMId(mCursor);
		if (id != null)
			Log.d(TAG, "updateOrNewRequestBuilder: doing an update of id = "
					+ id);
		else
			Log.d(TAG, "updateOrNewRequestBuilder: creating a new poi");

		String name = POIHelper.getName(mCursor);
		SupportManager sm = WheelmapApp.getSupportManager();

		String categoryIdentifier = POIHelper.getCategoryIdentifier(mCursor);
		String nodeTypeIdentifier = POIHelper.getNodeTypeIdentifier(mCursor);

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

		return new NodeUpdateOrNewAllRequestBuilder(getServer(), apiKey,
				AcceptType.JSON, id, name, categoryIdentifier,
				nodeTypeIdentifier, latitude, longitude, state, comment,
				street, housenumber, city, postcode, website, phone);
	}

}
