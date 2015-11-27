package org.wheelmap.android.modules;

import com.google.inject.Inject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;

import de.akquinet.android.androlog.Log;
import oak.Base64;

public class BundlePreferences implements IBundlePreferences {

    private final static String TAG = BundlePreferences.class.getSimpleName();

    public static final int BASE64_OPTS = Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE;

    private static final String PREFERENCES_FILE = "BundleStore";

    private final SharedPreferences preferences;

    @Inject
    public BundlePreferences(Context context) {
        preferences = context.getSharedPreferences(
                PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Override
    public void store(String id, Bundle bundle) {
        Parcel parcel = Parcel.obtain();
        bundle.writeToParcel(parcel, Bundle.PARCELABLE_WRITE_RETURN_VALUE);
        byte[] bytes = parcel.marshall();
        String value = Base64.encodeToString(bytes, BASE64_OPTS);
        preferences.edit().putString(id, value).commit();
        Log.d(TAG, "store: id = " + id);


    }

    @Override
    public boolean contains(String id) {
        return preferences.contains(id);
    }

    @Override
    public Bundle retrieve(String id) {
        String value = preferences.getString(id, null);
        if (value == null) {
            return new Bundle();
        }

        byte[] bytes = Base64.decode(value, BASE64_OPTS);
        Parcel parcel = Parcel.obtain();
        Bundle bundle;
        try {
            parcel.unmarshall(bytes, 0, bytes.length);
            parcel.setDataPosition(0);
            bundle = parcel.readBundle();
        } catch (RuntimeException e) {
            Log.w(TAG, "retrieve: parcel could not be decoded - returning empty");
            return new Bundle();
        }
        parcel.recycle();
        if(bundle != null) {
            Log.d(TAG, "retrieve: id = " + id + " empty = " + bundle.isEmpty());
            return bundle;
        } else {
            return new Bundle();
        }
    }
}
