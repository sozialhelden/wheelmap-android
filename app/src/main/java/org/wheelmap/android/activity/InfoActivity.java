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

import com.actionbarsherlock.view.MenuItem;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.TextView;
import org.wheelmap.android.fragment.InfoFragment.OnInfoListener;
import org.wheelmap.android.online.R;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

@Activity.Addons(Activity.ADDON_SHERLOCK)
public class InfoActivity extends Activity implements
        OnInfoListener {

    private TextView txt_credit_version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_info);

        String versionName;
        int versionCode;

        PackageInfo pInfo;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        txt_credit_version = (TextView)findViewById(R.id.credits_version);


        try {
            pInfo = getApplicationContext().getPackageManager().getPackageInfo(this.getPackageName(),0);

            versionName = pInfo.versionName;
            versionCode = pInfo.versionCode;

            versionName = txt_credit_version.getText().toString() + ": " + versionName + " (Build " + versionCode + ")";

            txt_credit_version.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewUri(Uri uri) {

        Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
        startActivity(intent);
    }

    @Override
    public void dismissInfoView() {
        //not used
        finish();
    }

    @Override
    public void onNextView(String view) {
        Class<? extends Activity> clzz;
        if (view.equals("LegalNotice")) {
            clzz = LegalNoticeActivity.class;
        } else {
            return;
        }

        Intent intent = new Intent(this, clzz);
        startActivity(intent);

    }
}
