package org.wheelmap.android.app;

import org.wheelmap.android.manager.MapFileManager;
import org.wheelmap.android.manager.MyLocationManager;

import android.app.Application;

public class WheelmapApp extends Application {
	private MyLocationManager locationManager;
	
	@Override
	public void onCreate() {
		super.onCreate();
		locationManager = MyLocationManager.initOnce( this );
		// MapFileManager.get( this ).updateLocal();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		locationManager.clear();
	}
}