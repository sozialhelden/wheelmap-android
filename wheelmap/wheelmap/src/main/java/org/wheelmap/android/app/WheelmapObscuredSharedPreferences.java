package org.wheelmap.android.app;

import android.content.Context;
import android.content.SharedPreferences;
import oak.ObscuredSharedPreferences;

import java.util.Set;

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
