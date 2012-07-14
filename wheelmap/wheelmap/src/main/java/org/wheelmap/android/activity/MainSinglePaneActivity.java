package org.wheelmap.android.activity;

import java.util.ArrayList;

import org.wheelmap.android.activity.MyTabListener.OnStateListener;
import org.wheelmap.android.activity.MyTabListener.TabHolder;
import org.wheelmap.android.fragment.POIsListFragment;
import org.wheelmap.android.fragment.POIsListWorkerFragment;
import org.wheelmap.android.fragment.POIsMapsforgeFragment;
import org.wheelmap.android.fragment.POIsMapsforgeWorkerFragment;
import org.wheelmap.android.online.R;

import roboguice.inject.ContentView;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import de.akquinet.android.androlog.Log;

@ContentView(R.layout.activity_main_singlepane)
public class MainSinglePaneActivity extends MapsforgeMapActivity implements
		OnStateListener {
	private static final String TAG = MainSinglePaneActivity.class
			.getSimpleName();
	private final static ArrayList<TabHolder> mIndexToTab;

	private int mSelectedTab;
	private final static String EXTRA_SELECTED_TAB = "org.wheelmap.android.SELECTED_TAB";
	private boolean mIsRecreated;
	private final static String EXTRA_IS_RECREATED = "org.wheelmap.android.IS_RECREATED";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		if (savedInstanceState != null)
			executeSavedInstanceState(savedInstanceState);
		else
			executeDefaultInstanceState();

		// getSupportFragmentManager().enableDebugLogging(true);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);

		Tab tab = actionBar
				.newTab()
				.setText(R.string.title_pois_list)
				.setTag(mIndexToTab.get(0).name)
				.setTabListener(
						new MyTabListener<POIsListFragment>(this, mIndexToTab
								.get(0), POIsListFragment.class));
		actionBar.addTab(tab, 0, false);

		tab = actionBar
				.newTab()
				.setText(R.string.title_pois_map)
				.setTag(mIndexToTab.get(1).name)
				.setTabListener(
						new MyTabListener<POIsMapsforgeFragment>(this,
								mIndexToTab.get(1), POIsMapsforgeFragment.class));
		actionBar.addTab(tab, 1, false);

		actionBar.setSelectedNavigationItem(mSelectedTab);

	}

	private void executeSavedInstanceState(Bundle state) {
		mIsRecreated = state.getBoolean(EXTRA_IS_RECREATED, false);
		mSelectedTab = state.getInt(EXTRA_SELECTED_TAB);
	}

	private void executeDefaultInstanceState() {
		mIsRecreated = false;
		mSelectedTab = 0;
	}

	public void onStateChange(String tag) {
		mSelectedTab = getSupportActionBar().getSelectedNavigationIndex();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(EXTRA_IS_RECREATED, true);
		outState.putInt(EXTRA_SELECTED_TAB, mSelectedTab);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.ab_main_activity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
		case R.id.menu_filter:

			return true;
		case R.id.menu_about:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	static {
		mIndexToTab = new ArrayList<TabHolder>();
		mIndexToTab.add(new TabHolder(POIsListFragment.TAG,
				POIsListWorkerFragment.TAG));
		mIndexToTab.add(new TabHolder(POIsMapsforgeFragment.TAG,
				POIsMapsforgeWorkerFragment.TAG));
	}

}
