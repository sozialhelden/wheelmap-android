package org.holoeverywhere.preference;

import org.holoeverywhere.preference.PreferenceManagerHelper.PreferenceImpl;
import org.holoeverywhere.preference.PreferenceManagerHelper.PreferenceManagerImpl;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class _PreferenceManagerImpl implements PreferenceManagerImpl {
    @Override
    public SharedPreferences getDefaultSharedPreferences(Context context, PreferenceImpl impl) {
        return PreferenceManager.getDefaultSharedPreferences(context/*, impl*/);
    }

    @Override
    public int obtainThemeTag() {
        return 0;
    }

    @Override
    public SharedPreferences wrap(Context context, PreferenceImpl impl, String name, int mode) {
//        return PreferenceManager.wrap(context, impl, name, mode);
        return null;
    }
}