package org.wheelmap.android.ui;

import org.wheelmap.android.R;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class WheelmapHomeActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }
    
    /** Handle "schedule" action. */
    public void onListClick(View v) {
        // Launch overall conference schedule
        startActivity(new Intent(this, POIsListActivity.class));
    }
    
	public void onSearchClick(View v) {
		Toast.makeText(this, "Searching..", Toast.LENGTH_SHORT).show();
  }

    /** Handle "map" action. */
    public void onMapClick(View v) {
        // Launch map of conference venue
    	
    	try {
        	startActivity(new Intent(this, POIsMapActivity.class));
    	} catch ( ActivityNotFoundException e) {
    	    e.printStackTrace();
    	}
    	
    }
}