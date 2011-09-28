package org.wheelmap.android.app;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.wheelmap.android.manager.MyLocationManager;
import org.wheelmap.android.manager.SupportManager;

import android.app.Application;

@ReportsCrashes(formKey = "dGJWQW5PelRXWUFTbDh6VW5UYm94cXc6MQ" )
public class WheelmapApp extends Application {
	
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
		mSupportManager = manager;
	}
	
	public static SupportManager getSupportManager() {
		return INSTANCE.mSupportManager;
	}
}
