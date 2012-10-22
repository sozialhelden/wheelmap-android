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

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriUtils;
import org.wheelmap.android.app.ICredentials;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.service.SyncServiceException;

import roboguice.RoboGuice;
import wheelmap.org.domain.apikey.AuthInfo;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.ApiKeyRequestBuilder;
import android.content.Context;
import android.os.Bundle;
import de.akquinet.android.androlog.Log;

public class ApiKeyExecutor extends AbstractExecutor {

	private String mEmail;
	private String mPassword;
	private String mApiKey;

	private final static int statusAuthFailed = 400;
	private final static int statusOSMFailed = 403;

	@Inject
	private ICredentials mCredentials;

	public ApiKeyExecutor(Context context, Bundle bundle) {
		super(context, bundle);
		RoboGuice.injectMembers(context, this );
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
		String request;
		try {
			request = UriUtils.encodeQuery(requestBuilder.buildRequestUri(),
					"utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new SyncServiceException(
					SyncServiceException.ERROR_INTERNAL_ERROR, e);
		}

		AuthInfo authInfo = null;
		try {
			authInfo = mRequestProcessor.post(new URI(request), null,
					AuthInfo.class);
		} catch (URISyntaxException e) {
			throw new SyncServiceException(
					SyncServiceException.ERROR_INTERNAL_ERROR, e);
		} catch (ResourceAccessException e) {
			throw new SyncServiceException(
					SyncServiceException.ERROR_NETWORK_FAILURE, e);
		} catch (HttpClientErrorException e) {
			HttpStatus status = e.getStatusCode();
			if (status.value() == statusAuthFailed) {
				Log.e(getTag(), "wrong email or password");
				throw new SyncServiceException(
						SyncServiceException.ERROR_AUTHORIZATION_ERROR, e);
			} else if (status.value() == statusOSMFailed) {
				Log.e(getTag(), "not osm connected");
				throw new SyncServiceException(
						SyncServiceException.ERROR_NOT_OSM_CONNECTED, e);
			} else {
				throw new SyncServiceException(
						SyncServiceException.ERROR_CLIENT_FAILURE, e);
			}
		} catch (HttpServerErrorException e) {
			throw new SyncServiceException(
					SyncServiceException.ERROR_SERVER_FAILURE, e);
		} catch (RestClientException e) {
			throw new SyncServiceException(
					SyncServiceException.ERROR_NETWORK_UNKNOWN_FAILURE, e);
		}
		mApiKey = authInfo.getUser().getApiKey();
	}

	@Override
	public void prepareDatabase() {
		mCredentials.save( mApiKey, mEmail );
	}

}
