package org.wheelmap.android.app;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.wheelmap.android.manager.MyLocationManager;
import org.wheelmap.android.manager.SupportManager;

import android.app.Application;
import android.util.Log;

@ReportsCrashes(formKey = "dGJWQW5PelRXWUFTbDh6VW5UYm94cXc6MQ" )
public class WheelmapApp extends Application {
	private final static String TAG = "wheelmapapp";
	
	private static WheelmapApp INSTANCE;
	private MyLocationManager mLocationManager;
	private SupportManager mSupportManager;
	
	@Override
	public void onCreate() {
		ACRA.init(this);
		super.onCreate();
		mLocationManager = MyLocationManager.initOnce( this );
		INSTANCE = this;
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		mLocationManager.clear();
	}
	
	public void setSupportManager( SupportManager manager ) {
		Log.d( TAG, "Setting new SupportManager" );
		mSupportManager = manager;
	}
	
	public static SupportManager getSupportManager() {
		if ( INSTANCE == null )
			Log.d( TAG, "INSTANCE == null - how can that be?" );
		if ( INSTANCE != null && INSTANCE.mSupportManager == null )
			Log.d(TAG, "INSTANCE != null - mSupportManager = null - how can that be?" );
		
		return INSTANCE.mSupportManager;
	}
}
