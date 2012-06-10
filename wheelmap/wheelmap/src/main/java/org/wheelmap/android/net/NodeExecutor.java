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

import org.springframework.web.util.UriUtils;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;

import wheelmap.org.domain.node.SingleNode;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.NodeRequestBuilder;
import wheelmap.org.request.RequestBuilder;

public class NodeExecutor extends AbstractExecutor implements IExecutor {
	private static final int MAX_RETRY_COUNT = 3;

	long mNodeId = -1;

	private SingleNode mTempStore = new SingleNode();
	private PrepareDatabaseHelper prepDbHelper;

	public NodeExecutor(ContentResolver resolver, Bundle bundle) {
		super(resolver, bundle);
		prepDbHelper = new PrepareDatabaseHelper( resolver );
	}

	@Override
	public void prepareContent() {
		mNodeId = getBundle().getLong(SyncService.EXTRA_WHEELMAP_ID);
	}

	@Override
	public void execute() throws SyncServiceException {
		NodeRequestBuilder requestBuilder = null;
		if (mNodeId != -1 && mNodeId != 0) {
			requestBuilder = new NodeRequestBuilder( SERVER, getApiKey(), AcceptType.JSON, mNodeId );
		}
		mTempStore = null;
		
		retrieveSingleNode( requestBuilder );
	}

	protected void retrieveSingleNode(RequestBuilder requestBuilder)
			throws SyncServiceException {
		String getRequest = requestBuilder.buildRequestUri();
		Log.d(TAG, "getRequest " + getRequest);

		SingleNode item = retrieveNumberOfHits(getRequest);
		if (item == null)
			return;

		mTempStore = item;
	}

	private SingleNode retrieveNumberOfHits(String getRequest)
			throws SyncServiceException {
		SingleNode content = null;

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
				content = mRequestProcessor.get(new URI(request), SingleNode.class);
				break;
			} catch (URISyntaxException e) {
				throw new SyncServiceException(
						SyncServiceException.ERROR_INTERNAL_ERROR, e);
			} catch (Exception e) {
				retryCount++;
				if (retryCount < MAX_RETRY_COUNT) {
					try {
						Thread.sleep(200);
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

	@Override
	public void prepareDatabase() throws SyncServiceException {
		prepDbHelper.deleteAllOldPending();

		insert( mTempStore );
		prepDbHelper.copyAllPendingDataToRetrievedData();
	}
	
	private void insert( SingleNode node ) {
		ContentValues values = new ContentValues();
		prepDbHelper.copyNodeToValues(node.getNode(), values);
		String whereClause = "( " + POIs.WM_ID + " = ? )";
		String whereValues[] = { node.getNode().getId().toString() };
		
		prepDbHelper.insertOrUpdateContentValues( Wheelmap.POIs.CONTENT_URI, Wheelmap.POIs.PROJECTION, whereClause, whereValues, values);
	}

}
