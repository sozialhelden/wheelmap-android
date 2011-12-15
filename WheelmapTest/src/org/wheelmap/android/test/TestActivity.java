package org.wheelmap.android.test;
import org.wheelmap.android.ui.LoginActivity;
import org.wheelmap.android.ui.POIDetailActivityEditable;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class TestActivity extends Activity {
	private final static String TAG = "logintest";
	private static final int PERFORM_LOGIN = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = new Intent( this, LoginActivity.class );
		startActivityForResult( intent, PERFORM_LOGIN );
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
		Log.d(TAG, "onActivityResult requestCode = " + requestCode + " resultCode = " + resultCode );
	}
}
