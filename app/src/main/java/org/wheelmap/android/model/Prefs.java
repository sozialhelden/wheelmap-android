package org.wheelmap.android.model;

import android.content.Context;
import android.content.SharedPreferences;

import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.tango.mode.Mode;

public class Prefs {

    private static SharedPreferences preferences = WheelmapApp.getApp().getSharedPreferences("wheelmap_prefs", Context.MODE_PRIVATE);

    private static final String MEASUREMENT_MODE_SHOWN = "MEASUREMENT_MODE_SHOWN_";


    public static void setModeTutorialWasShown(Mode mode, boolean shown) {
        preferences.edit()
                .putBoolean(MEASUREMENT_MODE_SHOWN + mode, shown)
                .apply();
    }

    public static boolean getModeTutorialWasShown(Mode mode) {
        return preferences.getBoolean(MEASUREMENT_MODE_SHOWN + mode, false);
    }

}
