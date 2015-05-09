package org.wheelmap.android.fragment;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;
import org.wheelmap.android.modules.AppProperties;
import org.wheelmap.android.modules.IAppProperties;
import org.wheelmap.android.modules.UserCredentials;
import org.wheelmap.android.online.R;

/**
 * Created by dschmidt on 26.03.15.
 */
public class LoginDescriptionFragment extends Fragment {

    public final static String TAG = LoginDescriptionFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_login_description_osm, container, false);

        Button login = (Button)v.findViewById(R.id.fragment_login_description_btn_login);
        Button register = (Button)v.findViewById(R.id.fragment_login_description_btn_register);
        TextView whyOSM = (TextView)v.findViewById(R.id.fragment_login_description_tv_why);

        final FragmentManager fm = getActivity().getSupportFragmentManager();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fm.beginTransaction()
                        .add(R.id.content, new LoginOSMFragment(),
                                LoginOSMFragment.TAG).commit();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uriUrl = Uri.parse("http://wheelmap.org/en/oauth/register_osm");
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
            }
        });

        whyOSM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fm.beginTransaction()
                        .add(R.id.content, new LoginWhyOSMFragment(),
                                LoginWhyOSMFragment.TAG).commit();
            }
        });

        return v;
    }
}
