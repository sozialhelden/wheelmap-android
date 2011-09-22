package org.wheelmap.android.ui;

import org.wheelmap.android.R;
import org.wheelmap.android.manager.SupportManager;
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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

public class StartupActivity extends Activity implements
		DetachableResultReceiver.Receiver {
	private final static String TAG = "startup";

	private State mState;
	private SupportManager mSupportManager;
	private ProgressBar mProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);

		FrameLayout layout = (FrameLayout) findViewById(R.id.startup_frame);
		Animation anim = AnimationUtils.loadAnimation(this,
				R.anim.zoom_in_animation);
		LayoutAnimationController controller = new LayoutAnimationController(
				anim, 0.0f);
		layout.setLayoutAnimation(controller);

		mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

		mState = (State) getLastNonConfigurationInstance();
		final boolean previousState = mState != null;

		if (previousState) {
			// Start listening for SyncService updates again
			mState.mReceiver.setReceiver(this);
		} else {
			mState = new State();
			mState.mReceiver.setReceiver(this);
		}
		mSupportManager = SupportManager.initOnce(getApplicationContext(),
				mState.mReceiver);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		startupApp();
	}

	private void startupApp() {
		finish();
		Intent intent = new Intent(getApplicationContext(),
				POIsListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP );
		startActivity(intent);
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Clear any strong references to this Activity, we'll reattach to
		// handle events on the other side.
		mState.mReceiver.clearReceiver();
		return mState;
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {

		if (resultCode == SyncService.STATUS_FINISHED) {
			int what = resultData.getInt(SyncService.EXTRA_WHAT);
			switch (what) {
			case SyncService.WHAT_RETRIEVE_LOCALES:
				mSupportManager.initLocales();
				mSupportManager.retrieveCategories();
				break;
			case SyncService.WHAT_RETRIEVE_CATEGORIES:
				mSupportManager.initCategories();
				mSupportManager.retrieveNodeTypes();
				break;
			case SyncService.WHAT_RETRIEVE_NODETYPES:
				mSupportManager.initNodeTypes();
				mSupportManager.createCurrentTimeTag();
				startupApp();
				break;
			default:
				// nothing to do
			}
		} else if (resultCode == SupportManager.CREATION_FINISHED) {
			Handler h = new Handler();
			h.postDelayed(new Runnable() {

				@Override
				public void run() {
					startupApp();
				}

			}, 1000);
		} else if (resultCode == SyncService.STATUS_ERROR) {
			final SyncServiceException e = resultData
					.getParcelable(SyncService.EXTRA_ERROR);
			// Log.w(TAG, e.getCause());
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

	private void showErrorDialog(SyncServiceException e) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.error_occurred);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setMessage(e.getRessourceString());
		builder.setPositiveButton(R.string.okay,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
		AlertDialog alert = builder.create();
		try {
			alert.show();
		} catch (Exception ex) {
			Toast.makeText(
					StartupActivity.this,
					getApplicationContext().getResources().getString(
							e.getRessourceString()), Toast.LENGTH_LONG).show();
		}
	}
}
