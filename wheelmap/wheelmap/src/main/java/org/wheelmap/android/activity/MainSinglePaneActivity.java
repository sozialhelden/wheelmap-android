package org.wheelmap.android.activity;

import java.util.ArrayList;

import org.wheelmap.android.activity.MyTabListener.OnStateListener;
import org.wheelmap.android.activity.MyTabListener.TabHolder;
import org.wheelmap.android.fragment.DisplayFragmentListener;
import org.wheelmap.android.fragment.ErrorDialogFragment;
import org.wheelmap.android.fragment.POIsListFragment;
import org.wheelmap.android.fragment.POIsListWorkerFragment;
import org.wheelmap.android.fragment.POIsMapsforgeFragment;
import org.wheelmap.android.fragment.POIsMapsforgeWorkerFragment;
import org.wheelmap.android.fragment.WorkerFragmentListener;
import org.wheelmap.android.manager.MyLocationManager;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.PrepareDatabaseHelper;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.SyncServiceException;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import de.akquinet.android.androlog.Log;

public class MainSinglePaneActivity extends MapsforgeMapActivity implements
		DisplayFragmentListener, WorkerFragmentListener, OnStateListener {
	private static final String TAG = MainSinglePaneActivity.class
			.getSimpleName();
	private final static ArrayList<TabHolder> mIndexToTab;

	private final static int DEFAULT_SELECTED_TAB = 0;
	private int mSelectedTab = DEFAULT_SELECTED_TAB;
	private boolean mIsRecreated;
	private GoogleAnalyticsTracker tracker;

	final static int TAB_LIST = 0;
	final static int TAB_MAP = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		// Debug.startMethodTracing("wheelmap");

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setSupportProgressBarIndeterminateVisibility(false);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		// FragmentManager.enableDebugLogging(true);

		// GA
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession("UA-25843648-1", 20, this);
		tracker.setAnonymizeIp(true);

		if (savedInstanceState != null)
			executeState(savedInstanceState);
		else
			executeDefaultInstanceState();

		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);

		Tab tab = actionBar
				.newTab()
				.setText(R.string.title_pois_list)
				.setIcon(
						getResources().getDrawable(
								R.drawable.ic_location_list_wm_holo_light))
				.setTag(mIndexToTab.get(TAB_LIST).name)
				.setTabListener(
						new MyTabListener<POIsListFragment>(this, mIndexToTab
								.get(TAB_LIST), POIsListFragment.class));
		actionBar.addTab(tab, TAB_LIST, false);

		tab = actionBar
				.newTab()
				.setText(R.string.title_pois_map)
				.setIcon(
						getResources().getDrawable(
								R.drawable.ic_location_map_wm_holo_light))

				.setTag(mIndexToTab.get(TAB_MAP).name)
				.setTabListener(
						new MyTabListener<POIsMapsforgeFragment>(this,
								mIndexToTab.get(TAB_MAP),
								POIsMapsforgeFragment.class));
		actionBar.addTab(tab, TAB_MAP, false);

		actionBar.setSelectedNavigationItem(mSelectedTab);

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d(TAG, "onNewIntent");
		setIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (getIntent() != null) {
			executeIntent(getIntent());
			setIntent(null);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Debug.stopMethodTracing();
	}

	private void executeIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras == null)
			return;

		executeState(extras);
		mIndexToTab.get(mSelectedTab).setExecuteBundle(extras);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setSelectedNavigationItem(mSelectedTab);
	}

	private void executeState(Bundle state) {
		mIsRecreated = state.getBoolean(Extra.IS_RECREATED, false);
		mSelectedTab = state.getInt(Extra.SELECTED_TAB, DEFAULT_SELECTED_TAB);
	}

	private void executeDefaultInstanceState() {
		mIsRecreated = false;
		mSelectedTab = DEFAULT_SELECTED_TAB;
	}

	public void onStateChange(String tag) {
		if (tag == null)
			return;

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
		outState.putBoolean(Extra.IS_RECREATED, true);
		outState.putInt(Extra.SELECTED_TAB, mSelectedTab);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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

	private long insertNewPoi() {
		Location location = MyLocationManager.get(null, false)
				.getLastLocation();

		String name = getString(R.string.new_default_name);
		long id = PrepareDatabaseHelper.insertNew(getContentResolver(), name,
				location.getLatitude(), location.getLongitude());

		return id;
	}

	private void createNewPoi() {
		long poiId = insertNewPoi();
		Intent i = new Intent(this, POIDetailEditableActivity.class);
		i.putExtra(Extra.POI_ID, poiId);
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
	public void onShowDetail(long id) {
		long copyId = PrepareDatabaseHelper.createCopyIfNotExists(
				getContentResolver(), id);
		Intent intent = new Intent(this, POIDetailActivity.class);
		intent.putExtra(Extra.POI_ID, copyId);
		startActivity(intent);
	}

	@Override
	public void onRefreshing(boolean isRefreshing) {
		Log.d(TAG, "onRefreshStatusChange isRefreshing = " + isRefreshing);
		setSupportProgressBarIndeterminateVisibility(isRefreshing);
	}

	@Override
	public void onSearchModeChange(boolean isSearchMode) {

	}

	static {
		mIndexToTab = new ArrayList<TabHolder>();
		mIndexToTab.add(new TabHolder(POIsListFragment.TAG,
				POIsListWorkerFragment.TAG));
		mIndexToTab.add(new TabHolder(POIsMapsforgeFragment.TAG,
				POIsMapsforgeWorkerFragment.TAG));
	}

}
