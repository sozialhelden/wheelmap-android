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
                .setText(R.string.title_pois_list)
                .setTag(new Integer(R.string.title_pois_list))
                .setTabListener(new TabListener()));
        
        bar.addTab(bar.newTab()
                .setText(R.string.title_pois_map)
                .setTag(new Integer(R.string.title_pois_map))
                .setTabListener(new TabListener()));
    }
    
   private class TabListener implements ActionBar.TabListener {

    	private Fragment fragment; 

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
        	final int tabId = (Integer)tab.getTag();
        	tabId.intValue();
        	//case t
    		fragment = Fragment.instantiate(MainActivity.this, POIsListFragment.class.getName());
            ft.add(android.R.id.content, fragment, POIsListFragment.class.getName());
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            ft.remove(fragment);
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            Toast.makeText(MainActivity.this, "Reselected!", Toast.LENGTH_SHORT).show();
        }

    }
}
