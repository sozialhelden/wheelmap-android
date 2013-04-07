package org.wheelmap.android.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import org.holoeverywhere.FontLoader;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;

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
