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

import com.google.inject.Inject;
import org.wheelmap.android.app.ICredentials;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.service.SyncServiceException;

import roboguice.RoboGuice;
import wheelmap.org.domain.apikey.AuthInfo;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.ApiKeyRequestBuilder;
import android.content.Context;
import android.os.Bundle;

public class ApiKeyExecutor extends AbstractExecutor<AuthInfo> {
	private static final int MAX_RETRY_COUNT = 1;

	private String mEmail;
	private String mPassword;
	private String mApiKey;

	private final static int statusAuthFailed = 400;
	private final static int statusOSMFailed = 403;

	@Inject
	private ICredentials mCredentials;

	public ApiKeyExecutor(Context context, Bundle bundle) {
		super(context, bundle, AuthInfo.class, MAX_RETRY_COUNT);
		RoboGuice.injectMembers(context, this);
	}

	@Override
	public void prepareContent() {
		mEmail = getBundle().getString(Extra.EMAIL);
		mPassword = getBundle().getString(Extra.PASSWORD);
	}

	@Override
	public void execute() throws SyncServiceException {

		ApiKeyRequestBuilder requestBuilder = new ApiKeyRequestBuilder(
				getServer(), AcceptType.JSON);
		requestBuilder.setCredentials(mEmail, mPassword);

		AuthInfo authInfo = executeRequest(requestBuilder);
		mApiKey = authInfo.getUser().getApiKey();
	}

	@Override
	public void prepareDatabase() {
		mCredentials.save(mApiKey, mEmail);
	}

}
