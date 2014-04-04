package org.wheelmap.android.activity;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;

import org.holoeverywhere.widget.LinearLayout;
import org.wheelmap.android.fragment.LoginFragment;
import org.wheelmap.android.fragment.LogoutFragment;
import org.wheelmap.android.modules.UserCredentials;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.UtilsMisc;

import org.holoeverywhere.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import de.akquinet.android.androlog.Log;

/**
 * Created by SMF on 04/04/14.
 */
public class WebViewNewsActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web_news);

        ((WebView)findViewById(R.id.webview_news)).loadUrl("http://wheelmap.org/blog/");

        if(getSupportActionBar() != null){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            default:
                //noop

        }
        return true;
    }
}