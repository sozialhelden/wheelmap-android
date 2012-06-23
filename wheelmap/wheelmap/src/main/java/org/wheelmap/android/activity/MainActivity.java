package org.wheelmap.android.activity;

import org.wheelmap.android.online.R;

import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;

public class MainActivity extends RoboSherlockFragmentActivity  {
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        setContentView(R.layout.main);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowTitleEnabled(false);
    }

}
