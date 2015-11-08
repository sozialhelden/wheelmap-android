package com.jayway.android.robotium.solo;

import android.app.Activity;
import android.support.v7.internal.view.menu.ActionMenuItem;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Window;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class ClickerCompatibilityAbs {

    private final String LOG_TAG = "Robotium";

    private final ActivityUtils activityUtils;

    private final Sleeper sleeper;

    private final Clicker clicker;

    /**
     * Constructs this object.
     *
     * @param activityUtils the {@code ActivityUtils} instance.
     * @param sleeper       the {@code Sleeper} instance
     * @param clicker       the {@code Clicker} instance
     */

    public ClickerCompatibilityAbs(ActivityUtils activityUtils, Sleeper sleeper, Clicker clicker) {
        this.activityUtils = activityUtils;
        this.sleeper = sleeper;
        this.clicker = clicker;
    }

    /**
     * Clicks on an ActionBar Home/Up button. Should be called only in apps that use ActionBarSherlock
     * and which are tested on pre ICS Android. Tests which run on 4.0 and higher should use {@link
     * Solo#clickOnActionBarHomeButton()}.
     */
    public void clickOnActionBarHomeButtonCompat() {

        Activity activity = activityUtils.getCurrentActivity();

        ActionMenuItem logoNavItem = new ActionMenuItem(activity, 0, android.R.id.home, 0, 0, "");

        ActionBar supportActionBar = null;
        try {
            supportActionBar
                    = (ActionBar) invokePrivateMethodWithoutParameters(
                    ActionBar.class, "getActionBar", activity);
        } catch (Exception ex) {
            Log.d(LOG_TAG, "Can not find methods to invoke Home button.");
        }

        if (supportActionBar != null) {
            activity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, logoNavItem);
        }
    }

    private Object invokePrivateMethodWithoutParameters(Class<?> clazz, String methodName,
            Object receiver)
            throws Exception {
        Method method = null;
        method = clazz.getDeclaredMethod(methodName, (Class<?>[]) null);

        if (method != null) {
            method.setAccessible(true);
            return method.invoke(receiver, (Object[]) null);
        }

        return null;
    }

    private Object getPrivateField(String fieldName, Object object) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);

        if (field != null) {
            field.setAccessible(true);
            return field.get(object);
        }

        return null;
    }
}