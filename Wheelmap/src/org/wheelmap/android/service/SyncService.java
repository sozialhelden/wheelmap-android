package org.wheelmap.android.service;

import org.wheelmap.android.net.ExecutorException;
import org.wheelmap.android.net.IExecutor;
import org.wheelmap.android.net.NodesExecutor;
import org.wheelmap.android.net.NodeUpdateOrNewExecutor;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * Background {@link Service} that synchronizes data living in
 * {@link POIsProvider}. Reads data from remote source
 */
public class SyncService extends IntentService {
	private static final String TAG = "SyncService";

	public static final String EXTRA_STATUS_RECEIVER = "org.wheelmap.android.EXTRA_STATUS_RECEIVER";
	public static final String EXTRA_BOUNDING_BOX = "org.wheelmap.android.EXTRA_BOUNDING_BOX";
	public static final String EXTRA_LOCATION = "org.wheelmap.android.EXTRA_LOCATION";
	public static final String EXTRA_DISTANCE_LIMIT = "org.wheelmap.android.EXTRA_DISTANCE_LIMIT";

	public static final String EXTRA_WHAT = "org.wheelmap.android.EXTRA_WHAT";
	public static final int WHAT_RETRIEVE_NODES = 0x1;
	public static final int WHAT_UPDATE_SERVER = 0x2;

	public static final int STATUS_RUNNING = 0x1;
	public static final int STATUS_ERROR = 0x2;
	public static final int STATUS_FINISHED = 0x3;

	private ContentResolver mResolver;
	
	public SyncService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		 mResolver = getContentResolver();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent(intent=" + intent.toString() + ")");

		final ResultReceiver receiver = intent
				.getParcelableExtra(EXTRA_STATUS_RECEIVER);
		if (receiver != null)
			receiver.send(STATUS_RUNNING, Bundle.EMPTY);
		
		final Bundle bundle = intent.getExtras();

		int what = bundle.getInt(EXTRA_WHAT);
		IExecutor executor = null;
		if (what == WHAT_RETRIEVE_NODES) {
			executor = new NodesExecutor(this, mResolver, bundle);
		} else if ( what == WHAT_UPDATE_SERVER ) {
			executor = new NodeUpdateOrNewExecutor( mResolver );
		}
		
		executor.prepareContent();
		try {
			executor.execute();
		} catch (ExecutorException e) {
			Log.e(TAG, "Problem while syncing", e);
			if (receiver != null) {
				// Pass back error to surface listener
				final Bundle responsebundle = new Bundle();
				responsebundle.putString(Intent.EXTRA_TEXT, e.toString());
				receiver.send(STATUS_ERROR, responsebundle);
				return;
			}
		}
		executor.prepareDatabase();
		Log.d(TAG, "sync finished");
		if (receiver != null)
			receiver.send(STATUS_FINISHED, Bundle.EMPTY);
	}

	
}
