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
package org.wheelmap.android.activity;

import java.util.List;

import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import org.wheelmap.android.app.AppCapability;
import org.wheelmap.android.app.IAppProperties;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Extra.What;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.UtilsMisc;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.google.inject.Inject;

import de.akquinet.android.androlog.Log;

public class StartupActivity extends RoboSherlockActivity implements
		DetachableResultReceiver.Receiver {
	private final static String TAG = StartupActivity.class.getSimpleName();

	@Inject
	IAppProperties appProperties;

	private State mState;
	private SupportManager mSupportManager;
	private ProgressBar mProgressBar;
	private boolean mIsInForeground;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.activity_startup);
		FrameLayout layout = (FrameLayout) findViewById(R.id.startup_frame);
		Animation anim = AnimationUtils.loadAnimation(this,
				R.anim.zoom_in_animation);
		LayoutAnimationController controller = new LayoutAnimationController(
				anim, 0.0f);
		layout.setLayoutAnimation(controller);

		mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
		mState = new State();
		mState.mReceiver.setReceiver(this);

		if (AppCapability.isNotWorking()) {
			showDialogNotWorking();
			return;
		}

		mSupportManager = WheelmapApp.getSupportManager();
		if (mSupportManager.needsReloading()) {
			mSupportManager.reload(mState.mReceiver);
			return;
		}

		if (needStartApp())
			startupAppDelayed();
		else
			finish();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(TAG, "onRestart");
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mIsInForeground = true;
		Log.d(TAG, "onResume isInForeground = " + mIsInForeground);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mIsInForeground = false;
		Log.d(TAG, "onPause isInForeground = " + mIsInForeground);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mSupportManager != null)
			mSupportManager.releaseReceiver();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// this prevents StartupActivity recreation on Configuration changes
		// (device orientation changes or hardware keyboard open/close).
		// just do nothing on these changes:
		super.onConfigurationChanged(null);
	}

	private boolean needStartApp() {
		final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final List<RunningTaskInfo> tasksInfo = am.getRunningTasks(1024);

		if (!tasksInfo.isEmpty()) {
			final String ourAppPackageName = getPackageName();
			RunningTaskInfo taskInfo;
			final int size = tasksInfo.size();
			for (int i = 0; i < size; i++) {
				taskInfo = tasksInfo.get(i);
				if (ourAppPackageName.equals(taskInfo.baseActivity
						.getPackageName())) {
					// continue application start only if there is the only
					// Activity in the task
					// (BTW in this case this is the StartupActivity)
					return taskInfo.numActivities == 1;
				}
			}
		}

		return true;
	}

	private void startupAppDelayed() {
		Handler h = new Handler();
		h.postDelayed(new Runnable() {

			@Override
			public void run() {
				startupApp();
			}

		}, 1000);
	}

	private void startupApp() {
		Intent intent;

		if (UtilsMisc.isTablet(getApplicationContext()))
			intent = new Intent(getApplicationContext(),
					MainMultiPaneActivity.class);
		else
			intent = new Intent(getApplicationContext(),
					MainSinglePaneActivity.class);

		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {

		if (resultCode == SyncService.STATUS_FINISHED) {
			int what = resultData.getInt(Extra.WHAT);
			switch (what) {
			case What.RETRIEVE_LOCALES:
				mSupportManager.reloadStageTwo();
				break;
			case What.RETRIEVE_CATEGORIES:
				mSupportManager.reloadStageThree();
				break;
			case What.RETRIEVE_NODETYPES:
				mSupportManager.reloadStageFour();
				startupAppDelayed();
				break;
			default:
				// nothing to do
			}
		} else if (resultCode == SyncService.STATUS_ERROR) {
			final SyncServiceException e = resultData
					.getParcelable(Extra.EXCEPTION);
			Log.w(TAG, e);
			mProgressBar.setVisibility(View.GONE);
			showErrorDialog(e);
		}
	}

	private static class State {
		public DetachableResultReceiver mReceiver;

		private State() {
			mReceiver = new DetachableResultReceiver(new Handler());
		}
	}

	private void showDialogNotWorking() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setTitle(R.string.error_title_occurred);
		builder.setMessage(getResources().getString(
				R.string.error_not_enough_memory));
		builder.setPositiveButton(R.string.btn_quit,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private void showErrorDialog(SyncServiceException e) {
		if (!mIsInForeground)
			return;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (e.getErrorCode() == SyncServiceException.ERROR_NETWORK_FAILURE)
			builder.setTitle(R.string.error_network_title);
		else
			builder.setTitle(R.string.error_title_occurred);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setMessage(e.getRessourceString());
		builder.setPositiveButton(R.string.btn_quit,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();

	}
}
