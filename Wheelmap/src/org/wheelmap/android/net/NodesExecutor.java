package org.wheelmap.android.net;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.utils.GeocoordinatesMath;
import org.wheelmap.android.utils.ParceableBoundingBox;

import wheelmap.org.BoundingBox;
import wheelmap.org.WheelMapException;
import wheelmap.org.WheelchairState;
import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;
import wheelmap.org.domain.node.json.Meta;
import wheelmap.org.domain.node.json.Node;
import wheelmap.org.domain.node.json.Nodes;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.NodesRequestBuilder;
import wheelmap.org.request.Paging;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

public class NodesExecutor extends AbstractExecutor implements IExecutor {
	public static final String PREF_KEY_WHEELCHAIR_STATE = "wheelchairState";
	private static final int DEFAULT_TEST_PAGE_SIZE = 500;
	
	private BoundingBox mBoundingBox;
	private WheelchairState mWheelchairState;
	private Context mContext;
	
	private List<Nodes> mNodesList = new ArrayList<Nodes>();

	public NodesExecutor(Context context, ContentResolver resolver, Bundle bundle) {
		super(resolver, bundle);
		mContext = context;
	}

	@Override
	public void prepareContent() {
		if (mBundle.containsKey( SyncService.EXTRA_BOUNDING_BOX)) {
			ParceableBoundingBox parcBoundingBox = (ParceableBoundingBox) mBundle
					.getSerializable(SyncService.EXTRA_BOUNDING_BOX);
			mBoundingBox = parcBoundingBox.toBoundingBox();
			Log.d(TAG,
					"retrieving with bounding box: "
							+ parcBoundingBox.toString());
		} else if ( mBundle.containsKey( SyncService.EXTRA_LOCATION)) {
			float distance = mBundle.getFloat( SyncService.EXTRA_DISTANCE_LIMIT);
			Location location = (Location) mBundle
					.getParcelable( SyncService.EXTRA_LOCATION);
			mBoundingBox = GeocoordinatesMath.calculateBoundingBox(
					new Wgs84GeoCoordinates(location.getLongitude(),
							location.getLatitude()), distance);
			Log.d(TAG,
					"retrieving with current location = ("
							+ location.getLongitude() + ","
							+ location.getLatitude() + ") and distance = "
							+ distance);
		}
		
		mWheelchairState = getWheelchairStateFromPreferences();
		
		deleteRetrievedData();
	}
	
	@Override
	public void execute() throws ExecutorException {
		final long startRemote = System.currentTimeMillis();
		// Retrieve all Pages is terribly slow. Anybody knows why?
		// mRemoteExecutor.retrieveAllPages(bb, wheelState);
		final NodesRequestBuilder requestBuilder = new NodesRequestBuilder( SERVER, API_KEY, AcceptType.JSON );
		// 1. maxi nodes
		requestBuilder.paging(new Paging(DEFAULT_TEST_PAGE_SIZE))
				.boundingBox(mBoundingBox);
		requestBuilder.wheelchairState(mWheelchairState);
		
		mNodesList.clear();
		try {
			// retrieveAllPages( requestBuilder );
			retrieveSinglePage(requestBuilder);
		} catch ( Exception e ) {
			throw new ExecutorException( e );
		}
		Log.d(TAG, "remote sync took "
				+ (System.currentTimeMillis() - startRemote) + "ms");
	}

	@Override
	public void prepareDatabase() {
		long insertStart = System.currentTimeMillis();
		for( Nodes nodes: mNodesList ) {
			bulkInsert(nodes);
		}
		long insertEnd = System.currentTimeMillis();
		Log.d(TAG, "insertTime = " + (insertEnd - insertStart) / 1000f);
		mNodesList.clear();
	}
	
	private void deleteRetrievedData() {
		String whereClause = "( " + Wheelmap.POIs.UPDATE_TAG + " = ? )";
		String[] whereValues = new String[]{ String.valueOf(Wheelmap.UPDATE_NO) };
		mResolver.delete(Wheelmap.POIs.CONTENT_URI, whereClause, whereValues);
	}
	
	
	private void retrieveSinglePage( NodesRequestBuilder requestBuilder ) throws RemoteException, OperationApplicationException {
		Meta m = executeSingleRequest(requestBuilder);
		Log.d(TAG, "totalItemsCount " + m.getItemCountTotal());
	}
	
	private WheelchairState getWheelchairStateFromPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		String prefWheelchairState = prefs.getString(PREF_KEY_WHEELCHAIR_STATE,
				String.valueOf( WheelchairState.DEFAULT.getId()));
		WheelchairState ws = WheelchairState.valueOf(Integer
				.valueOf(prefWheelchairState));
		return ws;
	}
	
	private void retrieveAllPages( NodesRequestBuilder requestBuilder) throws RemoteException, OperationApplicationException {
		// Server seems to count from 1...
		Paging page = new Paging(DEFAULT_TEST_PAGE_SIZE, 1);

		Meta m = executeSingleRequest(requestBuilder);
		Log.d(TAG, "totalItemsCount " + m.getItemCountTotal());

		int numOfPages = m.getNumPages().intValue();

		int crrPage;
		for (crrPage = 2; crrPage <= numOfPages; crrPage++) {
			page.setPage(crrPage);
			executeSingleRequest(requestBuilder);
		}
	}
	
	private Meta executeSingleRequest(NodesRequestBuilder requestBuilder) throws RemoteException, OperationApplicationException {
		String getRequest = requestBuilder.buildRequestUri();
		Log.d(TAG, "getRequest " + getRequest);
		long retrieveStart = System.currentTimeMillis();

		Nodes nodes = retrieveNumberOfHits(getRequest);
		mNodesList.add( nodes );
		
		long retrieveEnd = System.currentTimeMillis();
		Log.d(TAG, "retrieveTime = " + (retrieveEnd - retrieveStart) / 1000f);
		return nodes.getMeta();
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
				Math.ceil(node.getLat().doubleValue() * 1E6));
		values.put(Wheelmap.POIs.COORD_LON,
				Math.ceil(node.getLon().doubleValue() * 1E6));
		values.put(Wheelmap.POIs.STREET, node.getStreet());
		values.put(Wheelmap.POIs.HOUSE_NUM, node.getHousenumber());
		values.put(Wheelmap.POIs.POSTCODE, node.getPostcode());
		values.put(Wheelmap.POIs.CITY, node.getCity());
		values.put(Wheelmap.POIs.PHONE, node.getPhone());
		values.put(Wheelmap.POIs.WEBSITE, node.getWebsite());
		values.put(Wheelmap.POIs.WHEELCHAIR, WheelchairState.myValueOf( node.getWheelchair()).getId());
		values.put(Wheelmap.POIs.WHEELCHAIR_DESC,
				node.getWheelchairDescription());
		values.put(Wheelmap.POIs.UPDATE_TAG, Wheelmap.UPDATE_NO);
	}

}
