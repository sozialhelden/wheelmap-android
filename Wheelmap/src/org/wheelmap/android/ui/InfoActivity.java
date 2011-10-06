/*
Copyright (C) 2011 Michal Harakal and Michael Kroez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS-IS" BASIS
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
        
*/

package org.wheelmap.android.ui;

import org.wheelmap.android.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class InfoActivity extends Activity implements OnClickListener {
	private TextView mChristophBuente;
	private ImageView mSozialhelden;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.activity_info );
		mChristophBuente = (TextView) findViewById( R.id.name_christophbuente );
		mSozialhelden = (ImageView) findViewById( R.id.logo_sozialhelden );
		
		mChristophBuente.setOnClickListener( this );
		mSozialhelden.setOnClickListener( this );
	}
	
	public void onLegalNotice(View v) {
		Intent intent = new Intent(this, LegalNoticeActivity.class);
		startActivity(intent);
		
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch(id) {
		case R.id.name_christophbuente: {
			Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.christophbuente.de" ));
			startActivity(intent);
			break;
		}
		case R.id.logo_sozialhelden: {
			Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.sozialhelden.de" ));
			startActivity(intent);
			break;
		}
		default:
			// nothing
		}
		
	}
}
