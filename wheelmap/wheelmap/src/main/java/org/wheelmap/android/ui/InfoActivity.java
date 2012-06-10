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
package org.wheelmap.android.ui;

import java.util.ArrayList;

import org.wheelmap.android.online.R;
import org.wheelmap.android.ui.info.Info;
import org.wheelmap.android.ui.info.InfoTypes;
import org.wheelmap.android.ui.info.InfoWidgetsAdapter;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class InfoActivity extends ListActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_info);
		ArrayList<Info> infoList = new ArrayList<Info>();

		Info info = null;

		// web version	   
		info = new Info(
				R.string.info_web_version, 
				R.string.info_web_version_one, 
				"http://www.wheelmap.org", InfoTypes.SIMPLE_TEXT );	   
		infoList.add( info );

		// mapdata
		info = new Info(
				R.string.info_kartendaten, 
				R.string.info_kartendaten_one,
				R.string.info_kartendaten_two,			   
				"http://www.openstreetmap.org", InfoTypes.DOUBLE_TEXT );	   
		infoList.add( info );

		// android version
		info = new Info(
				R.string.info_android_development, 
				R.string.info_android_development_one, 
				"http://fiwio.com", 
				R.string.info_android_development_two, 
				"http://studiorutton.de",				
				InfoTypes.WITH_TWO_LINKS );	   
		infoList.add( info );
		
		info = new Info(
				R.string.info_clientdevelopment, 
				R.string.info_clientdevelopment_one, 
				"", InfoTypes.SIMPLE_TEXT );	   
		infoList.add( info );

		// web development
		info = new Info(
				R.string.info_webdevelopment, 
				R.string.info_webdevelopment_one, 
				"http://www.christophbuente.de", InfoTypes.SIMPLE_TEXT );	   
		infoList.add( info );
		// legal notice
		info = new Info(
				R.string.btn_legal_notice,
				LegalNoticeActivity.class,
				InfoTypes.NEXT_ACTIVITY );	   
		infoList.add( info );
		
		// project by sozialhelden
		info = new Info(
				R.string.info_a_project_of,
				R.drawable.logo_sozialhelden_232x47,
				"http://www.sozialhelden.de",
				InfoTypes.WITH_IMAGE );	   
		infoList.add( info );
        // thanks stiftung
		info = new Info(
				R.string.stiftung_text_one,
				R.drawable.logo_fds,
				"http://www.fdst.de/",
				InfoTypes.WITH_IMAGE );	   
		infoList.add( info );


		InfoWidgetsAdapter infoAdapter = new InfoWidgetsAdapter(this, infoList); 
		setListAdapter( infoAdapter );
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Info info = (Info) this.getListAdapter().getItem(position);
		switch (info.getInfoType()) {
		case NEXT_ACTIVITY:
			Intent intent = new Intent(this, info.getActivityClass());
			startActivity(intent);
			break;
		default:
			intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(info.getUrl()));
			startActivity(intent);
		}
	}
	
	

}
