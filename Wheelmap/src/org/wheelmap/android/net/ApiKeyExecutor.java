/*
Copyright (C) 2011 Michal Harakal and Michael Kroez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS-IS" BASIS
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
        
*/

package org.wheelmap.android.net;

import java.io.UnsupportedEncodingException;

import org.springframework.web.util.UriUtils;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;

import wheelmap.org.domain.apikey.ApiKey;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.ApiKeyRequestBuilder;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class ApiKeyExecutor extends AbstractExecutor {
	private Context mContext;
	private String mUserName;
	private String mPassword;
	private String mApiKey;

	public ApiKeyExecutor(Context context, ContentResolver resolver,
			Bundle bundle) {
		super(resolver, bundle);
		mContext = context;
	}

	@Override
	public void prepareContent() {
		mUserName = getBundle().getString(SyncService.EXTRA_USERNAME);
		mPassword = getBundle().getString(SyncService.EXTRA_PASSWORD);
	}

	@Override
	public void execute() throws SyncServiceException {

		ApiKeyRequestBuilder requestBuilder = new ApiKeyRequestBuilder(SERVER,
				AcceptType.JSON);
		requestBuilder.setCredentials(mUserName, mPassword);
		String request;
		try {
			request = UriUtils.encodeQuery(requestBuilder.buildRequestUri(),
					"utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new SyncServiceException( SyncServiceException.ERROR_INTERNAL_ERROR, e);
		}
		ApiKey apiKey;
		try {
			// apiKey = mRequestProcessor.get(new URI(request), ApiKey.class);
		} catch (Exception e) {
			throw new SyncServiceException( SyncServiceException.ERROR_INTERNAL_ERROR, e);
		}

		// mApiKey = apiKey.getApiKey();
		mApiKey = "jWeAsb34CJq4yVAryjtc";
	}

	@Override
	public void prepareDatabase() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		prefs.edit().putString(SyncService.PREFS_API_KEY, mApiKey).commit();

	}

}
