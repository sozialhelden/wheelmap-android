package org.wheelmap.android.net;

import java.net.URI;
import java.net.URISyntaxException;

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

	public void prepareContent() {
		String[] whereValue = new String[] { String.valueOf(Wheelmap.UPDATE_NO) };

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
			case Wheelmap.UPDATE_ALL:
				requestBuilder = updateOrNewRequestBuilder(true);
				break;
			case Wheelmap.UPDATE_ALL_NEW:
				requestBuilder = updateOrNewRequestBuilder(false);
				break;
			default:
				throw new ExecutorException(
						"Cant find matching RequestBuilder for update request");
			}

			String request = requestBuilder.buildRequestUri();
			Log.d(TAG, "postRequest = *" + request + "*");
			try {
				if (updateWay == Wheelmap.UPDATE_ALL_NEW)
					mRequestProcessor.post(new URI(request), null);
				else
					mRequestProcessor.put(new URI(request), null);
			} catch (URISyntaxException e) {
				throw new ExecutorException(e);
			}

			mCursor.moveToNext();
		}

	}

	public void prepareDatabase() {
		String[] whereValues = new String[] { String
				.valueOf(Wheelmap.UPDATE_WHEELCHAIR_STATE) };
		ContentValues values = new ContentValues();
		values.put(Wheelmap.POIs.UPDATE_TAG, Wheelmap.UPDATE_NO);
		mResolver.update(Wheelmap.POIs.CONTENT_URI, values, whereClause,
				whereValues);
	}

	private RequestBuilder wheelchairUpdateRequestBuilder() {
		long id = POIHelper.getWMId(mCursor);
		WheelchairState state = POIHelper.getWheelchair(mCursor);

		return new WheelchairUpdateRequestBuilder(SERVER, API_KEY,
				AcceptType.JSON, (int) id, state);
	}

	private RequestBuilder updateOrNewRequestBuilder(boolean update) {
		long id;
		if (update)
			id = POIHelper.getWMId(mCursor);
		else
			id = -1;

		String name = POIHelper.getName(mCursor);
		// TODO: node type;
		String type = null;
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

		return new NodeUpdateOrNewAllRequestBuilder(SERVER, API_KEY, AcceptType.JSON,
				id, name, type, latitude, longitude, state, comment, street,
				housenumber, city, postcode, website, phone, update);

	}
}
