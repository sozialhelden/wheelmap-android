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

import org.wheelmap.android.analytics.AnalyticsTrackingManager;
import org.wheelmap.android.fragment.InfoFragment.OnInfoListener;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.UtilsMisc;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class InfoActivity extends AppCompatActivity implements OnInfoListener {

    private TextView txt_credit_version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_info);

        if (UtilsMisc.isTablet(this)) {
            int widthDp = 300;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                widthDp = 500;
            }
            findViewById(R.id.fragment_info).setMinimumWidth((int) UtilsMisc.dbToPx(getResources(), widthDp));
        }

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

        AnalyticsTrackingManager.trackScreen(AnalyticsTrackingManager.TrackableScreensName.INFOSCREEN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar bar = getSupportActionBar();
        if(bar == null){
            return true;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        final View customView = inflater.inflate(R.layout.actionbar_tablet,
                null);

        bar.setCustomView(customView, new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        bar.setDisplayShowCustomEnabled(true);

        return true;
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
        Class<? extends AppCompatActivity> clzz;
        if (view.equals("LegalNotice")) {
            clzz = LegalNoticeActivity.class;
        } else {
            return;
        }

        Intent intent = new Intent(this, clzz);
        startActivity(intent);

    }
}
