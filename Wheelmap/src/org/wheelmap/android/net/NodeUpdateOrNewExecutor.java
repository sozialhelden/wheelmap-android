package org.wheelmap.android.net;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.springframework.web.util.UriUtils;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.Wheelmap;

import wheelmap.org.WheelchairState;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.NodeUpdateOrNewAllRequestBuilder;
import wheelmap.org.request.RequestBuilder;
import wheelmap.org.request.WheelchairUpdateRequestBuilder;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class NodeUpdateOrNewExecutor extends AbstractExecutor {
	public NodeUpdateOrNewExecutor(ContentResolver resolver) {
		super(resolver, null);
	}

	private Cursor mCursor;

	private static final String whereClause = "( " + Wheelmap.POIs.UPDATE_TAG
			+ " != ? ) ";
	private static final String[] whereValue = new String[] { String
			.valueOf(Wheelmap.UPDATE_NO) };

	public void prepareContent() {
		mCursor = mResolver.query(Wheelmap.POIs.CONTENT_URI,
				Wheelmap.POIs.PROJECTION, whereClause, whereValue, null);
		mCursor.moveToFirst();
	}

	public void execute() throws ExecutorException {
		while (!mCursor.isAfterLast()) {
			int updateWay = POIHelper.getUpdateTag(mCursor);

			RequestBuilder requestBuilder = null;
			switch (updateWay) {
			case Wheelmap.UPDATE_WHEELCHAIR_STATE:
				requestBuilder = wheelchairUpdateRequestBuilder();
				break;
			case Wheelmap.UPDATE_ALL_NEW:
				requestBuilder = updateOrNewRequestBuilder();
				break;
			default:
				throw new ExecutorException(
						"Cant find matching RequestBuilder for update request");
			}

			String request;
			try {
				request = UriUtils.encodeQuery( requestBuilder.buildRequestUri(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				throw new ExecutorException( e );
			}
			try {
				if ( requestBuilder.getRequestType() == RequestBuilder.REQUEST_POST ) {
					Log.d(TAG, "postRequest = *" + request + "*");
					mRequestProcessor.post(new URI(request), null);
				} else {
					Log.d(TAG, "putRequest = *" + request + "*");
					mRequestProcessor.put(new URI(request), null);
				}
			} catch ( Exception e ) {
				throw new ExecutorException(e);
			}

			mCursor.moveToNext();
		}

	}

	public void prepareDatabase() {
		ContentValues values = new ContentValues();
		values.put(Wheelmap.POIs.UPDATE_TAG, Wheelmap.UPDATE_NO);
		mResolver.update(Wheelmap.POIs.CONTENT_URI, values, whereClause,
				whereValue);
	}

	private RequestBuilder wheelchairUpdateRequestBuilder() {
		long id = POIHelper.getWMId(mCursor);
		WheelchairState state = POIHelper.getWheelchair(mCursor);

		return new WheelchairUpdateRequestBuilder(SERVER, API_KEY,
				AcceptType.JSON, (int) id, state);
	}

	private RequestBuilder updateOrNewRequestBuilder() {
		long id = POIHelper.getWMId(mCursor);
		
		boolean update = false;
		if ( id != 0 )
			update = true;

		String name = POIHelper.getName(mCursor);
		// TODO: node type;
		String type = "butcher";
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

		return new NodeUpdateOrNewAllRequestBuilder(SERVER, API_KEY,
				AcceptType.JSON, id, name, type, latitude, longitude, state,
				comment, street, housenumber, city, postcode, website, phone,
				update);

	}
}
