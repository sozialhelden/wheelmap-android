package org.wheelmap.android;

import java.net.URI;
import java.net.URISyntaxException;

import org.wheelmap.android.model.Wheelmap;

import wheelmap.org.BoundingBox;
import wheelmap.org.WheelMapException;
import wheelmap.org.WheelchairState;
import wheelmap.org.domain.node.Meta;
import wheelmap.org.domain.node.Node;
import wheelmap.org.domain.node.Nodes;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.NodesRequestBuilder;
import wheelmap.org.request.Paging;
import wheelmap.org.request.RequestProcessor;
import wheelmap.org.util.XmlSupport;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.util.Log;

public class RESTExecutor {
	private static final String TAG = "RESTExecutor";

	private ContentResolver mResolver;
	private static final String SERVER = "staging.wheelmap.org";
	private static final String API_KEY = "9NryQWfDWgIebZIdqWiK";
	public static final int DEFAULT_TEST_PAGE_SIZE = 5;

	private static RequestProcessor mRequestProcessor = new RequestProcessor();

	public RESTExecutor(ContentResolver resolver) {
		mResolver = resolver;

	}

	public void retrieveSinglePage(BoundingBox boundingBox, WheelchairState wheelchairState ) {
		// get stuff from server
		final NodesRequestBuilder requestBuilder = new NodesRequestBuilder(
				SERVER, API_KEY, AcceptType.XML);

		// 1. maxi nodes
		requestBuilder.paging(new Paging(DEFAULT_TEST_PAGE_SIZE))
				.boundingBox(boundingBox)
				.wheelchairState(wheelchairState);

		// here just delete whole table
		mResolver.delete(Wheelmap.POIs.CONTENT_URI, null, null);
		executeSingleRequest(requestBuilder);
	}

	public void retrieveAllPages(BoundingBox boundingBox,
			WheelchairState wheelchairState) {
		// here just delete all pois. Perhaps something more intelligent may come up
		mResolver.delete(Wheelmap.POIs.CONTENT_URI, null, null);
		
		final NodesRequestBuilder requestBuilder = new NodesRequestBuilder(
				SERVER, API_KEY, AcceptType.XML);

		// Server seems to count from 1...
		Paging page = new Paging( DEFAULT_TEST_PAGE_SIZE, 1 );
		requestBuilder.boundingBox(boundingBox)
				.paging( page )
				.wheelchairState(wheelchairState);

		Meta m = executeSingleRequest(requestBuilder);
		int numOfPages = m.getNumPages().intValue();

		int crrPage;
		for (crrPage = 2; crrPage <= numOfPages; crrPage++) {
			page.setPage( crrPage );
			executeSingleRequest(requestBuilder );
		}

	}

	public Meta executeSingleRequest(NodesRequestBuilder requestBuilder) {
		String getRequest = requestBuilder.buildRequestUri();
		Log.d(TAG, "getRequest " + getRequest);
		
		Nodes nodes = retrieveNumberOfHits(getRequest);
		ContentValues values = new ContentValues();
		for (int i = 0; i < nodes.getMeta().getItemCount().intValue(); i++) {
			Node node = nodes.getNodes().getNode().get(i);
			// insert data into DB
			copyNodeToValues(node, values);
			mResolver.insert(Wheelmap.POIs.CONTENT_URI, values);
		}

		return nodes.getMeta();
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
		values.put(Wheelmap.POIs.WHEELCHAIR, node.getWheelchair());
		values.put(Wheelmap.POIs.WHEELCHAIR_DESC,
				node.getWheelchairDescription());
	}

	private static Nodes retrieveNumberOfHits(String getRequest) {
		String response = null;
		try {
			response = mRequestProcessor.get(new URI(getRequest), String.class);
		} catch (URISyntaxException e) {
			throw new WheelMapException(e);
		}
//		Log.d(TAG, "response " + response);

		Nodes nodes = unmarshal(response, Nodes.class);
		Log.d(TAG, "totalItemsCount " + nodes.getMeta().getItemCountTotal());
		return nodes;
	}

	public static <T> T unmarshal(final String xml, Class<T> clazz) {
		return XmlSupport.serialize(xml, clazz);
	}

}
