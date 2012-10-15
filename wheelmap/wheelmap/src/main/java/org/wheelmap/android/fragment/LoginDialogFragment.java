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

import org.wheelmap.android.model.Extra;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.service.SyncServiceHelper;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.UtilsMisc;

import android.app.Activity;
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
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.WazaBe.HoloEverywhere.app.AlertDialog;
import com.WazaBe.HoloEverywhere.app.Dialog;
import com.actionbarsherlock.app.SherlockDialogFragment;

import de.akquinet.android.androlog.Log;

public class LoginDialogFragment extends SherlockDialogFragment implements
		OnClickListener, DetachableResultReceiver.Receiver,
		OnEditorActionListener {
	public final static String TAG = LoginDialogFragment.class.getSimpleName();

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
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof OnLoginDialogListener) {
			mListener = (OnLoginDialogListener) activity;
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.title_login);
		builder.setIcon(R.drawable.ic_login_wm_holo_light);
		builder.setNeutralButton(R.string.login_submit, null);
		builder.setOnCancelListener(this);

		View view = getActivity().getLayoutInflater().inflate(
				R.layout.fragment_dialog_login, null);
		builder.setView(view);

		Dialog d = builder.create();
		return d;
	}

	@Override
	public void onResume() {
		super.onResume();

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
		load();
		mProgressBar = (ProgressBar) dialog.findViewById(R.id.progressbar);

	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);

		if (mListener != null)
			mListener.onLoginCancelled();
	}

	private void load() {
		mEmailText.setText("");
		mPasswordText.setText("");
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Log.d(TAG, "onReceiveResult in list resultCode = " + resultCode);
		switch (resultCode) {
		case SyncService.STATUS_RUNNING:
			mSyncing = true;
			updateRefreshStatus();
			break;
		case SyncService.STATUS_FINISHED:
			mSyncing = false;
			updateRefreshStatus();
			loginSuccessful();
			break;
		case SyncService.STATUS_ERROR:
			// Error happened down in SyncService, show as toast.
			mSyncing = false;
			updateRefreshStatus();
			final SyncServiceException e = resultData
					.getParcelable(Extra.EXCEPTION);
			showErrorDialog(e);
			break;
		default: // noop
		}

	}

	private void updateRefreshStatus() {
		if (mSyncing)
			mProgressBar.setVisibility(View.VISIBLE);
		else
			mProgressBar.setVisibility(View.INVISIBLE);
	}

	private void showErrorDialog(SyncServiceException e) {
		FragmentManager fm = getFragmentManager();
		ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance(e,
				Extra.UNKNOWN);
		if (errorDialog == null)
			return;

		errorDialog.show(fm, ErrorDialogFragment.TAG);
	}

	private void loginSuccessful() {
		dismiss();
		if (mListener != null)
			mListener.onLoginSuccessful();
	}

	@Override
	public void onClick(View v) {
		login();
	}

	private void login() {
		String email = mEmailText.getText().toString();
		String password = mPasswordText.getText().toString();

		if (email.length() == 0 || password.length() == 0)
			return;

		SyncServiceHelper.executeRetrieveApiKey(getActivity(), email, password,
				mReceiver);
	}

	private boolean checkInputFields(TextView v) {
		if (v.getText().toString().length() == 0)
			return false;

		EditText otherText;
		if (v == mEmailText)
			otherText = mPasswordText;
		else
			otherText = mEmailText;

		if (otherText.getText().toString().length() == 0) {
			otherText.requestFocus();
			return false;
		}

		return true;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (EditorInfo.IME_ACTION_DONE == actionId) {
			if (checkInputFields(v))
				login();
			return true;
		}

		return false;
	}
}
