package org.wheelmap.android.app;

import android.util.DisplayMetrics;
import org.acra.ACRA;
import org.mapsforge.android.maps.MapActivity;

import android.app.ActivityManager;
import android.content.Context;
import de.akquinet.android.androlog.Log;

public class AppCapability {
	private static final String TAG = AppCapability.class.getSimpleName();

	private final static long MAX_MEMORY_DIVISOR = 1024 * 1024;

	static class MemoryLimits {
		static int FULL = -1;
		static int DEGRADED_MIN = -1;
		static int DEGRADED_MAX = -1;
	}

	static class HighDensityMemoryLimits extends MemoryLimits {
		HighDensityMemoryLimits() {
			FULL = 28;
			DEGRADED_MIN = 24;
			DEGRADED_MAX = 20;
		}
	}

	static class MediumDensityMemoryLimits extends MemoryLimits {
		MediumDensityMemoryLimits() {
			FULL = 24;
			DEGRADED_MIN = 20;
			DEGRADED_MAX = 16;
		}
	}

	static class LowDensityMemoryLimits extends MemoryLimits {
		LowDensityMemoryLimits() {
			FULL = 20;
			DEGRADED_MIN = 16;
			DEGRADED_MAX = 12;
		}
	}

	private final static int MAPSFORGE_MEMCACHE_CAPACITY_MAX = 16;
	private final static int MAPSFORGE_MEMCACHE_CAPACITY_MED = 8;
	private final static int MAPSFORGE_MEMCACHE_CAPACITY_MIN = 0;

	private static int sMemoryClass;
	private static int sMaxMemoryMB;
	private static Capability sCapability;

	private enum Capability {
		FULL, DEGRADED_MIN, DEGRADED_MAX, NOTWORKING
	}

	public static void init(Context context) {
		getMemoryInfo(context);
		calcOverallCapability(context);
		setMapsforgeSharedMemcacheSize();
		setAcraData();
	}

	private static void getMemoryInfo(Context context) {
		sMaxMemoryMB = (int) (Runtime.getRuntime().maxMemory() / MAX_MEMORY_DIVISOR);
		Log.d(TAG, "mMaxMemoryMB = " + sMaxMemoryMB);
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		sMemoryClass = am.getMemoryClass();
		Log.d(TAG, "memoryClass = " + sMemoryClass);
	}

	private static void calcOverallCapability(Context context) {
		DisplayMetrics displaymetrics = context.getResources().getDisplayMetrics();
		Log.d(TAG, "Screen density is = " + displaymetrics.densityDpi);
		if (displaymetrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM)
			calcCapabilityLevel(new MediumDensityMemoryLimits());
		else if (displaymetrics.densityDpi == DisplayMetrics.DENSITY_LOW)
			calcCapabilityLevel(new LowDensityMemoryLimits());
		else
			calcCapabilityLevel(new HighDensityMemoryLimits());
	}

	private static void calcCapabilityLevel(MemoryLimits memoryLimits) {
		// Log.d( TAG, "Limits: full = " + memoryLimits.FULL + " degraded min " + memoryLimits.DEGRADED_MIN + " degraded max " + memoryLimits.DEGRADED_MAX);

		if (sMaxMemoryMB >= memoryLimits.FULL)
			sCapability = Capability.FULL;
		else if (sMaxMemoryMB < memoryLimits.FULL
				&& sMaxMemoryMB >= memoryLimits.DEGRADED_MIN)
			sCapability = Capability.DEGRADED_MIN;
		else if (sMaxMemoryMB < memoryLimits.DEGRADED_MIN
				&& sMaxMemoryMB >= memoryLimits.DEGRADED_MAX)
			sCapability = Capability.DEGRADED_MAX;
		else
			sCapability = Capability.NOTWORKING;

		Log.d(TAG, "Capability Levels = " + sCapability.name() + " with heap mem = " + sMaxMemoryMB);

	}

	private static void setMapsforgeSharedMemcacheSize() {
		int capacity;
		if (sCapability == Capability.FULL)
			capacity = MAPSFORGE_MEMCACHE_CAPACITY_MAX;
		else if (sCapability == Capability.DEGRADED_MAX)
			capacity = MAPSFORGE_MEMCACHE_CAPACITY_MED;
		else
			capacity = MAPSFORGE_MEMCACHE_CAPACITY_MIN;

		MapActivity.setSharedRAMCacheCapacity(capacity);
	}

	private static void setAcraData() {
		ACRA.getErrorReporter().putCustomData("memoryClass",
				Integer.toString(sMemoryClass));

		ACRA.getErrorReporter().putCustomData("maxMemoryMB",
				Integer.toString(sMaxMemoryMB));
	}

	public static int getMemoryClass() {
		return sMemoryClass;
	}

	public static boolean isNotWorking() {
		return sCapability == Capability.NOTWORKING;
	}

	public static boolean degradeDetailMapAsButton() {
		return sCapability == Capability.DEGRADED_MAX;
	}

	public static boolean degradeLargeMapQuality() {
		return (sCapability == Capability.DEGRADED_MIN || sCapability == Capability.DEGRADED_MAX);
	}
}
