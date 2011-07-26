package org.wheelmap.android.service;

import org.wheelmap.android.RESTExecutor;

import wheelmap.org.BoundingBox;
import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;
import android.app.IntentService;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * Background {@link Service} that synchronizes data living in
 * {@link ScheduleProvider}. Reads data from both local {@link Resources} and
 * from remote sources, such as a spreadsheet.
 */
public class SyncService extends IntentService {
    private static final String TAG = "SyncService";

    public static final String EXTRA_STATUS_RECEIVER =
            "com.google.android.iosched.extra.STATUS_RECEIVER";

    public static final int STATUS_RUNNING = 0x1;
    public static final int STATUS_ERROR = 0x2;
    public static final int STATUS_FINISHED = 0x3;

    //private static final int SECOND_IN_MILLIS = (int) DateUtils.SECOND_IN_MILLIS;
    
    RESTExecutor mRemoteExecutor;
    
    public SyncService() {
        super(TAG);
    }
    
    @Override
    public void onCreate() {
        super.onCreate();

        final ContentResolver resolver = getContentResolver();
        mRemoteExecutor = new RESTExecutor(resolver);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent(intent=" + intent.toString() + ")");

        final ResultReceiver receiver = intent.getParcelableExtra(EXTRA_STATUS_RECEIVER);
        if (receiver != null) receiver.send(STATUS_RUNNING, Bundle.EMPTY);

       // final Context context = this;
        /*
        final SharedPreferences prefs = getSharedPreferences(Prefs.IOSCHED_SYNC,
                Context.MODE_PRIVATE);
        final int localVersion = prefs.getInt(Prefs.LOCAL_VERSION, VERSION_NONE);
        */

        try {
            final long startRemote = System.currentTimeMillis();
            // dummy coordinates
            mRemoteExecutor.execute(new BoundingBox(new Wgs84GeoCoordinates(13.37811,52.43752),new Wgs84GeoCoordinates(13.38278,52.43957)));
            Log.d(TAG, "remote sync took " + (System.currentTimeMillis() - startRemote) + "ms");

        } catch (Exception e) {
            Log.e(TAG, "Problem while syncing", e);

            if (receiver != null) {
                // Pass back error to surface listener
                final Bundle bundle = new Bundle();
                bundle.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(STATUS_ERROR, bundle);
            }
        }

        // Announce success to any surface listener
        Log.d(TAG, "sync finished");
        if (receiver != null) receiver.send(STATUS_FINISHED, Bundle.EMPTY);
    }
}
