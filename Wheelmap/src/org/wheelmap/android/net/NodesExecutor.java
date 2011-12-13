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

import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.utils.GeocoordinatesMath;
import org.wheelmap.android.utils.ParceableBoundingBox;

import wheelmap.org.BoundingBox;
import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;
import wheelmap.org.WheelchairState;
import wheelmap.org.domain.node.Node;
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
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

public class NodesExecutor extends BaseRetrieveExecutor<Nodes> implements
		IExecutor {

	private BoundingBox mBoundingBox = null;
	private int mCategory = -1;
	private int mNodeType = -1;
	private String mSearchTerm = null;

	private static final int MAX_PAGES_TO_RETRIEVE = 2;
	private static final long TIME_TO_DELETE_FOR_PENDING = 10 * 60 * 1000;

	public NodesExecutor(ContentResolver resolver, Bundle bundle) {
		super(resolver, bundle, Nodes.class);
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
	}

	@Override
	public void execute() throws SyncServiceException {
		final long startRemote = System.currentTimeMillis();
		BaseNodesRequestBuilder requestBuilder;
		if (mCategory != -1) {
			requestBuilder = new CategoryNodesRequestBuilder(SERVER,
					getApiKey(), AcceptType.JSON, mCategory);
		} else if (mNodeType != -1) {
			requestBuilder = new NodeTypeNodesRequestBuilder(SERVER,
					getApiKey(), AcceptType.JSON, mNodeType);
		} else if (mSearchTerm != null) {
			requestBuilder = new SearchNodesRequestBuilder(SERVER, getApiKey(),
					AcceptType.JSON, mSearchTerm);
		} else {
			requestBuilder = new NodesRequestBuilder(SERVER, getApiKey(),
					AcceptType.JSON);
		}

		requestBuilder.paging(new Paging(DEFAULT_TEST_PAGE_SIZE)).boundingBox(
				mBoundingBox);

		clearTempStore();
		retrieveMaxNPages(requestBuilder, MAX_PAGES_TO_RETRIEVE);

		Log.d(TAG, "remote sync took "
				+ (System.currentTimeMillis() - startRemote) + "ms");
	}

	@Override
	public void prepareDatabase() {
		deleteRetrievedData();
		deleteAllOldPending();

		for (Nodes nodes : getTempStore()) {
			bulkInsert(nodes);
		}
		copyAllPendingDataToRetrievedData();
		clearTempStore();
	}

	private void deleteRetrievedData() {
		String whereClause = "( " + Wheelmap.POIs.UPDATE_TAG + " = ? )";
		String[] whereValues = new String[] { String
				.valueOf(Wheelmap.UPDATE_NO) };
		getResolver().delete(Wheelmap.POIs.CONTENT_URI, whereClause,
				whereValues);
	}

	private void copyAllPendingDataToRetrievedData() {
		String whereClause = "( " + Wheelmap.POIs.UPDATE_TAG + " = ? ) OR ( "
				+ Wheelmap.POIs.UPDATE_TAG + " = ? )";
		String[] whereValues = new String[] {
				Integer.toString(Wheelmap.UPDATE_PENDING_STATE_ONLY),
				Integer.toString(Wheelmap.UPDATE_PENDING_FIELDS_ALL) };

		String whereClauseTarget = "( " + Wheelmap.POIs.WM_ID + " = ? )";
		String[] whereValuesTarget = new String[1];

		Cursor c = getResolver().query(Wheelmap.POIs.CONTENT_URI,
				Wheelmap.POIs.PROJECTION, whereClause, whereValues, null);
		c.moveToFirst();
		ContentValues values = new ContentValues();
		while (!c.isAfterLast()) {
			long wmId = POIHelper.getWMId(c);

			values.clear();
			int updateTag = POIHelper.getUpdateTag(c);
			if (updateTag == Wheelmap.UPDATE_PENDING_STATE_ONLY)
				copyPendingWheelchairState(c, values);
			else if (updateTag == Wheelmap.UPDATE_PENDING_FIELDS_ALL)
				copyPendingAllValues(c, values);
			else
				continue;

			whereValuesTarget[0] = Long.toString(wmId);
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

	private void bulkInsert(Nodes nodes) {
		int size = nodes.getMeta().getItemCount().intValue();
		ContentValues[] contentValuesArray = new ContentValues[size];
		for (int i = 0; i < size; i++) {
			ContentValues values = new ContentValues();
			copyNodeToValues(nodes.getNodes().get(i), values);

			contentValuesArray[i] = values;
		}
		int count = getResolver().bulkInsert(Wheelmap.POIs.CONTENT_URI,
				contentValuesArray);
		Log.d(TAG, "Inserted records count = " + count);
	}

	private void copyNodeToValues(Node node, ContentValues values) {
		values.clear();
		values.put(Wheelmap.POIs.WM_ID, node.getId().intValue());
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

	private void deleteAllOldPending() {
		long now = System.currentTimeMillis();
		String whereClause = "(( " + Wheelmap.POIs.UPDATE_TAG + " == ? ) OR ( "
				+ Wheelmap.POIs.UPDATE_TAG + " == ? )) AND ( "
				+ Wheelmap.POIs.UPDATE_TIMESTAMP + " < ?)";
		String[] whereValues = {
				Integer.toString(Wheelmap.UPDATE_PENDING_STATE_ONLY),
				Integer.toString(Wheelmap.UPDATE_PENDING_FIELDS_ALL),
				Long.toString(now - TIME_TO_DELETE_FOR_PENDING) };

		getResolver().delete(Wheelmap.POIs.CONTENT_URI, whereClause,
				whereValues);
	}
}
