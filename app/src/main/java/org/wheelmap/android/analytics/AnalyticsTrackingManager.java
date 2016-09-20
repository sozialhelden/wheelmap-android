package org.wheelmap.android.analytics;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import org.wheelmap.android.online.BuildConfig;
import org.wheelmap.android.utils.Constants;


public class AnalyticsTrackingManager {

    public enum TrackableScreensName {
        SPLASHSCREEN(Constants.Tracking.Screens.SPLASHSCREEN),
        HOMESCREEN(Constants.Tracking.Screens.HOMESCREEN),
        NEARBYSCREEN(Constants.Tracking.Screens.NEARBYSCREEN),
        MAPSCREEN(Constants.Tracking.Screens.MAPSCREEN),
        CATEGORYSCREEN(Constants.Tracking.Screens.CATEGORYSCREEN),
        CONTRIBUTESCREEN(Constants.Tracking.Screens.CONTRIBUTESCREEN),
        OSMONBORDINGSCREEN(Constants.Tracking.Screens.OSMONBORDINGSCREEN),
        OSMLOGOUTSCREEN(Constants.Tracking.Screens.OSMLOGOUTSCREEN),
        INFOSCREEN(Constants.Tracking.Screens.INFOSCREEN);


        private final String screenName;

        TrackableScreensName(String screenName) {
            this.screenName = screenName;
        }

        public String getScreenName() {
            return screenName;
        }
    }


    private static String TAG = AnalyticsTrackingManager.class.getName();

    private static Tracker googleAnalyticsTracker;


    public static void init(Context context) {
        if (googleAnalyticsTracker == null) {
            googleAnalyticsTracker = initGoogleAnalyticsTracker(context);
        }
    }


    private static Tracker initGoogleAnalyticsTracker(Context context) {

        String trackingId = BuildConfig.ANALYTICS_TRACKING_ID;
        if(!TextUtils.isEmpty(trackingId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
            return analytics.newTracker(trackingId);
        }

        return null;
    }

    public static synchronized void trackScreen(final TrackableScreensName screenName) {

        if(googleAnalyticsTracker == null) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "tracking screen name: " + screenName.getScreenName());
                googleAnalyticsTracker.setScreenName(screenName.getScreenName());
                googleAnalyticsTracker.send(new HitBuilders.ScreenViewBuilder().build());
            }
        }).start();
    }
}
