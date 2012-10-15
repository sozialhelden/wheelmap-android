package org.wheelmap.android.activity;

import android.content.ComponentCallbacks;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.WazaBe.HoloEverywhere.FontLoader;
import com.WazaBe.HoloEverywhere.app.Activity;
import com.WazaBe.HoloEverywhere.preference.PreferenceManager;
import com.WazaBe.HoloEverywhere.preference.SharedPreferences;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import de.akquinet.android.androlog.Log;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class HoloRoboSherlockFragmentActivity extends
		RoboSherlockFragmentActivity implements LayoutInflater.Factory {
    private final static String TAG = HoloRoboSherlockFragmentActivity.class.getSimpleName();

	@Override
	public void addContentView(View view, LayoutParams params) {
		super.addContentView(FontLoader.apply(view), params);
	}

    public SharedPreferences getSupportSharedPreferences(String name, int mode) {
        return PreferenceManager.wrap(this, name, mode);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        getLayoutInflater().setFactory(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(FontLoader.inflate(this, layoutResID));
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(FontLoader.apply(view));
	}

	@Override
	public void setContentView(View view, LayoutParams params) {
		super.setContentView(FontLoader.apply(view), params);
	}
}
