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
package org.wheelmap.android.model;

import android.content.Context;
import android.content.SharedPreferences;

public class UserCredentials {

	// TODO refactor constants for preferencies into one class
	public static final String PREFS_NAME = "credentials";


	private String mLogin;
	private String mPassword;
	private String mApiKey;
	private boolean mIsLoggenIn;
	private Context mContext;

	public UserCredentials(Context context){
		mContext = context;
		load();
	}

	public void login(String login, String password, String apiKey) {
		mLogin = login;
		mPassword = password;
		mIsLoggenIn = true;
		// TODO make request to server to get the API_key for login/password
		mApiKey = apiKey;
		save();
	}
	
	public String getApiKey() {
		return mApiKey;
	};
	
	public void logout() {
		mIsLoggenIn = false;
		mLogin = null;
		mPassword = null;
		mApiKey = null;
		save();
	}	

	public String getLogin() {
		return mLogin;
	}

	public String getPassword() {
		return mPassword;
	}	

	public boolean isLoggedIn() {
		return mIsLoggenIn;		
	}

	private void save(){
		// Restore preferences
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("login", mLogin);
		editor.putString("password", mPassword);
		editor.putString("apikey", mApiKey);
		editor.putBoolean("loggedin", mIsLoggenIn);
		// Commit the edits!
		editor.commit();
	}

	private void load(){
		// Restore preferences
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
		mLogin = settings.getString("login", "android_app@wheelmap.org");
		mPassword = settings.getString("password", "");
		mApiKey = settings.getString("apikey", "jWeAsb34CJq4yVAryjtc");
		mIsLoggenIn = settings.getBoolean("loggedin", false);
	}

}
