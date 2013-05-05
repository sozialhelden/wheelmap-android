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
package org.wheelmap.android.modules;


import com.google.inject.Inject;

import oak.ObscuredSharedPreferences;
import android.content.Context;
import android.content.SharedPreferences;
import org.wheelmap.android.app.WheelmapObscuredSharedPreferences;

public class UserCredentials implements ICredentials {

	private static final String LOGGED_IN = "loggedin";
	private static final String E_MAIL = "email";
	private static final String API_KEY = "apiKey";
	private static final String PREFS_NAME = "credentials";

	// anonymous API key for changing wheelchair state (no access to OSM data)
	protected static final String ANONYMOUNS_ACCESS_API_KEY = "jWeAsb34CJq4yVAryjtc";


	private String mApiKey;
	private String mEmail;	
	private boolean mIsLoggedIn;
	private Context mContext;

	@Inject
	public UserCredentials(Context context) {
		mContext = context;
		load();
	}

	/**
	 * saves credential for user logged in
	 */
	@Override
	public void save(final String apiKey, final String email) {
		mIsLoggedIn = true;
		mApiKey = apiKey;
		mEmail = email;
		saveSecure();
	}

	public String getApiKey() {
		if (mIsLoggedIn)
			return mApiKey;
		else	
			return ANONYMOUNS_ACCESS_API_KEY;
	}

	public void logout() {
		mIsLoggedIn = false;
		mApiKey = null;
		saveSecure();
	}


	@Override
	public boolean isLoggedIn() {
		return mIsLoggedIn;
	}

	private void saveSecure() {
		// Restore preferences
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,
				0);

		ObscuredSharedPreferences  obscuredSharedPreferences = new WheelmapObscuredSharedPreferences(mContext, settings);

		SharedPreferences.Editor editor = obscuredSharedPreferences.edit();
		editor.putString(API_KEY, mApiKey);
		editor.putBoolean(LOGGED_IN, mIsLoggedIn);
		editor.putString(E_MAIL, mEmail);
		// Commit the edits!
		editor.commit();
	}

	private void load() {
		// Restore preferences
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,
				0);

		ObscuredSharedPreferences  obscuredSharedPreferences = new WheelmapObscuredSharedPreferences(mContext, settings);
		try {
			mApiKey = obscuredSharedPreferences.getString(API_KEY, ANONYMOUNS_ACCESS_API_KEY);
			mEmail = obscuredSharedPreferences.getString(E_MAIL, "");
			mIsLoggedIn = obscuredSharedPreferences.getBoolean(LOGGED_IN, false);
		} catch( RuntimeException e ) {
			mApiKey = null;
			mEmail = null;
			mIsLoggedIn = false;
			saveSecure();
		}
	}

	@Override
	public String getUserName() {
		return mEmail;
	}
}
