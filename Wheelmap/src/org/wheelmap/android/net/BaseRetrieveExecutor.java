package org.wheelmap.android.net;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.util.UriUtils;
import org.wheelmap.android.service.SyncServiceException;

import wheelmap.org.domain.BaseDomain;
import wheelmap.org.domain.Meta;
import wheelmap.org.request.Paging;
import wheelmap.org.request.RequestBuilder;
import android.content.ContentResolver;
import android.os.Bundle;
import android.util.Log;

public abstract class BaseRetrieveExecutor<T extends BaseDomain> extends AbstractExecutor implements IExecutor {
	protected static final int DEFAULT_TEST_PAGE_SIZE = 500;
	private final Class<T> mClazz;
	private List<T> mTempStore = new ArrayList<T>();

	public BaseRetrieveExecutor(ContentResolver resolver, Bundle bundle, Class<T> clazz) {
		super(resolver, bundle);
		mClazz = clazz;
	}
	
	protected void clearTempStore() {
		mTempStore.clear();
	}
	
	protected List<T> getTempStore() {
		return mTempStore;
	}

	protected void retrieveSinglePage( RequestBuilder requestBuilder ) throws SyncServiceException {
		Meta m = executeSingleRequest(requestBuilder);
		Log.d(TAG, "totalItemsCount " + m.getItemCountTotal());
	}

	
	protected void retrieveMaxNPages( RequestBuilder requestBuilder, int n) throws SyncServiceException {
		// Server seems to count from 1...
		Paging page = new Paging(DEFAULT_TEST_PAGE_SIZE, 1);

		Meta m = executeSingleRequest(requestBuilder);
		Log.d(TAG, "totalItemsCount " + m.getItemCountTotal());

		int numOfPages = m.getNumPages().intValue();

		int numOfPagesToRetrieve = n < numOfPages ? n : numOfPages;
		int crrPage;
		for (crrPage = 2; crrPage <= numOfPagesToRetrieve; crrPage++) {
			page.setPage(crrPage);
			executeSingleRequest(requestBuilder);
		}
	}
		
	protected Meta executeSingleRequest(RequestBuilder requestBuilder) throws SyncServiceException {
		String getRequest = requestBuilder.buildRequestUri();
		Log.d(TAG, "getRequest " + getRequest);
		long retrieveStart = System.currentTimeMillis();

		T items = retrieveNumberOfHits(getRequest );
		mTempStore.add( items );
		
		long retrieveEnd = System.currentTimeMillis();
		Log.d(TAG, "retrieveTime = " + (retrieveEnd - retrieveStart) / 1000f);
		return items.getMeta();
	}
		
	private T retrieveNumberOfHits(String getRequest) throws SyncServiceException {
		T content;
		
		long requestTime = System.currentTimeMillis();
		String request;
		try {
			request = UriUtils.encodeQuery( getRequest, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new SyncServiceException( SyncServiceException.ERROR_INTERNAL_ERROR, e );
		}
	
		try {
			content = mRequestProcessor.get(new URI(request), mClazz);
		} catch (URISyntaxException e) {
			throw new SyncServiceException( SyncServiceException.ERROR_INTERNAL_ERROR, e );
		} catch (Exception e ) {
			throw new SyncServiceException( SyncServiceException.ERROR_NETWORK_FAILURE, e );
		}
		// Log.d(TAG, "response " + response);
		long requestEndTime = System.currentTimeMillis();
		Log.d(TAG, "requestTime = " + (requestEndTime - requestTime) / 1000f);

		return content;
	}

}
