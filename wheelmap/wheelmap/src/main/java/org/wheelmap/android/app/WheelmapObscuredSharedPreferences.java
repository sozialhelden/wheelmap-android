package org.wheelmap.android.app;

import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

import oak.ObscuredSharedPreferences;

public class WheelmapObscuredSharedPreferences extends
		ObscuredSharedPreferences {

	public WheelmapObscuredSharedPreferences(Context context,
			SharedPreferences delegate) {
		super(context, delegate);
	}

	@Override
	public Set<String> getStringSet(String arg0, Set<String> arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected char[] getSpecialCode() {
		return "secretpassword".toCharArray();
	}

}
