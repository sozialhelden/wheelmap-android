/*
 * #%L
 * Wheelmap - App
 * %%
 * Copyright (C) 2011 - 2012 Michal Harakal - Michael Kroez - Sozialhelden e.V.
 * %%
 * Wheelmap App based on the Wheelmap Service by Sozialhelden e.V.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wheelmap.android.activity;

import org.wheelmap.android.activity.MyTabListener.OnStateListener;
import org.wheelmap.android.activity.listeners.Progress;
import org.wheelmap.android.analytics.AnalyticsTrackingManager;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.fragment.CombinedWorkerFragment;
import org.wheelmap.android.fragment.DisplayFragmentListener;
import org.wheelmap.android.fragment.ErrorDialogFragment;
import org.wheelmap.android.fragment.POIsListFragment;
import org.wheelmap.android.fragment.POIsOsmdroidFragment;
import org.wheelmap.android.fragment.SearchDialogCombinedFragment;
import org.wheelmap.android.fragment.SearchDialogFragment;
import org.wheelmap.android.fragment.WorkerFragmentListener;
import org.wheelmap.android.manager.MyLocationManager;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.MapModeType;
import org.wheelmap.android.model.PrepareDatabaseHelper;
import org.wheelmap.android.model.Support;
import org.wheelmap.android.modules.AppProperties;
import org.wheelmap.android.modules.IAppProperties;
import org.wheelmap.android.online.R;
import org.wheelmap.android.popup.FilterWindow;
import org.wheelmap.android.service.RestServiceException;
import org.wheelmap.android.utils.Constants;
import org.wheelmap.android.utils.MapActivityUtils;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.LayoutParams;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import de.akquinet.android.androlog.Log;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainSinglePaneActivity extends MapActivity implements
        DisplayFragmentListener, WorkerFragmentListener, OnStateListener, Progress.Provider {

    private static final String TAG = MainSinglePaneActivity.class.getSimpleName();

    IAppProperties appProperties;

    private int mSelectedTab = Constants.TabContent.LOCATION_BASED_LIST;

    public boolean mFirstStart;

    private CombinedWorkerFragment mWorkerFragment;
    private POIsListFragment mListFragment;
    private POIsOsmdroidFragment mMapFragment;
    private ViewFlipper flipper;

    private MapModeType mapModeType;

    private boolean onRefresh = false;

    /**
     * used for testCases
     */
    Progress.Listener mProgressListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_single_pane);

        appProperties = AppProperties.getInstance(WheelmapApp.getApp());
        Log.d(TAG, "onCreate");

        ActionBar actionbar = getSupportActionBar();
        if(actionbar != null){
            actionbar.setHomeButtonEnabled(true);
            actionbar.setDisplayShowTitleEnabled(true);
            actionbar.setDisplayHomeAsUpEnabled(true);
        }

        flipper = (ViewFlipper) findViewById(R.id.flipper);
        flipper.setDisplayedChild(0);

        FragmentManager.enableDebugLogging(true);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        View customNav = LayoutInflater.from(this).inflate(R.layout.actionbar, null);
        actionBar.setCustomView(customNav);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction t = fm.beginTransaction();

        mWorkerFragment = (CombinedWorkerFragment) fm
                .findFragmentByTag(CombinedWorkerFragment.TAG);
        if (mWorkerFragment == null) {
            mWorkerFragment = new CombinedWorkerFragment();
            t.add(mWorkerFragment, CombinedWorkerFragment.TAG);
        }

        mListFragment = (POIsListFragment) fm
                .findFragmentById(R.id.list_layout);
        if (mListFragment == null) {
            mListFragment = POIsListFragment.newInstance(false, true);
            t.add(R.id.list_layout, mListFragment, POIsListFragment.TAG);
        }

        mMapFragment = (POIsOsmdroidFragment) fm
                .findFragmentById(R.id.map_layout);
        if (mMapFragment == null) {
            mMapFragment = POIsOsmdroidFragment.newInstance(false, true);
            t.add(R.id.map_layout, mMapFragment, POIsOsmdroidFragment.TAG);
        }

        t.commit();

        if (savedInstanceState != null) {
            executeState(savedInstanceState);
        } else {
            executeDefaultInstanceState();
        }

        Bundle extras = getIntent().getExtras();
        if(extras.containsKey(Extra.MAP_MODE_ENGAGE)) {
            mapModeType = MapModeType.MAP_MODE_ENGAGE;
            MapActivityUtils.setWheelchairFilterToEngageMode(this);
        } else {
            mapModeType = MapModeType.MAP_MODE_NORMAL;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");
        executeIntent(getIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mapModeType == MapModeType.MAP_MODE_ENGAGE) {
            MapActivityUtils.resetWheelchairFilter(this);
        }
        Log.d(TAG, "onDestroy");
    }

    private void executeIntent(Intent intent) {
        Log.d(TAG, "executeIntent intent = " + intent);
        Bundle extras = intent.getExtras();
        if (extras == null || (!mFirstStart && extras.containsKey(Extra.REQUEST))) {
            return;
        }

        executeState(extras);
    }

    private void executeState(Bundle state) {
        mSelectedTab = state.getInt(Extra.SELECTED_TAB, Constants.TabContent.LOCATION_BASED_LIST);
        mFirstStart = false;

        flipper.setDisplayedChild(mSelectedTab);
        trackTabScreen();
    }

    private void executeDefaultInstanceState() {
        mSelectedTab = getIntent().getIntExtra(Extra.SELECTED_TAB, Constants.TabContent.LOCATION_BASED_LIST);
        mFirstStart = true;
        ActionBar actionBar = getSupportActionBar();
        Log.d(TAG, "executeDefaultInstanceState: selectedNavigationIndex = " + actionBar.getSelectedNavigationIndex());

        flipper.setDisplayedChild(mSelectedTab);
        trackTabScreen();
    }

    private void trackTabScreen() {
        if(mSelectedTab == Constants.TabContent.LOCATION_BASED_LIST) {
            AnalyticsTrackingManager.trackScreen(AnalyticsTrackingManager.TrackableScreensName.NEARBYSCREEN);
        } else if(mSelectedTab == Constants.TabContent.MAP) {
            AnalyticsTrackingManager.trackScreen(AnalyticsTrackingManager.TrackableScreensName.MAPSCREEN);
        }
    }

    public void onStateChange(String tag) {
        if (tag == null) {
            return;
        }

        Log.d(TAG, "onStateChange " + tag);
        String readableName = tag.replaceAll("Fragment", "");
//        mTrackerWrapper.track(readableName);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Extra.SELECTED_TAB, mSelectedTab);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        View filterWheelChairs = findViewById(R.id.menu_filter);
        MapActivityUtils.setAccessFilterOptionDrawable(this, null, filterWheelChairs);

        View filterWc = findViewById(R.id.menu_wc);
        MapActivityUtils.setWcFilterOptionsDrawable(this, null, filterWc);


        ActionBar bar = getSupportActionBar();
        if(bar == null){
            return true;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        final View customView = inflater.inflate(R.layout.actionbar,
                null);

        ImageView addItem = (ImageView)  customView.findViewById(R.id.menu_new_poi);
         OnClickListener l = new OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewPoi();
            }
        };
        addItem.setOnClickListener(l);

        TextView title = (TextView) customView.findViewById(R.id.title);
        int title_res;
        if(mapModeType == MapModeType.MAP_MODE_ENGAGE) {
            title_res = R.string.title_engage;
            AnalyticsTrackingManager.trackScreen(AnalyticsTrackingManager.TrackableScreensName.CONTRIBUTESCREEN);
        } else {
            title_res = mSelectedTab == Constants.TabContent.LOCATION_BASED_LIST ? R.string.dashboard_button_title_nearby : R.string.dashboard_button_title_map;
        }
        title.setText(title_res);

        bar.setCustomView(customView, new ActionBar.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        bar.setDisplayShowCustomEnabled(true);

        ImageView listMapToggle = (ImageView)findViewById(R.id.switch_view);
        if(listMapToggle != null) {
            initMapSwitchListOptionsItem(listMapToggle, title);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean b = onOptionItemClicked(item.getItemId(), null, item);
        return b ? b : super.onOptionsItemSelected(item);
    }

    public boolean onOptionItemClicked(int id,View v, MenuItem item){

        switch (id) {
            case R.id.menu_search:
                if(mWorkerFragment.isSearchMode()){
                    mWorkerFragment.setSearchMode(false);
                    mWorkerFragment.requestUpdate(null);
                }else{
                    showSearch();
                }
                return true;
            case R.id.menu_filter_kategorie:
                showFilterCategories();
                return true;
            case R.id.menu_filter:
            case R.id.menu_wc:
                View anchor = v;
                if(anchor == null){
                    anchor = item.getActionView();
                }
                showFilterSettings(item,v,anchor);
                return true;
            case R.id.menu_about:
                showInfo();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onOptionItemBottomClicked(View view){
        onOptionItemClicked(view.getId(), view, null);
    }

    private void initMapSwitchListOptionsItem(final ImageView listMapToggle, final TextView title){
        int switch_res = mSelectedTab == Constants.TabContent.LOCATION_BASED_LIST ? R.drawable.ic_map : R.drawable.ic_list;
        listMapToggle.setImageResource(switch_res);
        listMapToggle.setAdjustViewBounds(true);
        OnClickListener l = new OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectedTab = mSelectedTab == Constants.TabContent.LOCATION_BASED_LIST ? Constants.TabContent.MAP : Constants.TabContent.LOCATION_BASED_LIST;

                int switch_res = mSelectedTab == Constants.TabContent.LOCATION_BASED_LIST ? R.drawable.ic_map : R.drawable.ic_list;
                int title_res = mSelectedTab == Constants.TabContent.LOCATION_BASED_LIST ? R.string.dashboard_button_title_nearby : R.string.dashboard_button_title_map;
                listMapToggle.setImageResource(switch_res);
                flipper.showNext();

                title.setText(title_res);

                AnalyticsTrackingManager.trackScreen(mSelectedTab == Constants.TabContent.LOCATION_BASED_LIST ? AnalyticsTrackingManager.TrackableScreensName.NEARBYSCREEN : AnalyticsTrackingManager.TrackableScreensName.MAPSCREEN);
            }
        };
        listMapToggle.setOnClickListener(l);
    }

    private void showInfo() {
        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
    }

    private void showFilterCategories() {
        Intent intent = new Intent(this, FilterActivity.class);
        intent.putExtra(Extra.FILTER_CATEGORIES, true);
        startActivity(intent);
    }

    private void showFilterSettings(MenuItem menuItem, View menuView,View anchor) {
        FilterWindow filter = new FilterWindow(this, null, menuView);
        filter.showAsDropDown(anchor);
    }

    private long insertNewPoi() {
        Location location = MyLocationManager.getLastLocation();
        String name = getString(R.string.poi_new_default_name);

        return PrepareDatabaseHelper.insertNew(getContentResolver(), name,
                location.getLatitude(), location.getLongitude());
    }

    private void createNewPoi() {
        long poiId = insertNewPoi();
        Intent i = new Intent(this, POIDetailEditableActivity.class);
        i.putExtra(Extra.POI_ID, poiId);
        startActivity(i);
    }


    private void showSearch() {
        FragmentManager fm = getSupportFragmentManager();
        SearchDialogCombinedFragment searchDialog = SearchDialogCombinedFragment
                .newInstance();

        searchDialog.setTargetFragment(mWorkerFragment, 0);
        searchDialog.show(fm, SearchDialogFragment.TAG);
    }

    @Override
    public void onError(RestServiceException e) {

        if (e.isNetworkError()) {
            try{
                Crouton.makeText(this, e.getRessourceString(), Style.ALERT).show();
            }catch(Exception ex){
                return;
            }
            return;
        }

        FragmentManager fm = getSupportFragmentManager();
        ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance(e,
                Extra.UNKNOWN);
        if (errorDialog == null) {
            return;
        }

        errorDialog.show(fm, TAG);
    }

    @Override
    public void onShowDetail(Fragment fragment, ContentValues values) {
        long copyId = PrepareDatabaseHelper.createCopyFromContentValues(
                getContentResolver(), values, false);
        Intent intent = new Intent(this, POIDetailActivity.class);
        intent.putExtra(Extra.POI_ID, copyId);
        startActivity(intent);
    }

    @Override
    public void onRefreshing(boolean isRefreshing) {
        onRefresh = isRefreshing;

        if (mProgressListener != null && isRefreshing) {
            mProgressListener.onProgressChanged(true);
        }

        if(isRefreshing) {
            if (getSupportActionBar() != null) {
                View progress = getSupportActionBar().getCustomView().findViewById(R.id.progress);
                if (progress.getVisibility() != View.VISIBLE) {
                    progress.setVisibility(View.VISIBLE);
                    checkProgressHide();
                }
            }
        }


    }

    /**
     * Methode to check progress-hiding
     * - prevent multiple hide-show-hide-show-...-actions by checking hide delayed
     */
    private void checkProgressHide(){
        if(!onRefresh) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().getCustomView().findViewById(R.id.progress).setVisibility(View.INVISIBLE);
            }
            if (mProgressListener != null) {
                mProgressListener.onProgressChanged(false);
            }
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkProgressHide();
                }
            }, 500);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        onRefresh = false;
    }

    @Override
    public void onSearchModeChange(boolean isSearchMode) {
        Log.d(TAG, "onSearchModeChange: showing custom view in actionbar");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowCustomEnabled(true);
        }
    }

    @Override
    public void refreshRegisterList(ListView listView) {
        // TODO - use for progressbar?!
    }

    @Override
    public void onRefreshEnabled(boolean refreshEnabled) {
        // TODO - use for progressbar?!
    }

    @Override
    public void onBackPressed() {

        WheelmapApp app = (WheelmapApp) this.getApplicationContext();

        if(app.isSaved()){
            app.setSaved(false);
            mWorkerFragment.setSearchMode(false);
            mWorkerFragment.requestUpdate(null);
        }else{
            super.onBackPressed();
        }
    }

    public void resetKategorieFilter(){
        Uri mUri = Support.CategoriesContent.CONTENT_URI;
        Cursor c = getContentResolver().query(mUri,
                Support.CategoriesContent.PROJECTION, null, null,
                Support.CategoriesContent.DEFAULT_SORT_ORDER);

        for(int i=0;i<c.getCount();i++){
            c.moveToPosition(i);
            int catId = Support.CategoriesContent.getCategoryId(c);

            ContentResolver resolver = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(Support.CategoriesContent.SELECTED,
                    Support.CategoriesContent.SELECTED_YES);

            String whereClause = "( " + Support.CategoriesContent.CATEGORY_ID
                    + " = ?)";
            String[] whereValues = new String[]{Integer.toString(catId)};
            resolver.update(mUri, values, whereClause, whereValues);
        }
        c.close();
    }

    @Override
    public void addProgressListener(Progress.Listener listener) {
        mProgressListener = listener;
    }
}
