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
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.fragment.CombinedWorkerFragment;
import org.wheelmap.android.fragment.DisplayFragmentListener;
import org.wheelmap.android.fragment.ErrorDialogFragment;
import org.wheelmap.android.fragment.POIsListFragment;
import org.wheelmap.android.fragment.POIsMapWorkerFragment;
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
import org.wheelmap.android.tracker.TrackerWrapper;
import org.wheelmap.android.utils.MapActivityUtils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.ViewFlipper;

import de.akquinet.android.androlog.Log;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class
        MainSinglePaneActivity extends MapActivity implements
        DisplayFragmentListener, WorkerFragmentListener, OnStateListener {

    private static final String TAG = MainSinglePaneActivity.class.getSimpleName();

    //@Inject
    IAppProperties appProperties;

    private final static int DEFAULT_SELECTED_TAB = 0;

    private int mSelectedTab = DEFAULT_SELECTED_TAB;

    private TrackerWrapper mTrackerWrapper;

    public boolean mFirstStart;

    private CombinedWorkerFragment mWorkerFragment;
    private POIsListFragment mListFragment;
    private POIsOsmdroidFragment mMapFragment;
    private ViewFlipper flipper;

    private MapModeType mapModeType;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_single_pane);


        //        appProperties = AppProperties.getInstance(getApplication());
        appProperties = AppProperties.getInstance(WheelmapApp.getApp());
        Log.d(TAG, "onCreate");


        setSupportProgressBarIndeterminateVisibility(false);

        ActionBar actionbar = getSupportActionBar();
        if(actionbar != null){
            actionbar.setHomeButtonEnabled(true);
            actionbar.setDisplayShowTitleEnabled(true);
            actionbar.setDisplayHomeAsUpEnabled(true);
        }


        flipper = (ViewFlipper) findViewById(R.id.flipper);
        flipper.setDisplayedChild(0);

        FragmentManager.enableDebugLogging(true);

        mTrackerWrapper = new TrackerWrapper(this);


        ActionBar actionBar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(false);
       // createSearchModeCustomView(actionBar);
        View customNav = LayoutInflater.from(this).inflate(R.layout.actionbar, null);
        actionBar.setCustomView(customNav);
       // mTabListener = new MyTabListener(this);


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

        configureRefresh();

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
        mSelectedTab = state.getInt(Extra.SELECTED_TAB, DEFAULT_SELECTED_TAB);
        mFirstStart = false;

        flipper.setDisplayedChild(mSelectedTab);

    }

    private void executeDefaultInstanceState() {
        mSelectedTab = getIntent().getIntExtra(Extra.SELECTED_TAB, DEFAULT_SELECTED_TAB);
        mFirstStart = true;
        ActionBar actionBar = getSupportActionBar();
        Log.d(TAG, "executeDefaultInstanceState: selectedNavigationIndex = " + actionBar.getSelectedNavigationIndex());

        flipper.setDisplayedChild(mSelectedTab);
    }

    public void onStateChange(String tag) {
        if (tag == null) {
            return;
        }

        Log.d(TAG, "onStateChange " + tag);
        String readableName = tag.replaceAll("Fragment", "");
        mTrackerWrapper.track(readableName);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Extra.SELECTED_TAB, mSelectedTab);
        super.onSaveInstanceState(outState);
    }

    private void configureRefresh() {
//        // As we're modifying some of the options, create an instance of
//        // PullToRefreshAttacher.Options
//        PullToRefreshAttacher.Options ptrOptions = new PullToRefreshAttacher.Options();
//
//        // Here we make the refresh scroll distance to 50% of the GridView height
//        ptrOptions.refreshScrollDistance = 0.5f;
//
//        // Here we customise the animations which are used when showing/hiding the header view
//        // ptrOptions.headerInAnimation = R.anim.slide_in_top;
//        // ptrOptions.headerOutAnimation = R.anim.slide_out_top;
//
//        // Here we define a custom header layout which will be inflated and used
//        ptrOptions.headerLayout = R.layout.ptr_header;
//
//        mPullToRefreshHelper = PullToRefreshAttacher.get(this, ptrOptions);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        View filterWheelChairs = findViewById(R.id.menu_filter);
        MapActivityUtils.setAccessFilterOptionDrawable(this, null, filterWheelChairs);

        View filterWc = findViewById(R.id.menu_wc);
        MapActivityUtils.setWcFilterOptionsDrawable(this, null, filterWc);


        ActionBar bar = getSupportActionBar();

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
        } else {
            title_res = mSelectedTab == 0 ? R.string.dashboard_button_title_nearby : R.string.dashboard_button_title_map;
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
        boolean b = onOptionItemClicked(item.getItemId(),null,item);
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
        int switch_res = mSelectedTab == 0 ? R.drawable.ic_map : R.drawable.ic_list;
        listMapToggle.setImageResource(switch_res);
        listMapToggle.setAdjustViewBounds(true);
        OnClickListener l = new OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectedTab = mSelectedTab == 0 ? 1: 0;

                int switch_res = mSelectedTab == 0 ? R.drawable.ic_map : R.drawable.ic_list;
                int title_res = mSelectedTab == 0 ? R.string.dashboard_button_title_nearby : R.string.dashboard_button_title_map;
                listMapToggle.setImageResource(switch_res);
                flipper.showNext();

                title.setText(title_res);
            }
        };
        listMapToggle.setOnClickListener(l);
    }

    private void createSearchModeCustomView(final ActionBar bar) {
        if(true){
               return;
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        View customView = inflater.inflate(R.layout.item_ab_searchmodebutton,
                null);
        ImageButton button = (ImageButton) customView.findViewById(R.id.image);
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Fragment f = getSupportFragmentManager().findFragmentByTag(
                        POIsMapWorkerFragment.TAG);
                if (f == null) {
                    return;
                }

                ((POIsMapWorkerFragment) f).setSearchMode(false);
                bar.setDisplayShowCustomEnabled(false);
            }
        });

        bar.setCustomView(customView, new ActionBar.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
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
        //Intent intent = new Intent(this, NewSettingsActivity.class);
        //startActivity(intent);

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
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Extra.POI_ID, copyId);
        startActivity(intent);
    }

    @Override
    public void onRefreshing(boolean isRefreshing) {
        Log.d(TAG, "onRefreshing isRefreshing = " + isRefreshing);
    }

    @Override
    public void onSearchModeChange(boolean isSearchMode) {
        Log.d(TAG, "onSearchModeChange: showing custom view in actionbar");
        createSearchModeCustomView(getSupportActionBar());
        getSupportActionBar().setDisplayShowCustomEnabled(true);
    }

    @Override
    public void refreshRegisterList(ListView listView) {
        //mPullToRefreshHelper.addRefreshableView(listView, this);
    }

    @Override
    public void onRefreshEnabled(boolean refreshEnabled) {
        //mPullToRefreshHelper.setEnabled(refreshEnabled);
    }

    @Override
    public void onBackPressed() {

        WheelmapApp app = (WheelmapApp) this.getApplicationContext();

        if(app.isSaved()){
            app.setSaved(false);
            /*Intent intent = new Intent(getApplicationContext(),MainSinglePaneActivity.class);
            intent.putExtra(Extra.SELECTED_TAB,1);
            startActivity(intent);
            super.onBackPressed(); */
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
}
