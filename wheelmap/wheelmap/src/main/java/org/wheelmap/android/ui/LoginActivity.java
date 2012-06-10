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
package org.wheelmap.android.ui;

import org.wheelmap.android.online.R;
import org.wheelmap.android.model.UserCredentials;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.utils.DetachableResultReceiver;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class LoginActivity extends Activity implements
		DetachableResultReceiver.Receiver {
	private final static String TAG = "poidetail";
	private State mState;

	private EditText mEmailText;
	private EditText mPasswordText;
	private ProgressBar mProgressBar;
	
	private boolean isInForeground;
	private boolean isShowingDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		mEmailText = (EditText) findViewById(R.id.login_email);
		mPasswordText = (EditText) findViewById(R.id.login_password);
		mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

		LinearLayout layout = (LinearLayout) findViewById(R.id.login_layout);
		Animation anim = AnimationUtils.loadAnimation(this,
				R.anim.move_in_from_top);
		LayoutAnimationController controller = new LayoutAnimationController(
				anim, 0.0f);
		layout.setLayoutAnimation(controller);

		mState = (State) getLastNonConfigurationInstance();
		final boolean previousState = mState != null;

		if (previousState) {
			// Start listening for SyncService updates again
			mState.mReceiver.setReceiver(this);
			updateRefreshStatus();
		} else {
			mState = new State();
			mState.mReceiver.setReceiver(this);
		}

		load();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		isInForeground = true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		isInForeground = false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	setResult( RESULT_CANCELED );
	        finish();
	    }
	    return super.onKeyDown(keyCode, event);
	}


	public void onSubmit(View v) {
		String email = mEmailText.getText().toString();
		String password = mPasswordText.getText().toString();
		login(email, password);
	}

	private void login(String email, String password) {
		final Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
				SyncService.class);
		intent.putExtra(SyncService.EXTRA_WHAT,
				SyncService.WHAT_RETRIEVE_APIKEY);
		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mState.mReceiver);
		intent.putExtra(SyncService.EXTRA_EMAIL, email);
		intent.putExtra(SyncService.EXTRA_PASSWORD, password);

		startService(intent);
	}

	private void load() {
		UserCredentials userCredentials = new UserCredentials(this);

		// get user credentials form LoginManager
		String login = userCredentials.getLogin();
		String password = userCredentials.getPassword();
		
		if ( userCredentials.isLoggedIn()) {
			mEmailText.setText( login );
			mPasswordText.setText( password );
		}
	}

	private void updateRefreshStatus() {
		if (mState.mSyncing) {
			mProgressBar.setVisibility(View.VISIBLE);
		} else {
			mProgressBar.setVisibility(View.INVISIBLE);
		}
	}

	/** {@inheritDoc} */
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Log.d(TAG, "onReceiveResult in list resultCode = " + resultCode);
		switch (resultCode) {
		case SyncService.STATUS_RUNNING: {
			mState.mSyncing = true;
			updateRefreshStatus();
			break;
		}
		case SyncService.STATUS_FINISHED: {
			mState.mSyncing = false;
			updateRefreshStatus();
			setResult(RESULT_OK, null );
			finish();
			break;
		}
		case SyncService.STATUS_ERROR: {
			// Error happened down in SyncService, show as toast.
			mState.mSyncing = false;
			updateRefreshStatus();
			final SyncServiceException e = resultData
					.getParcelable(SyncService.EXTRA_ERROR);
			showErrorDialog(e);
			break;
		}

		}
	}

	private static class State {
		public DetachableResultReceiver mReceiver;
		public boolean mSyncing = false;

		private State() {
			mReceiver = new DetachableResultReceiver(new Handler());
		}
	}
	
	private void showErrorDialog(SyncServiceException e) {
		if (!isInForeground)
			return;
		if (isShowingDialog)
			return;
		
		isShowingDialog = true;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Log.d(TAG, "showErrorDialog: e.getCode = " + e.getErrorCode());
		if (e.getErrorCode() == SyncServiceException.ERROR_NETWORK_FAILURE)
			builder.setTitle(R.string.error_network_title);
		else
			builder.setTitle(R.string.error_occurred);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setMessage(e.getRessourceString());
		builder.setNeutralButton(R.string.okay,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						isShowingDialog = false;
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
