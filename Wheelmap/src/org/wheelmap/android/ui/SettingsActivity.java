package org.wheelmap.android.ui;

import org.wheelmap.android.R;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		Preference mapfileManager = (Preference) findPreference("mapfileManager");
		mapfileManager.setOnPreferenceClickListener( this );
		
		Preference mapfileSelect = (Preference) findPreference("mapfileSelect");
		mapfileSelect.setOnPreferenceClickListener( this );
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();
		if ( key.equals( "mapfileManager" ))
			startActivity( new Intent( this, MapFileDownloadActivity.class ));
		else if ( key.equals( "mapfileSelect" ))
			startActivity( new Intent( this, MapFileSelectActivity.class ));
		return true;
	}

}
