package org.wheelmap.android.model;

import android.content.Context;
import android.content.SharedPreferences;

public class UserCredentials {

	// TODO refactor constants for preferencies into one class
	public static final String PREFS_NAME = "credentials";


	private String mLogin;
	private String mPassword;
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
		// to do check if password valid
		save();
	}
	
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
		editor.putBoolean("loggedin", mIsLoggenIn);
		// Commit the edits!
		editor.commit();
	}

	private void load(){
		// Restore preferences
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
		mLogin = settings.getString("login", "");
		mPassword = settings.getString("password", "");
		mIsLoggenIn = settings.getBoolean("loggedin", false);
	}

}
