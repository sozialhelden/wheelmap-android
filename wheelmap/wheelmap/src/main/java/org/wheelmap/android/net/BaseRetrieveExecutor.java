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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.util.UriUtils;
import org.wheelmap.android.service.SyncServiceException;

import wheelmap.org.domain.BaseDomain;
import wheelmap.org.domain.Meta;
import wheelmap.org.request.BaseNodesRequestBuilder;
import wheelmap.org.request.Paging;
import wheelmap.org.request.RequestBuilder;
import android.content.ContentResolver;
import android.os.Bundle;
import android.util.Log;

public abstract class BaseRetrieveExecutor<T extends BaseDomain> extends
		AbstractExecutor implements IExecutor {
	protected static final int DEFAULT_TEST_PAGE_SIZE = 500;
	private static final int MAX_RETRY_COUNT = 3;

	private final Class<T> mClazz;
	private List<T> mTempStore = new ArrayList<T>();

	public BaseRetrieveExecutor(ContentResolver resolver, Bundle bundle,
			Class<T> clazz) {
		super(resolver, bundle);
		mClazz = clazz;
	}

	protected void clearTempStore() {
		mTempStore.clear();
	}

	protected List<T> getTempStore() {
		return mTempStore;
	}

	protected void retrieveSinglePage(RequestBuilder requestBuilder)
			throws SyncServiceException {
		Meta m = executeSingleRequest(requestBuilder);
//		Log.d(TAG, "totalItemsCount " + m.getItemCountTotal());
	}

	protected void retrieveMaxNPages(RequestBuilder requestBuilder, int n)
			throws SyncServiceException {
		// Server seems to count from 1...
		Paging page = new Paging(DEFAULT_TEST_PAGE_SIZE, 1);
		if ( requestBuilder instanceof BaseNodesRequestBuilder)
				((BaseNodesRequestBuilder)requestBuilder).paging( page );
		
		Meta m = executeSingleRequest(requestBuilder);
		if ( m == null )
			return;
		
//		Log.d(TAG, "totalItemsCount " + m.getItemCountTotal());

		int numOfPages = m.getNumPages().intValue();

		int numOfPagesToRetrieve = n < numOfPages ? n : numOfPages;
		int crrPage;
		for (crrPage = 2; crrPage <= numOfPagesToRetrieve; crrPage++) {
			page.setPage(crrPage);
			executeSingleRequest(requestBuilder);
		}
	}

	protected Meta executeSingleRequest(RequestBuilder requestBuilder)
			throws SyncServiceException {
		String getRequest = requestBuilder.buildRequestUri();
		// Log.d(TAG, "getRequest " + getRequest);

		T items = retrieveNumberOfHits(getRequest);
		if ( items == null )
			return null;
		
		mTempStore.add(items);

		return items.getMeta();
	}

	private T retrieveNumberOfHits(String getRequest)
			throws SyncServiceException {
		T content = null;

		String request;
		try {
			request = UriUtils.encodeQuery(getRequest, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new SyncServiceException(
					SyncServiceException.ERROR_INTERNAL_ERROR, e);
		}

		int retryCount = 0;

		while ( retryCount < MAX_RETRY_COUNT) {
			try {
				content = mRequestProcessor.get(new URI(request), mClazz);
				break;
			} catch (URISyntaxException e) {
				throw new SyncServiceException(
						SyncServiceException.ERROR_INTERNAL_ERROR, e);
			} catch (Exception e) {
				retryCount++;
				if (retryCount < MAX_RETRY_COUNT) {
					try {
						Thread.sleep( 200 );
					} catch (InterruptedException e1) {
						// do nothing, just continue and try again
					}
					continue;
				} else
					throw new SyncServiceException(
							SyncServiceException.ERROR_NETWORK_FAILURE, e);
			}
		}

		return content;
	}

}
