package org.wheelmap.android.ui;

import org.wheelmap.android.R;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.GeocoordinatesMath;
import org.wheelmap.android.utils.GeocoordinatesMath.DistanceUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;

public class StartupActivity extends Activity implements
		DetachableResultReceiver.Receiver {
	private final static String TAG = "startup";

	private State mState;
	private SupportManager mSupportManager;
	private ProgressBar mProgressBar;
	public static Boolean ENABLE_RESTART = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.activity_startup);

		mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

		mState = new State();
		mState.mReceiver.setReceiver(this);
		mSupportManager = SupportManager.initOnce(getApplicationContext(),
				mState.mReceiver);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		ENABLE_RESTART = false;
		startupApp();
		Log.d(TAG, "onRestart");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
	}

	private void startupApp() {
		if (ENABLE_RESTART) {
			Intent intent = new Intent(getApplicationContext(),
					POIsListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		finish();
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Log.d(TAG, "StartupActivity:onReceiveResult resultCode = " + resultCode);

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
				ENABLE_RESTART = true;
				startupApp();
				break;
			default:
				// nothing to do
			}
		} else if (resultCode == SupportManager.CREATION_FINISHED) {
			ENABLE_RESTART = true;
			startupApp();
		} else if (resultCode == SyncService.STATUS_ERROR) {
			SyncServiceException e = resultData.getParcelable( SyncService.EXTRA_ERROR );
			showErrorDialog( e );
		}
	}

	private static class State {
		public DetachableResultReceiver mReceiver;

		private State() {
			mReceiver = new DetachableResultReceiver(new Handler());
		}
	}
	
	private void showErrorDialog( SyncServiceException e ) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.error_occurred);
		builder.setIcon( android.R.drawable.ic_dialog_alert);
		builder.setMessage( e.getRessourceString());
		builder.setNeutralButton( R.string.okay, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
				
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
