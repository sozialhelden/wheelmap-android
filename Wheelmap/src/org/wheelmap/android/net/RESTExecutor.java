package org.wheelmap.android.net;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;

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
import android.content.ContentProviderOperation;
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
		if ( wheelchairState != null )
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
		long startTime = new Date().getTime();

		Nodes nodes = retrieveNumberOfHits(getRequest);

		long retrieveTime = new Date().getTime();
		Log.d(TAG, "retrieveTime = " + (retrieveTime - startTime) / 1000f);
		
		// TODO make Content provider sortable
		//nodes.
		// quick and dirty sorting nodes 
		
		batchInsert( nodes );
		long insertTime = new Date().getTime();
		Log.d(TAG, "insertTime = " + (insertTime - retrieveTime) / 1000f);

		return nodes.getMeta();
	}

	private void batchInsert(Nodes nodes) throws RemoteException, OperationApplicationException {
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

		for (int i = 0; i < nodes.getMeta().getItemCount().intValue(); i++) {
			ContentValues values = new ContentValues();
			copyNodeToValues(nodes.getNodes().get(i), values);
			ContentProviderOperation operation = ContentProviderOperation
					.newInsert(Wheelmap.POIs.CONTENT_URI).withValues(values).build();
			operations.add( operation );
		}
		
		mResolver.applyBatch( Wheelmap.AUTHORITY, operations );
		
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
		long requestTime = new Date().getTime();
		try {
			nodes = mRequestProcessor.get(new URI(getRequest), Nodes.class);
		} catch (URISyntaxException e) {
			throw new WheelMapException(e);
		}
		// Log.d(TAG, "response " + response);
		long requestEndTime = new Date().getTime();
		Log.d(TAG, "requestTime = " + (requestEndTime - requestTime) / 1000f);

		return nodes;
	}

	public static <T> T unmarshal(final String xml, Class<T> clazz) {
		return XmlSupport.serialize(xml, clazz);
	}

}
