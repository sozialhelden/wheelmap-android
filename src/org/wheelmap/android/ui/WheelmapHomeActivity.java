package org.wheelmap.android.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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