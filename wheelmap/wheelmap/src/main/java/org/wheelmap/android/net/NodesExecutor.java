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

import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.utils.GeocoordinatesMath;
import org.wheelmap.android.utils.ParceableBoundingBox;

import wheelmap.org.BoundingBox;
import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;
import wheelmap.org.WheelchairState;
import wheelmap.org.domain.node.Nodes;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.BaseNodesRequestBuilder;
import wheelmap.org.request.CategoryNodesRequestBuilder;
import wheelmap.org.request.NodeTypeNodesRequestBuilder;
import wheelmap.org.request.NodesRequestBuilder;
import wheelmap.org.request.Paging;
import wheelmap.org.request.SearchNodesRequestBuilder;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class NodesExecutor extends BaseRetrieveExecutor<Nodes> implements
		IExecutor {
	private static final int MAX_PAGES_TO_RETRIEVE = 2;

	private BoundingBox mBoundingBox = null;
	private int mCategory = -1;
	private int mNodeType = -1;
	private String mSearchTerm = null;
	private WheelchairState mWheelchairState = null;
	private PrepareDatabaseHelper prepDbHelper;

	public NodesExecutor(ContentResolver resolver, Bundle bundle) {
		super(resolver, bundle, Nodes.class);
		prepDbHelper = new PrepareDatabaseHelper(resolver);
	}

	@Override
	public void prepareContent() {
		if (getBundle().containsKey(SyncService.EXTRA_BOUNDING_BOX)) {
			ParceableBoundingBox parcBoundingBox = (ParceableBoundingBox) getBundle()
					.getSerializable(SyncService.EXTRA_BOUNDING_BOX);
			mBoundingBox = parcBoundingBox.toBoundingBox();
			// Log.d(TAG,
			// "retrieving with bounding box: "
			// + parcBoundingBox.toString());
		} else if (getBundle().containsKey(SyncService.EXTRA_LOCATION)) {
			float distance = getBundle().getFloat(
					SyncService.EXTRA_DISTANCE_LIMIT);
			Location location = (Location) getBundle().getParcelable(
					SyncService.EXTRA_LOCATION);
			mBoundingBox = GeocoordinatesMath.calculateBoundingBox(
					new Wgs84GeoCoordinates(location.getLongitude(), location
							.getLatitude()), distance);
			// Log.d(TAG,
			// "retrieving with current location = ("
			// + location.getLongitude() + ","
			// + location.getLatitude() + ") and distance = "
			// + distance);
		}

		if (getBundle().containsKey(SyncService.EXTRA_CATEGORY)) {
			mCategory = getBundle().getInt(SyncService.EXTRA_CATEGORY);
		} else if (getBundle().containsKey(SyncService.EXTRA_NODETYPE)) {
			mNodeType = getBundle().getInt(SyncService.EXTRA_NODETYPE);
		}

		if (getBundle().containsKey(SearchManager.QUERY)) {
			mSearchTerm = getBundle().getString(SearchManager.QUERY);
		}

		if (getBundle().containsKey(SyncService.EXTRA_WHEELCHAIR_STATE))
			mWheelchairState = WheelchairState.valueOf(getBundle().getInt(
					SyncService.EXTRA_WHEELCHAIR_STATE));
	}

	@Override
	public void execute() throws SyncServiceException {
		final long startRemote = System.currentTimeMillis();
		BaseNodesRequestBuilder requestBuilder;
		if (mCategory != -1) {
			requestBuilder = new CategoryNodesRequestBuilder(SERVER,
					getApiKey(), AcceptType.JSON, mCategory, mSearchTerm);
		} else if (mNodeType != -1) {
			requestBuilder = new NodeTypeNodesRequestBuilder(SERVER,
					getApiKey(), AcceptType.JSON, mNodeType, mSearchTerm);
		} else if (mSearchTerm != null) {
			requestBuilder = new SearchNodesRequestBuilder(SERVER, getApiKey(),
					AcceptType.JSON, mSearchTerm);
		} else {
			requestBuilder = new NodesRequestBuilder(SERVER, getApiKey(),
					AcceptType.JSON);
		}

		requestBuilder.paging(new Paging(DEFAULT_TEST_PAGE_SIZE)).boundingBox(
				mBoundingBox);
		requestBuilder.wheelchairState(mWheelchairState);
		clearTempStore();
		retrieveMaxNPages(requestBuilder, MAX_PAGES_TO_RETRIEVE);

		Log.d(TAG, "remote sync took "
				+ (System.currentTimeMillis() - startRemote) + "ms");
	}

	@Override
	public void prepareDatabase() {
		deleteRetrievedData();

		prepDbHelper.deleteAllOldPending();
		for (Nodes nodes : getTempStore()) {
			bulkInsert(nodes);
		}
		prepDbHelper.copyAllPendingDataToRetrievedData();
		clearTempStore();
	}

	private void deleteRetrievedData() {
		String whereClause = "( " + Wheelmap.POIs.UPDATE_TAG + " = ? )";
		String[] whereValues = new String[] { String
				.valueOf(Wheelmap.UPDATE_NO) };
		Uri uri = Wheelmap.POIs.CONTENT_URI.buildUpon()
				.appendQueryParameter(Wheelmap.QUERY_DELETE_NOTIFY_PARAM, "false")
				.build();
		getResolver().delete(uri, whereClause, whereValues);
	}

	private void bulkInsert(Nodes nodes) {
		int size = nodes.getMeta().getItemCount().intValue();
		ContentValues[] contentValuesArray = new ContentValues[size];
		for (int i = 0; i < size; i++) {
			ContentValues values = new ContentValues();
			prepDbHelper.copyNodeToValues(nodes.getNodes().get(i), values);

			contentValuesArray[i] = values;
		}
		int count = getResolver().bulkInsert(Wheelmap.POIs.CONTENT_URI,
				contentValuesArray);
		Log.d(TAG, "Inserted records count = " + count);
	}
}
