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

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriUtils;
import org.wheelmap.android.service.SyncServiceException;

import wheelmap.org.domain.Base;
import wheelmap.org.request.RequestBuilder;
import android.content.Context;
import android.os.Bundle;
import de.akquinet.android.androlog.Log;

public abstract class SinglePageExecutor<T extends Base> extends
		AbstractExecutor implements IExecutor {
	private static final int MAX_RETRY_COUNT = 3;
	protected static final int DEFAULT_TEST_PAGE_SIZE = 500;

	private final Class<T> mClazz;
	private List<T> mTempStore = new ArrayList<T>();

	public SinglePageExecutor(Context context, Bundle bundle, Class<T> clazz) {
		super(context, bundle);
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
		final long startRemote = System.currentTimeMillis();
		int result = executeSingleRequest(requestBuilder);
		Log.i(getTag(), "remote sync took "
				+ (System.currentTimeMillis() - startRemote) + "ms");
	}

	protected int executeSingleRequest(RequestBuilder requestBuilder)
			throws SyncServiceException {

		String getRequest = requestBuilder.buildRequestUri();
		// Log.d(TAG, "getRequest " + getRequest);

		T items = retrieveNumberOfHits(getRequest);
		if (items == null) {
			Log.e(getTag(), "retrieved no items - tempstore is empty");
			return 0;
		}

		mTempStore.add(items);

		return 1;
	}

	protected T retrieveNumberOfHits(String getRequest)
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

		while (retryCount < MAX_RETRY_COUNT) {
			try {
				content = mRequestProcessor.get(new URI(request), mClazz);
				break;
			} catch (URISyntaxException e) {
				throw new SyncServiceException(
						SyncServiceException.ERROR_INTERNAL_ERROR, e);
			} catch (ResourceAccessException e) {
				retryCount++;
				if (retryCount < MAX_RETRY_COUNT) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e1) { // do nothing, just
														// continue and try
														// again
					}
					continue;
				} else {
					throw new SyncServiceException(
							SyncServiceException.ERROR_NETWORK_FAILURE, e);
				}
			} catch (HttpClientErrorException e) {
				throw new SyncServiceException(
						SyncServiceException.ERROR_CLIENT_FAILURE, e);

			} catch (HttpServerErrorException e) {
				throw new SyncServiceException(
						SyncServiceException.ERROR_SERVER_FAILURE, e);

			} catch (RestClientException e) {
				throw new SyncServiceException(
						SyncServiceException.ERROR_NETWORK_UNKNOWN_FAILURE, e);
			}

		}

		return content;
	}
}
