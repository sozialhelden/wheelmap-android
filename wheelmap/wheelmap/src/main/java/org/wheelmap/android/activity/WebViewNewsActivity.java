package org.wheelmap.android.activity;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;

import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.ProgressBar;
import org.wheelmap.android.fragment.LoginFragment;
import org.wheelmap.android.fragment.LogoutFragment;
import org.wheelmap.android.modules.UserCredentials;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.UtilsMisc;

import org.holoeverywhere.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import de.akquinet.android.androlog.Log;

/**
 * Created by SMF on 04/04/14.
 */
public class WebViewNewsActivity extends Activity {

    WebView webView;
    ProgressBar progress;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web_news);

         progress = (ProgressBar) findViewById(R.id.progress);

        webView = (WebView)  findViewById(R.id.webview_news);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progress.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progress.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.loadUrl("http://wheelmap.org/category/news/");

        if(getSupportActionBar() != null){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {

        if(webView.canGoBack()){
             webView.goBack();
            return;
        }

        super.onBackPressed();
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