package org.wheelmap.android.ui;

import org.wheelmap.android.R;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.utils.DetachableResultReceiver;

import android.app.Activity;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.activity_startup );
		
		mProgressBar = (ProgressBar) findViewById( R.id.progressbar );
		
		mState = new State();
		mState.mReceiver.setReceiver(this);
		mSupportManager = SupportManager.initOnce(getApplicationContext(),
				mState.mReceiver);
	}

	private void startupApp() {
		Intent intent = new Intent(getApplicationContext(),
				POIsListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
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
				startupApp();
				break;
			default:
				// nothing to do
			}
		} else if (resultCode == SupportManager.CREATION_FINISHED) {
			startupApp();
		} else if (resultCode == SyncService.STATUS_ERROR) {
			// need some error handling
		}
	}

	private static class State {
		public DetachableResultReceiver mReceiver;

		private State() {
			mReceiver = new DetachableResultReceiver(new Handler());
		}
	}
}
