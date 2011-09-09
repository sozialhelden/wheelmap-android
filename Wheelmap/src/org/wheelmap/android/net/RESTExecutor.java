package org.wheelmap.android.net;

import java.net.URI;
import java.net.URISyntaxException;

import org.wheelmap.android.model.Wheelmap;

import wheelmap.org.BoundingBox;
import wheelmap.org.WheelMapException;
import wheelmap.org.WheelchairState;
import wheelmap.org.domain.node.json.Meta;
import wheelmap.org.domain.node.json.Node;
import wheelmap.org.domain.node.json.Nodes;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.NodesRequestBuilder;
import wheelmap.org.request.Paging;
import wheelmap.org.request.RequestProcessor;
import wheelmap.org.util.XmlSupport;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.util.Log;

public class RESTExecutor {
	private static final String TAG = "RESTExecutor";

	private ContentResolver mResolver;
	private static final String SERVER = "staging.wheelmap.org";
	private static final String API_KEY = "9NryQWfDWgIebZIdqWiK";
	public static final int DEFAULT_TEST_PAGE_SIZE = 500;

	private static RequestProcessor mRequestProcessor = new RequestProcessor();

	public RESTExecutor(ContentResolver resolver) {
		mResolver = resolver;
	}

	public void retrieveSinglePage(BoundingBox boundingBox,
			WheelchairState wheelchairState) throws RemoteException, OperationApplicationException {
		// get stuff from server
		final NodesRequestBuilder requestBuilder = new NodesRequestBuilder(
				SERVER, API_KEY, AcceptType.JSON);

		// 1. maxi nodes
		requestBuilder.paging(new Paging(DEFAULT_TEST_PAGE_SIZE))
				.boundingBox(boundingBox);
		requestBuilder.wheelchairState(wheelchairState);

		// here just delete whole table
		mResolver.delete(Wheelmap.POIs.CONTENT_URI, null, null);
		Meta m = executeSingleRequest(requestBuilder);
		Log.d(TAG, "totalItemsCount " + m.getItemCountTotal());
	}

	public void retrieveAllPages(BoundingBox boundingBox,
			WheelchairState wheelchairState) throws RemoteException, OperationApplicationException {
		// here just delete all pois. Perhaps something more intelligent may
		// come up
		mResolver.delete(Wheelmap.POIs.CONTENT_URI, null, null);

		final NodesRequestBuilder requestBuilder = new NodesRequestBuilder(
				SERVER, API_KEY, AcceptType.JSON);

		// Server seems to count from 1...
		Paging page = new Paging(DEFAULT_TEST_PAGE_SIZE, 1);
		requestBuilder.boundingBox(boundingBox).paging(page)
				.wheelchairState(wheelchairState);

		Meta m = executeSingleRequest(requestBuilder);
		Log.d(TAG, "totalItemsCount " + m.getItemCountTotal());

		int numOfPages = m.getNumPages().intValue();

		int crrPage;
		for (crrPage = 2; crrPage <= numOfPages; crrPage++) {
			page.setPage(crrPage);
			executeSingleRequest(requestBuilder);
		}

	}

	public Meta executeSingleRequest(NodesRequestBuilder requestBuilder) throws RemoteException, OperationApplicationException {
		String getRequest = requestBuilder.buildRequestUri();
		Log.d(TAG, "getRequest " + getRequest);
		long startTime = System.currentTimeMillis();

		Nodes nodes = retrieveNumberOfHits(getRequest);

		long retrieveTime = System.currentTimeMillis();
		Log.d(TAG, "retrieveTime = " + (retrieveTime - startTime) / 1000f);
		bulkInsert( nodes );
		long insertTime = System.currentTimeMillis();
		Log.d(TAG, "insertTime = " + (insertTime - retrieveTime) / 1000f);

		return nodes.getMeta();
	}

	private void bulkInsert(Nodes nodes) {
		long makeupTime = System.currentTimeMillis();
		int size = nodes.getMeta().getItemCount().intValue();
		ContentValues[] contentValuesArray = new ContentValues[size];
		for (int i = 0; i < size; i++) {
			ContentValues values = new ContentValues();
			copyNodeToValues(nodes.getNodes().get(i), values);
			
			contentValuesArray[i] = values;
		}
		long bulkInsertTime = System.currentTimeMillis();
		Log.d( TAG, "makeupTime = " + (bulkInsertTime - makeupTime ) / 1000f);
		int count = mResolver.bulkInsert( Wheelmap.POIs.CONTENT_URI, contentValuesArray );
		long bulkInsertDoneTime = System.currentTimeMillis();
		Log.d( TAG, "bulkInsertTime = " + (bulkInsertDoneTime - bulkInsertTime ) / 1000f );
		Log.d( TAG, "Inserted records count = " + count );
	}

	private void copyNodeToValues(Node node, ContentValues values) {
		values.clear();
		values.put(Wheelmap.POIs.WM_ID, node.getId().intValue());
		values.put(Wheelmap.POIs.NAME, node.getName());
		values.put(Wheelmap.POIs.COORD_LAT,
				Math.ceil(node.getLat().floatValue() * 1E6));
		values.put(Wheelmap.POIs.COORD_LON,
				Math.ceil(node.getLon().floatValue() * 1E6));
		values.put(Wheelmap.POIs.STREET, node.getStreet());
		values.put(Wheelmap.POIs.HOUSE_NUM, node.getHousenumber());
		values.put(Wheelmap.POIs.POSTCODE, node.getPostcode());
		values.put(Wheelmap.POIs.CITY, node.getCity());
		values.put(Wheelmap.POIs.PHONE, node.getPhone());
		values.put(Wheelmap.POIs.WEBSITE, node.getWebsite());
		values.put(Wheelmap.POIs.WHEELCHAIR, WheelchairState.myValueOf( node.getWheelchair()).getId());
		values.put(Wheelmap.POIs.WHEELCHAIR_DESC,
				node.getWheelchairDescription());
	}

	private static Nodes retrieveNumberOfHits(String getRequest) {
		Nodes nodes;
		long requestTime = System.currentTimeMillis();
		try {
			nodes = mRequestProcessor.get(new URI(getRequest), Nodes.class);
		} catch (URISyntaxException e) {
			throw new WheelMapException(e);
		}
		// Log.d(TAG, "response " + response);
		long requestEndTime = System.currentTimeMillis();
		Log.d(TAG, "requestTime = " + (requestEndTime - requestTime) / 1000f);

		return nodes;
	}

	public static <T> T unmarshal(final String xml, Class<T> clazz) {
		return XmlSupport.serialize(xml, clazz);
	}

}
