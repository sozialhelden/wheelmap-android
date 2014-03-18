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
package org.wheelmap.android.fragment;

import com.google.inject.Inject;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.app.DialogFragment;
import org.holoeverywhere.app.Fragment;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.modules.ICredentials;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.RestService;
import org.wheelmap.android.service.RestServiceException;
import org.wheelmap.android.service.RestServiceHelper;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.UtilsMisc;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import de.akquinet.android.androlog.Log;

public class LoginFragment extends Fragment implements
        OnClickListener, DetachableResultReceiver.Receiver,
        OnEditorActionListener {

    public final static String TAG = LoginFragment.class.getSimpleName();

    private EditText mEmailText;

    private EditText mPasswordText;

    private TextView mRegisterText;

    private ProgressBar mProgressBar;

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
        mReceiver = new DetachableResultReceiver(new Handler());
        mReceiver.setReceiver(this);


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

        TextView forgot_password = (TextView)v.findViewById(R.id.login_forgot_password);
        String forgot_password_format = "<a href=\"http://wheelmap.org/users/password/new\">%s</a>";
        forgot_password.setText(Html.fromHtml(
           String.format(forgot_password_format, forgot_password.getText())));
        forgot_password.setLinksClickable(true);
        forgot_password.setMovementMethod(LinkMovementMethod.getInstance());

        TextView login2 = (TextView)v.findViewById(R.id.login_login_2);
        String login2_format = "<a href=\""+getString(R.string.login_link_wheelmap)+"\">%s</a>";
        login2.setText(Html.fromHtml(
                String.format(login2_format, login2.getText())));
        login2.setLinksClickable(true);
        login2.setMovementMethod(LinkMovementMethod.getInstance());

        mEmailText = (EditText) v.findViewById(R.id.login_email);
        mPasswordText = (EditText) v.findViewById(R.id.login_password);

        mEmailText.setText("");
        mPasswordText.setText("");

        return v;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnLoginDialogListener) {
            mListener = (OnLoginDialogListener) activity;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        /*
        AlertDialog dialog = (AlertDialog) getDialog();
        Button button = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        button.setOnClickListener(this);
        mEmailText = (EditText) dialog.findViewById(R.id.login_email);
        mEmailText.setOnEditorActionListener(this);
        mPasswordText = (EditText) dialog.findViewById(R.id.login_password);
        mPasswordText.setOnEditorActionListener(this);
        String formattedHtml = UtilsMisc.formatHtmlLink(
                getString(R.string.login_link_wheelmap),
                getString(R.string.login_link_text));
        Spanned spannedText = Html.fromHtml(formattedHtml);
        mRegisterText = (TextView) dialog.findViewById(R.id.login_register);
        mRegisterText.setText(spannedText);
        mRegisterText.setMovementMethod(LinkMovementMethod.getInstance());
        */
        //load();
        //mProgressBar = (ProgressBar) dialog.findViewById(R.id.progressbar);

    }

    private void load() {
        mEmailText.setText("");
        mPasswordText.setText("");
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        Log.d(TAG, "onReceiveResult in list resultCode = " + resultCode);
        switch (resultCode) {
            case RestService.STATUS_RUNNING:
                mSyncing = true;
                updateRefreshStatus();
                break;
            case RestService.STATUS_FINISHED:
                mSyncing = false;
                updateRefreshStatus();
                loginSuccessful();
                break;
            case RestService.STATUS_ERROR:
                // Error happened down in RestService, show as crouton.
                mSyncing = false;
                updateRefreshStatus();
                final RestServiceException e = resultData
                        .getParcelable(Extra.EXCEPTION);

                FragmentManager fm = getFragmentManager();
                ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance(e,
                        Extra.UNKNOWN);
                if (errorDialog == null) {
                    return;
                }

                errorDialog.show(fm, ErrorDialogFragment.TAG);
                break;
            default: // noop
        }

    }

    private void updateRefreshStatus() {
        if (mSyncing) {
            //mProgressBar.setVisibility(View.VISIBLE);
        } else {
            //mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void loginSuccessful() {
        getActivity().onBackPressed();
        if (mListener != null) {
            mListener.onLoginSuccessful();
        }
    }

    @Override
    public void onClick(View v) {
        login();
    }

    private void login() {
        String email = mEmailText.getText().toString();
        String password = mPasswordText.getText().toString();

        if (email.length() == 0 || password.length() == 0) {
            return;
        }

        RestServiceHelper.executeRetrieveApiKey(getActivity(), email, password,
                mReceiver);
    }

    private boolean checkInputFields(TextView v) {
        if (v.getText().toString().length() == 0) {
            return false;
        }

        EditText otherText;
        if (v == mEmailText) {
            otherText = mPasswordText;
        } else {
            otherText = mEmailText;
        }

        if (otherText.getText().toString().length() == 0) {
            otherText.requestFocus();
            return false;
        }

        return true;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            if (checkInputFields(v)) {
                login();
            }
            return true;
        }

        return false;
    }
}
