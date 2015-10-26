package org.holoeverywhere.preference;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.wheelmap.android.online.BuildConfig;


public class PreferenceManagerHelper {

    public static enum PreferenceImpl {
        JSON, XML
    }

    /**
     * Preference implementation using by default
     */
    public static PreferenceImpl PREFERENCE_IMPL = PreferenceImpl.XML;

    static interface PreferenceManagerImpl {
        SharedPreferences getDefaultSharedPreferences(Context context, PreferenceImpl impl);

        int obtainThemeTag();

        SharedPreferences wrap(Context context, PreferenceImpl impl, String name, int mode);
    }

    private static PreferenceManagerImpl IMPL;

    static {
        try {
            Class<?> clazz = Class
                    .forName("org.holoeverywhere.preference._PreferenceManagerImpl");
            IMPL = (PreferenceManagerImpl) clazz.newInstance();
        } catch (Exception e) {
            IMPL = null;
            if (BuildConfig.DEBUG) {
                Log.w("HoloEverywhere",
                        "Cannot find PreferenceManager class. Preference framework are disabled.",
                        e);
            }
        }
    }

    private static void checkImpl() {
        if (IMPL == null) {
            throw new UnsatisfiedLinkError("HoloEverywhere: PreferenceFramework not found");
        }
    }

    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        return getDefaultSharedPreferences(context, PREFERENCE_IMPL);
    }

    public static SharedPreferences getDefaultSharedPreferences(Context context,
                                                                PreferenceImpl impl) {
        checkImpl();
        return IMPL.getDefaultSharedPreferences(context, impl);
    }

    private PreferenceManagerHelper() {

    }
}