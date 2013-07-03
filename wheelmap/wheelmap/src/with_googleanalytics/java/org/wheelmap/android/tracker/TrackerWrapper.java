package org.wheelmap.android.tracker;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.content.Context;

import de.akquinet.android.androlog.Log;

public class TrackerWrapper {

    private final static String TAG = TrackerWrapper.class.getSimpleName();

    private GoogleAnalyticsTracker tracker;

    public TrackerWrapper(Context context) {
        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.startNewSession("UA-25843648-1", 20, context);
        tracker.setAnonymizeIp(true);

    }

    public void track(String name) {
        Log.v(TAG, "track request done for " + name);

        tracker.trackPageView(name);
        tracker.trackEvent("Clicks", // Category
                "Button", // Action
                "SwitchMaps", // Label
                0); // Value
    }

}
