package org.wheelmap.android.utils;

import android.os.Bundle;
import android.os.Parcelable;

/**
 * arguments which can be used to pass values to an activity or fragment
 * use {@link com.google.auto.value.AutoValue} to generate Parcelable implementation
 */
public abstract class Arguments implements Parcelable {

    public static final String KEY_ARGUMENTS = "Arguments";
    public static final String KEY_TYPE = "Type";

    public Bundle toBundle() {
        Bundle b = new Bundle();
        addToBundle(b);
        return b;
    }

    public static <T extends Arguments> T fromBundle(Bundle b) {
        if (b == null) {
            return null;
        }
        return b.getParcelable(KEY_ARGUMENTS);
    }

    public void addToBundle(Bundle b) {
        b.putParcelable(KEY_ARGUMENTS, this);
        b.putString(KEY_TYPE, this.getClass().getCanonicalName());
    }

    public static void removeArguments(Bundle b) {
        if (b == null) {
            return;
        }
        b.remove(KEY_ARGUMENTS);
    }

    public static boolean isInstanceOf(Class compareClass, Bundle b){
        if (b == null || compareClass == null) {
            return false;
        }
        return compareClass.getCanonicalName().equals(b.getString(KEY_TYPE));
    }

}
