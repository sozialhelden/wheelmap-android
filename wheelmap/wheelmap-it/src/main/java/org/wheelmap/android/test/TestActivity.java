/*
 * #%L
 * Wheelmap-it - Integration tests
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
