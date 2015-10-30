package org.wheelmap.android.activity.profile;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.wheelmap.android.modules.ICredentials;
import org.wheelmap.android.modules.UserCredentials;
import org.wheelmap.android.online.BuildConfig;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.Constants;
import org.wheelmap.android.utils.UtilsMisc;

/**
 *
 * Created by timfreiheit on 30.10.15.
 */
public class LoginWebActivity extends AppCompatActivity {

    WebView webView;
    ProgressBar progress;

    private ICredentials mCredentials;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCredentials = new UserCredentials(this);

        if (UtilsMisc.isTablet(getApplicationContext())) {
            UtilsMisc.showAsPopup(this);
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.webview);

        progress = (ProgressBar) findViewById(R.id.progress);

        webView = (WebView)  findViewById(R.id.webview);
        webView.getSettings().setUserAgentString(BuildConfig.APPLICATION_ID);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.clearCache(true);
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        webView.setWebViewClient(mWebViewClient);

        String url = BuildConfig.API_BASE_URL + Constants.Api.LOGIN;
        if (!BuildConfig.API_BASE_URL.startsWith("http")) {
            url = "http://" + url;
        }
        webView.loadUrl(url);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(R.string.login_activity_title);
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

    private void handleLoginInformationFromUri(Uri uri) {
        if (saveUserCredentials(uri)) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    private boolean saveUserCredentials(Uri uri){
        String email = uri.getQueryParameter("email");
        String token = uri.getQueryParameter("token");
        email = email != null ? email : "";
        if (TextUtils.isEmpty(token)) {
            return false;
        }
        mCredentials.save(token, email);
        return true;
    }

    private WebViewClient mWebViewClient = new WebViewClient(){

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
            Uri uri = Uri.parse(url);
            if (uri.getScheme().equalsIgnoreCase(Constants.Api.SCHEMA_WHEELMAP)) {
                handleLoginInformationFromUri(uri);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    };
}
