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

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.wheelmap.android.mapping.apikey.AuthInfo;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.modules.ICredentials;
import org.wheelmap.android.net.request.AcceptType;
import org.wheelmap.android.net.request.ApiKeyRequestBuilder;
import org.wheelmap.android.service.RestServiceException;

import roboguice.RoboGuice;
import android.content.Context;
import android.os.Bundle;

import com.google.inject.Inject;
import de.akquinet.android.androlog.Log;

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
	public void execute(long id) throws RestServiceException {

		ApiKeyRequestBuilder requestBuilder = new ApiKeyRequestBuilder(getServer(), AcceptType.JSON);
		requestBuilder.setCredentials(mEmail, mPassword);

		AuthInfo authInfo = executeRequest(requestBuilder);
		mApiKey = authInfo.getUser().getApiKey();
	}

	@Override
	public void prepareDatabase() {
		mCredentials.save(mApiKey, mEmail);
	}

	@Override
	protected void checkApiCallClientErrors(HttpClientErrorException e) throws RestServiceException {
		HttpStatus status = e.getStatusCode();
		if (status.value() == statusAuthFailed) {
			Log.e(getTag(), "authorization failed - email or password not valid");
            processException(RestServiceException.ERROR_AUTHORIZATION_ERROR, e, false);
		} else if (status.value() == statusOSMFailed) {
			Log.e(getTag(), "osm failed");
			processException(RestServiceException.ERROR_NOT_OSM_CONNECTED, e, false);
		}
	}
}
