package org.wheelmap.android.service;

import org.wheelmap.android.net.ApiKeyExecutor;
import org.wheelmap.android.net.CategoriesExecutor;
import org.wheelmap.android.net.IExecutor;
import org.wheelmap.android.net.LocalesExecutor;
import org.wheelmap.android.net.NodeTypesExecutor;
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
	public static final String EXTRA_LOCALE = "org.wheelmap.android.EXTRA_LOCALE";
	public static final String EXTRA_CATEGORY = "org.wheelmap.android.EXTRA_CATEGORY";
	public static final String EXTRA_NODETYPE = "org.wheelmap.android.EXTRA_NODETYPE";
	public static final String EXTRA_USERNAME = "org.wheelmap.android.EXTRA_USERNAME";
	public static final String EXTRA_PASSWORD = "org.wheelmap.android.EXTRA_PASSWORD";
	public static final String EXTRA_ERROR = "org.wheelmap.android.EXTRA_ERROR";
	
	public static final String EXTRA_WHAT = "org.wheelmap.android.EXTRA_WHAT";
	public static final int WHAT_RETRIEVE_NODES = 0x1;
	public static final int WHAT_RETRIEVE_LOCALES = 0x2;
	public static final int WHAT_RETRIEVE_CATEGORIES = 0x3;
	public static final int WHAT_RETRIEVE_NODETYPES = 0x4;
	public static final int WHAT_UPDATE_SERVER = 0x5;
	public static final int WHAT_RETRIEVE_APIKEY = 0x6;

	public static final int STATUS_RUNNING = 0x1;
	public static final int STATUS_ERROR = 0x2;
	public static final int STATUS_FINISHED = 0x3;
	
	public static final String PREFS_API_KEY = "prefs_apikey";

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
		Log.d(TAG, "onHandleIntent(intent=" + intent.getIntExtra( EXTRA_WHAT, -1 ) + ")");

		final ResultReceiver receiver = intent
				.getParcelableExtra(EXTRA_STATUS_RECEIVER);
		if (receiver != null)
			receiver.send(STATUS_RUNNING, Bundle.EMPTY);
		
		final Bundle bundle = intent.getExtras();

		int what = bundle.getInt(EXTRA_WHAT);
		IExecutor executor = null;
		switch(what) {
		case WHAT_RETRIEVE_NODES:
			executor = new NodesExecutor( mResolver, bundle);
			break;
		case WHAT_RETRIEVE_LOCALES:
			executor = new LocalesExecutor(mResolver, bundle );
			break;
		case WHAT_RETRIEVE_CATEGORIES:
			executor = new CategoriesExecutor( mResolver, bundle );
			break;
		case WHAT_RETRIEVE_NODETYPES:
			executor = new NodeTypesExecutor( mResolver, bundle );
			break;
		case WHAT_UPDATE_SERVER:
			executor = new NodeUpdateOrNewExecutor( getApplicationContext(), mResolver );
			break;
		case WHAT_RETRIEVE_APIKEY:
			executor = new ApiKeyExecutor(getApplicationContext(), mResolver, bundle);
			break;
		default:
			return; // noop no instruction, no operation;
		}
		
		executor.prepareContent();
		try {
			executor.execute();
			executor.prepareDatabase();
		} catch ( SyncServiceException e ) {
		
			Log.e(TAG, "Problem while executing", e);
			if (receiver != null) {
				// Pass back error to surface listener
				final Bundle responsebundle = new Bundle();
				responsebundle.putParcelable( SyncService.EXTRA_ERROR, e );
				responsebundle.putInt( EXTRA_WHAT, what );
				receiver.send(STATUS_ERROR, responsebundle);
				return;
			}
		}
		
//		Log.d(TAG, "sync finished");
		if (receiver != null) {
			Log.d( TAG, "sending STATUS_FINISHED" );
			final Bundle responsebundle = new Bundle();
			responsebundle.putInt( EXTRA_WHAT, what );
			receiver.send(STATUS_FINISHED, responsebundle);
		}
	}

}
