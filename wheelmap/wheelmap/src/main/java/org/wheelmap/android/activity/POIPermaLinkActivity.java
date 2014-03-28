package org.wheelmap.android.activity;

import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.modules.AppProperties;
import org.wheelmap.android.modules.IAppProperties;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by SMF on 28/03/14.
 */
public class POIPermaLinkActivity extends Activity {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String uri  = this.getIntent().getDataString();
        String[]  uriArray = uri.split("/");

        uri = uriArray[uriArray.length-1];

        WheelmapApp app = (WheelmapApp) getApplication();
        app.setUriString(uri);

        startActivity(new Intent(this, StartupActivity.class));
    }
}