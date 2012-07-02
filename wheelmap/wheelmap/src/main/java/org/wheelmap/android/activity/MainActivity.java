package org.wheelmap.android.activity;

import org.wheelmap.android.fragment.POIsListFragment;
import org.wheelmap.android.online.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
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
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        
        bar.addTab(bar.newTab()
                .setText("List")
                .setTabListener(new TabListener()));
    }
    
    /**
     * A TabListener receives event callbacks from the action bar as tabs
     * are deselected, selected, and reselected. A FragmentTransaction
     * is provided to each of these callbacks; if any operations are added
     * to it, it will be committed at the end of the full tab switch operation.
     * This lets tab switches be atomic without the app needing to track
     * the interactions between different tabs.
     *
     * NOTE: This is a very simple implementation that does not retain
     * fragment state of the non-visible tabs across activity instances.
     * Look at the FragmentTabs example for how to do a more complete
     * implementation.
     */
    private class TabListener implements ActionBar.TabListener {

    	private Fragment fragment; 

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
    		final Fragment fragment = Fragment.instantiate(MainActivity.this, POIsListFragment.class.getName());
            ft.add(android.R.id.content, fragment, "map");
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            ft.remove(fragment);
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            Toast.makeText(MainActivity.this, "Reselected!", Toast.LENGTH_SHORT).show();
        }

    }
}
