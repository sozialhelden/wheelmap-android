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

import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.PrepareDatabaseHelper;
import org.wheelmap.android.service.SyncServiceException;

import wheelmap.org.domain.node.SingleNode;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.NodeRequestBuilder;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;

public class NodeExecutor extends SinglePageExecutor<SingleNode> implements
		IExecutor {
	private String mWMId = Extra.WM_ID_UNKNOWN;

	public NodeExecutor(Context context, Bundle bundle) {
		super(context, bundle, SingleNode.class);
	}

	@Override
	public void prepareContent() {
		mWMId = getBundle().getString(Extra.WM_ID);
	}

	@Override
	public void execute() throws SyncServiceException {
		NodeRequestBuilder requestBuilder = null;
		if (mWMId == Extra.WM_ID_UNKNOWN)
			throw new SyncServiceException(
					SyncServiceException.ERROR_INTERNAL_ERROR,
					new IllegalArgumentException());

		requestBuilder = new NodeRequestBuilder(getServer(), getApiKey(),
				AcceptType.JSON, mWMId);
		int count = executeSingleRequest(requestBuilder);
		if (count == 0)
			throw new SyncServiceException(
					SyncServiceException.ERROR_NETWORK_FAILURE,
					new NetworkErrorException());
	}

	@Override
	public void prepareDatabase() throws SyncServiceException {
		PrepareDatabaseHelper.cleanupOldCopies(getResolver());
		PrepareDatabaseHelper.insert(getResolver(), getTempStore().get(0));
		PrepareDatabaseHelper.replayChangedCopies(getResolver());
	}
}
