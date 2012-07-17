package org.wheelmap.android.activity;

import java.util.ArrayList;

import org.wheelmap.android.activity.MyTabListener.OnStateListener;
import org.wheelmap.android.activity.MyTabListener.TabHolder;
import org.wheelmap.android.fragment.ErrorDialogFragment;
import org.wheelmap.android.fragment.POIDetailEditableFragment;
import org.wheelmap.android.fragment.POIsListFragment;
import org.wheelmap.android.fragment.POIsListFragment.OnPOIsListListener;
import org.wheelmap.android.fragment.POIsListWorkerFragment;
import org.wheelmap.android.fragment.POIsListWorkerFragment.OnPOIsListWorkerListener;
import org.wheelmap.android.fragment.POIsMapsforgeFragment;
import org.wheelmap.android.fragment.POIsMapsforgeFragment.OnPOIsMapsforgeListener;
import org.wheelmap.android.fragment.POIsMapsforgeWorkerFragment;
import org.wheelmap.android.fragment.POIsMapsforgeWorkerFragment.OnPOIsMapsforgeWorkerListener;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.SyncServiceException;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import de.akquinet.android.androlog.Log;

public class MainSinglePaneActivity extends MapsforgeMapActivity implements
		OnStateListener, OnPOIsListListener, OnPOIsListWorkerListener,
		OnPOIsMapsforgeListener, OnPOIsMapsforgeWorkerListener {
	private static final String TAG = MainSinglePaneActivity.class
			.getSimpleName();
	private final static ArrayList<TabHolder> mIndexToTab;

	private int mSelectedTab;
	private final static String EXTRA_SELECTED_TAB = "org.wheelmap.android.SELECTED_TAB";
	private boolean mIsRecreated;
	private final static String EXTRA_IS_RECREATED = "org.wheelmap.android.IS_RECREATED";
	private GoogleAnalyticsTracker tracker;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main_singlepane);
		setSupportProgressBarIndeterminateVisibility(false);

		// GA
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession("UA-25843648-1", 20, this);
		tracker.setAnonymizeIp(true);

		if (savedInstanceState != null)
			executeSavedInstanceState(savedInstanceState);
		else
			executeDefaultInstanceState();

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
		Log.d(TAG, "onStateChange " + tag);

		mSelectedTab = getSupportActionBar().getSelectedNavigationIndex();
		String readableName = tag.replaceAll("Fragment", "");
		tracker.trackPageView(readableName);
		tracker.trackEvent("Clicks", // Category
				"Button", // Action
				"SwitchMaps", // Label
				0); // Value
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
			showFilterSettings();
			return true;
		case R.id.menu_about:
			showInfo();
			return true;
		case R.id.menu_new_poi:
			createNewPoi();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void showInfo() {
		Intent intent = new Intent(this, InfoActivity.class);
		startActivity(intent);
	}

	private void showFilterSettings() {
		Intent intent = new Intent(this, NewSettingsActivity.class);
		startActivity(intent);
	}

	private void createNewPoi() {
		long poiId = 0; // = list worker fragment creates one
		Intent i = new Intent(this, POIDetailEditableActivity.class);
		i.putExtra(POIDetailEditableFragment.ARGUMENT_POI_ID, poiId);
		startActivity(i);
	}

	@Override
	public void onError(SyncServiceException e) {

		FragmentManager fm = getSupportFragmentManager();
		ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance(e);
		if (errorDialog == null)
			return;

		errorDialog.show(fm, ErrorDialogFragment.TAG);
	}

	@Override
	public void onSearchModeChange(boolean isSearchMode) {

	}

	@Override
	public void onRefreshStatusChange(boolean refreshStatus) {
		Log.d(TAG, "onRefreshStatusChange refreshStatus = " + refreshStatus);
		setSupportProgressBarIndeterminateVisibility(refreshStatus);
	}

	@Override
	public void onShowDetail(long id) {
		Intent i = new Intent(this, POIDetailActivity.class);
		i.putExtra(Wheelmap.POIs.EXTRAS_POI_ID, id);
		startActivity(i);
	}

	static {
		mIndexToTab = new ArrayList<TabHolder>();
		mIndexToTab.add(new TabHolder(POIsListFragment.TAG,
				POIsListWorkerFragment.TAG));
		mIndexToTab.add(new TabHolder(POIsMapsforgeFragment.TAG,
				POIsMapsforgeWorkerFragment.TAG));
	}

}
