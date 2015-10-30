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
package org.wheelmap.android.fragment.profile;

import org.wheelmap.android.activity.profile.LoginWebActivity;
import org.wheelmap.android.fragment.ErrorDialogFragment;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.online.BuildConfig;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.RestService;
import org.wheelmap.android.service.RestServiceException;
import org.wheelmap.android.service.RestServiceHelper;
import org.wheelmap.android.utils.Constants;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.UtilsMisc;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import de.akquinet.android.androlog.Log;

public class LoginFragment extends Fragment {

    public final static String TAG = LoginFragment.class.getSimpleName();

    private boolean mSyncing;

    private DetachableResultReceiver mReceiver;

    private OnLoginDialogListener mListener;

    public interface OnLoginDialogListener {

        public void onLoginSuccessful();

        public void onLoginCancelled();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_dialog_login, container, false);

        v.findViewById(R.id.button_login).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        v.findViewById(R.id.button_login_register).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        if(!UtilsMisc.isTablet(getActivity().getApplicationContext())){
            View scrollView = v.findViewById(R.id.scrollView);
            ViewGroup.LayoutParams params = scrollView.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            scrollView.setLayoutParams(params);
        }

        return v;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnLoginDialogListener) {
            mListener = (OnLoginDialogListener) activity;
        }
    }

    private void login() {
        startActivity(new Intent(getActivity(), LoginWebActivity.class));
    }

    private void register() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String url = BuildConfig.API_BASE_URL+ Constants.Api.WM_REGISTER_LINK;
        if (!url.startsWith("http")) {
            url = "http://"+ url;
        }
        intent.setData(Uri.parse(url));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

}
