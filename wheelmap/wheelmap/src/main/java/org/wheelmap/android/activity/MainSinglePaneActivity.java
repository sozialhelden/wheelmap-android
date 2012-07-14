package org.wheelmap.android.activity;

import java.util.ArrayList;

import org.wheelmap.android.activity.MyTabListener.OnStateListener;
import org.wheelmap.android.activity.MyTabListener.TabHolder;
import org.wheelmap.android.fragment.POIsListFragment;
import org.wheelmap.android.fragment.POIsMapsforgeFragment;
import org.wheelmap.android.online.R;

import roboguice.inject.ContentView;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;

@ContentView(R.layout.activity_main_singlepane)
public class MainSinglePaneActivity extends MapsforgeMapActivity implements
		OnStateListener {
	private static final String TAG = MainSinglePaneActivity.class
			.getSimpleName();
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE);
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

		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);
		Log.d(TAG, "actionBar tabCount " + actionBar.getTabCount());

		Tab tab = actionBar
				.newTab()
				.setText(R.string.title_pois_list)
				.setTag(mIndexToTab.get(0).name)
				.setTabListener(
						new MyTabListener<POIsListFragment>(this, mIndexToTab
								.get(0), POIsListFragment.class));
		actionBar.addTab(tab);

		tab = actionBar
				.newTab()
				.setText(R.string.title_pois_map)
				.setTag(mIndexToTab.get(1).name)
				.setTabListener(
						new MyTabListener<POIsMapsforgeFragment>(this,
								mIndexToTab.get(1), POIsMapsforgeFragment.class));
		actionBar.addTab(tab);

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
		Log.d(TAG, "onStateChange = " + tag);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState");
		outState.putBoolean(EXTRA_IS_RECREATED, true);
		outState.putInt(EXTRA_SELECTED_TAB, mSelectedTab);
		super.onSaveInstanceState(outState);
	}

	static {
		mIndexToTab = new ArrayList<TabHolder>();
		mIndexToTab.add(new TabHolder("list"));
		mIndexToTab.add(new TabHolder("map"));
	}

}
