package org.wheelmap.android.fragment;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.Toast;
import org.wheelmap.android.modules.AppProperties;
import org.wheelmap.android.modules.IAppProperties;
import org.wheelmap.android.modules.ICredentials;
import org.wheelmap.android.modules.UserCredentials;
import org.wheelmap.android.online.R;

import java.net.URI;

/**
 * Created by dschmidt on 26.03.15.
 */
public class LoginOSMFragment extends Fragment {

    public final static String TAG = LoginOSMFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String serverUrl = AppProperties.getInstance(getActivity().getApplication()).get(IAppProperties.KEY_WHEELMAP_URI);
        String url = "http://"+serverUrl+"/users/auth/osm";
        Log.d(TAG, "URL: "+url+", Agent: "+this.getAppUserAgent(getActivity().getApplication()));

        View v = inflater.inflate(R.layout.fragment_dialog_login_osm, container, false);
        WebView webView = (WebView)v.findViewById(R.id.login_osm_webview);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUserAgentString(this.getAppUserAgent(getActivity().getApplication()));
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d(TAG, "STARTED: "+url+", Agent: "+view.getSettings().getUserAgentString());
                if(url.startsWith("wheelmap://success")){
                    view.stopLoading();
                    loginSuccessful(url);
                }
                else if(url.startsWith("wheelmap://error")){
                    view.stopLoading();
                    loginError();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "FINISHED: "+url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.d(TAG, "ERROR: "+errorCode+", "+description);
            }
        });
        webView.loadUrl(url);

        return v;
    }

    public String getAppUserAgent(Application application) {
        PackageInfo pInfo;
        try {
            pInfo = application.getPackageManager().getPackageInfo(application.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            pInfo = null;
            e.printStackTrace();
        }

        final StringBuilder sb = new StringBuilder(application.getPackageName());
        if (pInfo != null) {
            sb.append("/");
            sb.append(pInfo.versionName);
        }
        return sb.toString();
    }

    private void loginSuccessful(String url) {

        try{
            Uri uri = Uri.parse(url);
            UserCredentials mCredentials = new UserCredentials(getActivity().getApplicationContext());
            mCredentials.save(uri.getQueryParameter("token"), "OSMUser");
            getActivity().setResult(Activity.RESULT_OK);
            Toast.makeText(getActivity(),R.string.login_succesfully,Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();

        }catch(NullPointerException npex){
            de.akquinet.android.androlog.Log.d("Tag:LoginOSMFragment", "NullPointException occurred");

            if(getActivity() == null){
                return;
            }

            Toast.makeText(this.getActivity().getApplicationContext(),getResources().getString(R.string.error_internal_error) , Toast.LENGTH_LONG).show();
        }
    }

    private void loginError(){
        //close webView
        getActivity().onBackPressed();
    }
}
