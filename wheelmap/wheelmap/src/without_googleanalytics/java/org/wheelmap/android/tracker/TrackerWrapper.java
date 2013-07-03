package org.wheelmap.android.tracker;

import android.content.Context;

import de.akquinet.android.androlog.Log;

public class TrackerWrapper {

    private final static String TAG = TrackerWrapper.class.getSimpleName();

    public TrackerWrapper(Context context) {

    }

    public void track(String name) {
        Log.v(TAG, "track request ignored");
    }

}
