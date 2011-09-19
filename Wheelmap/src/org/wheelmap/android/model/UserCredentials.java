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

	public void login(String login, String password) {
		mLogin = login;
		mPassword = password;
		mIsLoggenIn = true;
		// TODO make request to server to get the API_key for login/password
		mApiKey = getApiKey();
		save();
	}
	
	private String getApiKey() {
		return "jWeAsb34CJq4yVAryjtc";
	};
	
	public void logout() {
		mIsLoggenIn = false;
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
