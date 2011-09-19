package org.wheelmap.android.net;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.springframework.web.util.UriUtils;
import org.wheelmap.android.service.SyncService;

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
	public void execute() throws ExecutorException {

		ApiKeyRequestBuilder requestBuilder = new ApiKeyRequestBuilder(SERVER,
				AcceptType.JSON);
		requestBuilder.setCredentials(mUserName, mPassword);
		String request;
		try {
			request = UriUtils.encodeQuery(requestBuilder.buildRequestUri(),
					"utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new ExecutorException(e);
		}
		ApiKey apiKey;
		try {
			// apiKey = mRequestProcessor.get(new URI(request), ApiKey.class);
		} catch (Exception e) {
			throw new ExecutorException(e);
		}

		// mApiKey = apiKey.getApiKey();
		mApiKey = API_KEY;
	}

	@Override
	public void prepareDatabase() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		prefs.edit().putString(SyncService.PREFS_API_KEY, mApiKey).commit();

	}

}
