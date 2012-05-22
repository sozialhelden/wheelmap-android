/*
Copyright (C) 2011 Michal Harakal and Michael Kroez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS-IS" BASIS
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
        
*/

package org.wheelmap.android.app;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.annotation.ReportsCrashes;
import org.mapsforge.android.maps.MapActivity;
import org.wheelmap.android.manager.MyLocationManager;
import org.wheelmap.android.manager.SupportManager;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

// Beta and PRE-RC key: "dGJWQW5PelRXWUFTbDh6VW5UYm94cXc6MQ"
// RC1 - key: @ReportsCrashes(formKey = "dC1VVDdKenJLRUpZTC1MZXBVR3p6ZlE6MQ" )
// RC2 - key: @ReportsCrashes(formKey = "dG1fUDltTlNiM3V4NmRvaVExT3dJclE6MQ" )
// Release v0.7 @ReportsCrashes( formKey = "dGMzcTRSZjRMRG14c0JmU25ET1JLQmc6MQ")
// the first market version. @ReportsCrashes( formKey = "dGMzcTRSZjRMRG14c0JmU25ET1JLQmc6MQ")
// the second market version @ReportsCrashes( formKey = "dEFLbUtHV1VlNEp2MHc0UXg3M0VyUnc6MQ")


@ReportsCrashes( formKey = "dGEyal90UGZ2Mk0tSmROYnBsVk02THc6MQ")
public class WheelmapApp extends Application {
	private final static String TAG = "wheelmapapp";
	
	private static WheelmapApp INSTANCE;
	private MyLocationManager mLocationManager;
	private SupportManager mSupportManager;
	private int mMemoryClass;
	
	private int mMaxMemoryMB;
	private final static long MAX_MEMORY_DIVISOR = 1024 * 1024;
	private final static int MAX_MEMORY_LIMIT_FULL = 28;
	private final static int MAX_MEMORY_LIMIT_DEGRADED_MIN = 24;
	private final static int MAX_MEMORY_LIMIT_DEGRADED_MAX = 20;
	
	private final static int MAPSFORGE_MEMCACHE_CAPACITY_MAX = 16;
	private final static int MAPSFORGE_MEMCACHE_CAPACITY_MED = 8;
	private final static int MAPSFORGE_MEMCACHE_CAPACITY_MIN = 0;
	
	public enum Capability { FULL, DEGRADED_MIN, DEGRADED_MAX, NOTWORKING };
	public Capability mCapability;
	
	@Override
	public void onCreate() {
		ACRA.init(this);
		
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		mMemoryClass = am.getMemoryClass();
		Log.d( TAG, "memoryClass = " + mMemoryClass );
		ErrorReporter.getInstance().putCustomData("memoryClass", Integer.toString( mMemoryClass));
		
		mMaxMemoryMB = (int)(Runtime.getRuntime().maxMemory() / MAX_MEMORY_DIVISOR);
		Log.d( TAG, "mMaxMemoryMB = " + mMaxMemoryMB );
		ErrorReporter.getInstance().putCustomData("maxMemoryMB", Integer.toString(mMaxMemoryMB ));
		
		super.onCreate();
		Log.d( TAG, "onCreate" );
		mLocationManager = MyLocationManager.initOnce( this );
		mSupportManager = new SupportManager( this );
		
		calcCapabilityLevel();
		setMapsforgeSharedMemcacheSize();
		INSTANCE = this;
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		mLocationManager.clear();
		Log.d(TAG,  "onTerminate" );
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.d( "lowmemory", "wheelmap app - onLowMemory" );
	}
	
	public static int getMemoryClass() {
		return INSTANCE.mMemoryClass;
	}
	
	private void calcCapabilityLevel() {
		if ( mMaxMemoryMB >= MAX_MEMORY_LIMIT_FULL )
			mCapability = Capability.FULL;
		else if ( mMaxMemoryMB < MAX_MEMORY_LIMIT_FULL && mMaxMemoryMB >= MAX_MEMORY_LIMIT_DEGRADED_MIN )
			mCapability = Capability.DEGRADED_MIN;
		else if ( mMaxMemoryMB < MAX_MEMORY_LIMIT_DEGRADED_MIN && mMaxMemoryMB >= MAX_MEMORY_LIMIT_DEGRADED_MAX )
			mCapability = Capability.DEGRADED_MAX;
		else
			mCapability = Capability.NOTWORKING;
	}
	
	public static Capability getCapabilityLevel() {
		return INSTANCE.mCapability;
	}
	
	public static SupportManager getSupportManager() {
		if ( INSTANCE == null )
			Log.d( TAG, "INSTANCE == null - how can that be?" );
		if ( INSTANCE != null && INSTANCE.mSupportManager == null )
			Log.d(TAG, "INSTANCE != null - mSupportManager = null - how can that be?" );
		
		return INSTANCE.mSupportManager;
	}
	
	private void setMapsforgeSharedMemcacheSize() {
		int capacity;
		if ( mCapability == Capability.FULL )
			capacity = MAPSFORGE_MEMCACHE_CAPACITY_MAX;
		else if ( mCapability == Capability.DEGRADED_MAX)
			capacity = MAPSFORGE_MEMCACHE_CAPACITY_MED;
		else
			capacity = MAPSFORGE_MEMCACHE_CAPACITY_MIN;
		
		MapActivity.setSharedRAMCacheCapacity( capacity );
	}
}
