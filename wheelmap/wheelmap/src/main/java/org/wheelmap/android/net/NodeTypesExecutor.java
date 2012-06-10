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

import org.wheelmap.android.model.Support.NodeTypesContent;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;

import wheelmap.org.Locale;
import wheelmap.org.domain.nodetype.NodeType;
import wheelmap.org.domain.nodetype.NodeTypes;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.NodeTypesRequestBuilder;
import wheelmap.org.request.Paging;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;

public class NodeTypesExecutor extends BaseRetrieveExecutor<NodeTypes>
		implements IExecutor {
	public static final String PREF_KEY_WHEELCHAIR_STATE = "wheelchairState";
	private Locale mLocale;

	public NodeTypesExecutor(ContentResolver resolver, Bundle bundle) {
		super(resolver, bundle, NodeTypes.class);
	}

	@Override
	public void prepareContent() {
		String locale = getBundle().getString(SyncService.EXTRA_LOCALE);
		if (locale != null && !locale.equals("de")) {
			mLocale = new Locale(locale);
		}

		getResolver().delete(NodeTypesContent.CONTENT_URI, null, null);
	}

	@Override
	public void execute() throws SyncServiceException {
		final long startRemote = System.currentTimeMillis();
		NodeTypesRequestBuilder requestBuilder = new NodeTypesRequestBuilder(
				SERVER, getApiKey(), AcceptType.JSON);
		requestBuilder.paging(new Paging(DEFAULT_TEST_PAGE_SIZE));
		if (mLocale != null) {
			requestBuilder.locale(mLocale);
		}
		clearTempStore();
		retrieveSinglePage(requestBuilder);
		Log.d(TAG, "remote sync took "
				+ (System.currentTimeMillis() - startRemote) + "ms");
	}

	@Override
	public void prepareDatabase() throws SyncServiceException {
		long insertStart = System.currentTimeMillis();
		for (NodeTypes nodeTypes : getTempStore()) {
			bulkInsert(nodeTypes);
		}
		long insertEnd = System.currentTimeMillis();
		Log.d(TAG, "insertTime = " + (insertEnd - insertStart) / 1000f);
		clearTempStore();
	}

	private void bulkInsert(NodeTypes nodeTypes) {
		int size = nodeTypes.getNodeTypes().size();
		ContentValues[] contentValuesArray = new ContentValues[size];
		int i;
		for (i = 0; i < size; i++) {
			ContentValues values = new ContentValues();
			copyNodeTypeToValues(nodeTypes.getNodeTypes().get(i),
					values );
			contentValuesArray[i] = values;
		}

		getResolver().bulkInsert(NodeTypesContent.CONTENT_URI,
				contentValuesArray);
	}

	private void copyNodeTypeToValues(NodeType nodeType, ContentValues values) {
		values.clear();
		values.put(NodeTypesContent.NODETYPE_ID, nodeType.getId().intValue());
		values.put(NodeTypesContent.IDENTIFIER, nodeType.getIdentifier());
		values.put(NodeTypesContent.ICON_URL, nodeType.getIconUrl());
		values.put(NodeTypesContent.LOCALIZED_NAME, nodeType.getLocalizedName());
		values.put(NodeTypesContent.CATEGORY_ID, nodeType.getCategoryId()
				.intValue());
		// values.put( NodeTypesContent.CATEGORY_ID,
		// nodeType.getCategory().getId().intValue());
		// values.put( NodeTypesContent.CATEGORY_IDENTIFIER,
		// nodeType.getCategory().getIdentifier());
	}
}
