package org.wheelmap.android.activity;

import org.wheelmap.android.activity.base.BaseActivity;
import org.wheelmap.android.app.WheelmapApp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by SMF on 28/03/14.
 */
public class POIPermaLinkActivity extends BaseActivity {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getIntent() == null) {
             return;
        }
        String uri  = this.getIntent().getDataString();
        if(uri == null){
            finish();
        }
        String[]  uriArray = uri.split("/");

        uri = uriArray[uriArray.length-1];

        WheelmapApp app = (WheelmapApp) getApplication();
        app.setUriString(uri);

        startActivity(new Intent(this, StartupActivity.class));
        finish();
    }
}