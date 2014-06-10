package org.wheelmap.android.activity;

import org.wheelmap.android.app.WheelmapApp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;

import java.io.UnsupportedEncodingException;

/**
 * Created by SMF on 10/06/14.
 */
public class POIPermaLinkMapActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String uri  = this.getIntent().getDataString();
        String[]  uriArray = uri.split("\\?");

        uri = uriArray[0];
        String uriAdress = uriArray[1];

        WheelmapApp app = (WheelmapApp) getApplication();

        uriArray = uri.split(":");
        uri = uriArray[1];
        app.setGeoString(uri);

        uriArray = uriAdress.split("=");
        uriAdress = uriArray[1];
        uri = uriAdress.replaceAll("\\+", " ");


        byte ptext[] = uri.getBytes();

        String s = null;
        try {
            s = new String(ptext, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        app.setAddressString(s);

        startActivity(new Intent(this, StartupActivity.class));
    }
}