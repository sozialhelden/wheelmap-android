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
package org.wheelmap.android.activity;

import org.wheelmap.android.fragment.InfoFragment.OnInfoListener;
import org.wheelmap.android.online.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class InfoActivity extends SherlockFragmentActivity implements
		OnInfoListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment_info);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
	}

	@Override
	public void onViewUri(Uri uri) {

		Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
		startActivity(intent);
	}

	@Override
	public void onNextView(String view) {
		Class<? extends SherlockFragmentActivity> clzz;
		if (view.equals("LegalNotice"))
			clzz = LegalNoticeActivity.class;
		else
			return;

		Intent intent = new Intent(this, clzz);
		startActivity(intent);

	}
}
