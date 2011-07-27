package org.wheelmap.android;

import java.net.URI;
import java.net.URISyntaxException;

import org.wheelmap.android.model.Wheelmap;

import wheelmap.org.BoundingBox;
import wheelmap.org.WheelMapException;
import wheelmap.org.WheelchairState;
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
	private static RequestProcessor mRequestProcessor = new RequestProcessor();

	public RESTExecutor(ContentResolver resolver) {
		mResolver = resolver;

	}

	public void execute(BoundingBox boundingBox) {
		// get stuff from server 
		final NodesRequestBuilder requestBuilder = new NodesRequestBuilder(SERVER,
				API_KEY,AcceptType.XML);

		// 1. maxi nodes
		String getRequest = requestBuilder.
		paging(new Paging(5)).
		boundingBox(boundingBox).
		wheelchairState(WheelchairState.UNKNOWN).buildRequestUri();

		Log.d(TAG, "getRequest " + getRequest);
		Nodes nodes = retrieveNumberOfHIts(getRequest);
		
		// sync ContentProvider
		// if ddat locally changed and not commited to the server, etc.
		
		// here just delete whole table 
		mResolver.delete(Wheelmap.POIs.CONTENT_URI, null, null);
		
		// and replace with new items, server is the master
		ContentValues values = new ContentValues();
		for (int i = 0; i < nodes.getMeta().getItemCount().intValue(); i++) {
			Node node = nodes.getNodes().getNode().get(i);
			copyNodeToValues( node, values );
			mResolver.insert(Wheelmap.POIs.CONTENT_URI, values);
		}
	}
	
	private void copyNodeToValues( Node node, ContentValues values ) {
		values.clear();
		// insert data into DB
		values.put(Wheelmap.POIs.WM_ID, node.getId().intValue());
		values.put(Wheelmap.POIs.NAME, node.getName());
		values.put(Wheelmap.POIs.COORD_LAT, node.getLat().toString());
		values.put(Wheelmap.POIs.COORD_LON, node.getLon().toString());
		values.put(Wheelmap.POIs.STREET, node.getStreet());
		values.put(Wheelmap.POIs.HOUSE_NUM, node.getHousenumber());
		values.put(Wheelmap.POIs.POSTCODE, node.getPostcode());
		values.put(Wheelmap.POIs.CITY, node.getCity());
		values.put(Wheelmap.POIs.PHONE, node.getPhone());
		values.put(Wheelmap.POIs.WEBSITE, node.getWebsite());
		values.put(Wheelmap.POIs.WHEELCHAIR, node.getWheelchair());
		values.put(Wheelmap.POIs.WHEELCHAIR_DESC, node.getWheelchairDescription());
	}

	private static Nodes retrieveNumberOfHIts(String getRequest) {
		String response=null;
		try {
			response = mRequestProcessor.get(new URI(getRequest),String.class);
		} catch (URISyntaxException e) {
			throw new WheelMapException(e);
		}
		Log.d(TAG, "response " + response);

		Nodes nodes = unmarshal(response,Nodes.class);
		Log.d(TAG, "totalItemsCount " + nodes.getMeta().getItemCountTotal());
		return nodes;
	}

	public static <T> T unmarshal(final String xml, Class<T> clazz) {	
		return XmlSupport.serialize(xml, clazz);
	}


}
