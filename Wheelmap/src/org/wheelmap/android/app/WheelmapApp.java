package org.wheelmap.android.app;

import org.wheelmap.android.manager.MyLocationManager;
import org.wheelmap.android.manager.SupportManager;

import android.app.Application;

public class WheelmapApp extends Application {
	
	private MyLocationManager mLocationManager;
	private SupportManager mSupportManager;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mLocationManager = MyLocationManager.initOnce( this );
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		mLocationManager.clear();
	}
	
	public void setSupportManager( SupportManager manager ) {
		mSupportManager = manager;
	}
}
