package org.wheelmap.android.service;

import org.wheelmap.android.net.RESTExecutor;
import org.wheelmap.android.utils.CurrentLocation;
import org.wheelmap.android.utils.CurrentLocation.LocationResult;
import org.wheelmap.android.utils.GeocoordinatesMath;
import org.wheelmap.android.utils.ParceableBoundingBox;

import wheelmap.org.BoundingBox;
import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;
import wheelmap.org.WheelchairState;
import android.app.IntentService;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
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
            "org.wheelmap.android.STATUS_RECEIVER";
    public static final String EXTRA_STATUS_RECEIVER_BOUNCING_BOX =
        "org.wheelmap.android.EXTRA_STATUS_RECEIVER_BOUNCING_BOX";
    
    public static final int STATUS_RUNNING = 0x1;
    public static final int STATUS_ERROR = 0x2;
    public static final int STATUS_FINISHED = 0x3;

    private CurrentLocation mCurrentLocation;
	
    
    RESTExecutor mRemoteExecutor;
    
    public SyncService() {
        super(TAG);
		// current location
		mCurrentLocation = new CurrentLocation();
    }
    
    @Override
    public void onCreate() {
        super.onCreate();

        final ContentResolver resolver = getContentResolver();
        mRemoteExecutor = new RESTExecutor(resolver);
    }
    
    // execute request with filters settings
    private void retrieveDatainBoundingBox(BoundingBox bb, ResultReceiver receiver, WheelchairState wheelState) {
    	
    	try {
            final long startRemote = System.currentTimeMillis();
            // Retrieve all Pages is terribly slow. Anybody knows why?
//            mRemoteExecutor.retrieveAllPages( parcBoundingBox.toBoundingBox(), wheelState );
            mRemoteExecutor.retrieveSinglePage( bb, wheelState);
            Log.d(TAG, "remote sync took " + (System.currentTimeMillis() - startRemote) + "ms");

        } catch (Exception e) {
            Log.e(TAG, "Problem while syncing", e);

            if (receiver != null) {
                // Pass back error to surface listener
                final Bundle responsebundle = new Bundle();
                responsebundle.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(STATUS_ERROR, responsebundle);
            }
        }
    }

    
    final class MyLocationResult extends LocationResult {
    	
    	private ResultReceiver mReceiver;
    	private WheelchairState mWheelState;
    	
    	public MyLocationResult(ResultReceiver receiver, WheelchairState wheelState) {
    		mReceiver = receiver;
    		mWheelState = wheelState;
    	}
    	
    	@Override
		public void gotLocation(final Location location){
			// calculate bounding box from current location around 20 km
			BoundingBox bb = GeocoordinatesMath.calculateBoundingBox(new Wgs84GeoCoordinates(location.getLongitude(), location.getLatitude()), 20);
			retrieveDatainBoundingBox(bb, mReceiver, mWheelState);
		}
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
        
        final Bundle bundle=intent.getExtras();        
        ParceableBoundingBox parcBoundingBox = (ParceableBoundingBox)bundle.getSerializable(SyncService.EXTRA_STATUS_RECEIVER_BOUNCING_BOX);
        // TODO get wheelchair filter from shared settings
        WheelchairState wheelState = WheelchairState.UNKNOWN;
        
        
        Log.d(TAG,"parcBoundingBox received, parcBoundingBox==null ? "+(parcBoundingBox==null));
        // execute request directly for given bounding box
        if (parcBoundingBox != null) {
          Log.d(TAG,"parcBoundingBox value " + parcBoundingBox.toString());
          
          retrieveDatainBoundingBox(parcBoundingBox.toBoundingBox(), receiver, wheelState);
        }
        else {
        	// get asynchronously current location from location manager and execute request
        	MyLocationResult locationResult = new MyLocationResult(receiver, wheelState);
        	mCurrentLocation.getLocation(this, locationResult);
        	return;
        }

        // Announce success to any surface listener
        Log.d(TAG, "sync finished");
        if (receiver != null) receiver.send(STATUS_FINISHED, Bundle.EMPTY);
    }
}
