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


import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ObjectAnimator;

import org.mapsforge.android.maps.GeoPoint;
import org.wheelmap.android.activity.listeners.Progress;
import org.wheelmap.android.activity.profile.ProfileActivity;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.fragment.CombinedWorkerFragment;
import org.wheelmap.android.fragment.DisplayFragmentListener;
import org.wheelmap.android.fragment.ErrorDialogFragment;
import org.wheelmap.android.fragment.POIDetailFragment;
import org.wheelmap.android.fragment.POIDetailFragment.OnPOIDetailListener;
import org.wheelmap.android.fragment.POIsListFragment;
import org.wheelmap.android.fragment.POIsOsmdroidFragment;
import org.wheelmap.android.fragment.SearchDialogCombinedFragment;
import org.wheelmap.android.fragment.SearchDialogFragment;
import org.wheelmap.android.fragment.WheelchairAccessibilityStateFragment;
import org.wheelmap.android.fragment.WheelchairToiletStateFragment;
import org.wheelmap.android.fragment.WorkerFragment;
import org.wheelmap.android.fragment.WorkerFragmentListener;
import org.wheelmap.android.manager.MyLocationManager;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.MapModeType;
import org.wheelmap.android.model.PrepareDatabaseHelper;
import org.wheelmap.android.model.Request;
import org.wheelmap.android.model.WheelchairFilterState;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.modules.AppProperties;
import org.wheelmap.android.modules.IAppProperties;
import org.wheelmap.android.modules.UserCredentials;
import org.wheelmap.android.online.R;
import org.wheelmap.android.popup.FilterWindow;
import org.wheelmap.android.service.RestServiceException;
import org.wheelmap.android.service.RestServiceHelper;
import org.wheelmap.android.utils.MapActivityUtils;
import org.wheelmap.android.utils.PressSelector;
import org.wheelmap.android.utils.SmoothInterpolator;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Interpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.LayoutParams;
import android.widget.ProgressBar;

import de.akquinet.android.androlog.Log;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainMultiPaneActivity extends MapActivity implements
        DisplayFragmentListener, WorkerFragmentListener, OnPOIDetailListener,
        OnClickListener , Progress.Provider{

    private static final String TAG = MainMultiPaneActivity.class
            .getSimpleName();


    IAppProperties appProperties;

    private POIsListFragment mListFragment;

    private POIsOsmdroidFragment mMapFragment;

    private POIDetailFragment mDetailFragment;

    private CombinedWorkerFragment mWorkerFragment;

    private ViewGroup mMovableLayout;

    private ImageButton mResizeButton;

    private static final Interpolator SMOOTH_INTERPOLATOR = new SmoothInterpolator();

    private static final long MOVABLE_ANIMATION_DURATION = 800;

    private boolean mMovableVisible;

    private boolean mMovableAnimationRunning;

    Long poiIdSelected = Extra.ID_UNKNOWN;

    private MapModeType mapModeType;

    private WheelmapApp app;

    private String address = null;

    private boolean onRefresh = false;

    /**
     * used for testCases
     */
    Progress.Listener mProgressListener;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        super.onCreate(savedInstanceState);

        appProperties = new AppProperties(WheelmapApp.getApp());
        Log.d(TAG, "onCreate");

        setProgressBarIndeterminate(true);
        setSupportProgressBarIndeterminateVisibility(false);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);

        setContentView(R.layout.activity_multipane);
        mMovableLayout = (ViewGroup) findViewById(R.id.movable_layout);
        mResizeButton = (ImageButton) findViewById(R.id.button_movable_resize);

        ViewGroup g = (ViewGroup) findViewById(R.id.layout_multi);
        if (Build.VERSION.SDK_INT > 16) {
            g.getLayoutTransition().disableTransitionType(LayoutTransition.APPEARING);
        }

        if (savedInstanceState != null) {
            executeState(savedInstanceState);
        } else {
            executeDefaultInstanceState();
        }

        Bundle extras = getIntent().getExtras();
        if (extras.containsKey(Extra.MAP_MODE_ENGAGE)) {
            mapModeType = MapModeType.MAP_MODE_ENGAGE;
        } else {
            mapModeType = MapModeType.MAP_MODE_NORMAL;
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        mResizeButton.setOnClickListener(this);

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

        mDetailFragment = (POIDetailFragment) fm
                .findFragmentById(R.id.detail_layout);
        if (mDetailFragment == null) {
            mDetailFragment = POIDetailFragment.newInstance();
            t.add(R.id.detail_layout, mDetailFragment);
        }

        t.commit();

        WheelmapApp.checkForUpdates(this);

        app = (WheelmapApp) this.getApplication();
        String uri = null;

        try {
            address = app.getAddressString();
        } catch (Exception ex) {}

        if (address != null) {
            showSearch();
        }
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

        WheelmapApp.checkForCrashes(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void executeIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }

        executeState(extras);
    }

    private void executeState(Bundle state) {
        mMovableVisible = state.getBoolean(Extra.MOVABLE_VISIBLE);
        if (!mMovableVisible) {
            setMovableGone();
        }
    }

    private void executeDefaultInstanceState() {
        setMovableGone();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Extra.MOVABLE_VISIBLE, mMovableVisible);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        ActionBar bar = getSupportActionBar();
        if(bar == null){
            return true;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View customView = inflater.inflate(R.layout.actionbar_tablet,
                null);
        bar.setCustomView(customView, new ActionBar.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
                Gravity.CENTER_VERTICAL | Gravity.END));

        boolean isPortraitMode = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (isPortraitMode) {

            ImageView addItem = (ImageView)  customView.findViewById(R.id.menu_new_poi);
            addItem.setVisibility(View.VISIBLE);
            OnClickListener addClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    createNewPoi();
                }
            };
            addItem.setOnClickListener(addClickListener);

            LinearLayout l = (LinearLayout) findViewById(R.id.actionbar_bottom);
            for (int i = 0; i < l.getChildCount(); i++) {
                l.getChildAt(i).setOnTouchListener(new PressSelector());
            }

            bar.setDisplayShowCustomEnabled(true);
            View v = findViewById(R.id.menu_filter);
            MapActivityUtils.setAccessFilterOptionDrawable(this, null, v);

            View filterWc = findViewById(R.id.menu_wc);
            MapActivityUtils.setWcFilterOptionsDrawable(this, null, filterWc);


            UserCredentials credentials = new UserCredentials(getApplicationContext());
            ImageView image = (ImageView) findViewById(R.id.menu_login);
            image.setImageResource(credentials.isLoggedIn()
                    ? R.drawable.start_icon_logged_in
                    : R.drawable.start_icon_login);
        } else {
            MenuInflater inflaterMenu = getMenuInflater();
            inflaterMenu.inflate(R.menu.ab_multi_activity, menu);
            MenuItem item = menu.findItem(R.id.menu_filter);
            MapActivityUtils.setAccessFilterOptionDrawable(this, item, null);
            item = menu.findItem(R.id.menu_wc);
            MapActivityUtils.setWcFilterOptionsDrawable(this, item, null);

        }

        if (mapModeType == MapModeType.MAP_MODE_ENGAGE) {
            MenuItem itemFilterWheelChairs = menu.findItem(R.id.menu_filter);
            itemFilterWheelChairs.setEnabled(false);
            //TODO Disable it - doesn't work yet
        }

        ImageView listMapToggle = (ImageView)findViewById(R.id.switch_view);
        if(listMapToggle != null) {
            listMapToggle.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleMovableResize();
                }
            });
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean b = onOptionItemClicked(item.getItemId(), item.getActionView(), item);
        return b ? b : super.onOptionsItemSelected(item);
    }


    public void onOptionItemBottomClicked(View v) {
        onOptionItemClicked(v.getId(), v, null);
    }

    /*
     * combined method to handle actionbar menu and custom bottom menu
     */
    public boolean onOptionItemClicked(int id, View v, MenuItem item) {

        switch (id) {
            case R.id.menu_search:
                if (mWorkerFragment.isSearchMode()) {
                    mWorkerFragment.setSearchMode(false);
                    mWorkerFragment.requestUpdate(null);
                } else {
                    showSearch();
                }
                return true;
            case R.id.menu_filter_kategorie:
                showFilterCategories();
                return true;
            case R.id.menu_filter:
            case R.id.menu_wc:
                View anchor = v;
                if (anchor == null) {
                    anchor = item.getActionView();
                }
                showFilterSettings(item, v, anchor);
                return true;
            case R.id.menu_about:
                showInfo();
                return true;
            case R.id.menu_new_poi:
                createNewPoi();
                return true;
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_login:
                showAccount();
                return true;
            case R.id.menu_news:
                showNews();
                return true;
            default:
                return false;
        }
    }

    private void showNews() {
        Intent intent = new Intent(this, WebViewNewsActivity.class);
        startActivity(intent);
    }

    private void showAccount() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivityForResult(intent, Request.REQUEST_CODE_LOGIN);
    }

    private void showSearch() {
        FragmentManager fm = getSupportFragmentManager();
        SearchDialogCombinedFragment searchDialog = SearchDialogCombinedFragment
                .newInstance();

        searchDialog.setTargetFragment(mWorkerFragment, 0);
        searchDialog.show(fm, SearchDialogFragment.TAG);
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

    private void showFilterSettings(MenuItem menuItem, View menuView, View anchor) {
        FilterWindow filter = new FilterWindow(this, null, menuView);
        filter.showAsDropDown(anchor);
    }

    private long insertNewPoi() {
        Location location = MyLocationManager.getLastLocation();
        String name = getString(R.string.poi_new_default_name);
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
    public void onError(RestServiceException e) {
        if (e.isNetworkError()) {
            String error = getString(R.string.error_network_failure);
            try {
                error = getString(e.getRessourceString());
            } catch (Exception ex) {
            }
            Crouton.makeText(this, error, Style.ALERT).show();
            return;
        }

        FragmentManager fm = getSupportFragmentManager();
        ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance(e,
                Extra.UNKNOWN);
        if (errorDialog == null) {
            return;
        }

        errorDialog.show(fm, ErrorDialogFragment.TAG);
    }

    @Override
    public void onShowDetail(Fragment fragment, ContentValues values) {
        long copyId = PrepareDatabaseHelper.createCopyFromContentValues(
                getContentResolver(), values, true);
        poiIdSelected = copyId;

        if (!mMovableVisible) {
            toggleMovableResize();
        }
        mDetailFragment.showDetail(poiIdSelected);

        if (fragment == mListFragment) {
            mMapFragment.markItem(values, true);
        }

        if (fragment == mMapFragment) {
            mListFragment.markItem(values, false);
        }
    }

    @Override
    public void onRefreshing(boolean isRefreshing) {
        Log.d(TAG, "onRefreshing isRefreshing = " + isRefreshing);

        onRefresh = isRefreshing;

        if (mProgressListener != null && isRefreshing) {
            mProgressListener.onProgressChanged(true);
        }

        if(isRefreshing) {
            if (getSupportActionBar() != null && getSupportActionBar().getCustomView() != null) {
                View progress = getSupportActionBar().getCustomView().findViewById(R.id.progress);
                if (progress.getVisibility() != View.VISIBLE) {
                    progress.setVisibility(View.VISIBLE);
                    checkProgressHide();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        onRefresh = false;
    }

    private void checkProgressHide(){
        if(!onRefresh) {

            if (getSupportActionBar() != null && getSupportActionBar().getCustomView() != null) {
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
    public void onSearchModeChange(boolean isSearchMode) {
        Log.d(TAG, "onSearchModeChange: showing custom view in actionbar");
        getSupportActionBar().setDisplayShowCustomEnabled(true);
    }

    @Override
    public void onEdit(long poiId, int focus) {
        Intent intent = new Intent(this, POIDetailEditableActivity.class);
        intent.putExtra(Extra.POI_ID, poiId);
        intent.putExtra("Focus", focus);
        startActivity(intent);
    }

    @Override
    public void onEditWheelchairState(WheelchairFilterState wState) {
        Intent intent = new Intent(this, WheelchairStateActivity.class);
        intent.putExtra(Extra.WHEELCHAIR_STATE, wState.getId());
        startActivityForResult(intent, Request.SELECT_WHEELCHAIRSTATE);
    }

    @Override
    public void onEditWheelchairToiletState(WheelchairFilterState wState) {
        Intent intent = new Intent(this, WheelchairStateActivity.class);
        intent.putExtra(Extra.WHEELCHAIR_TOILET_STATE, wState.getId());
        startActivityForResult(intent, Request.SELECT_WHEELCHAIRSTATE);
    }

    @Override
    public void onShowLargeMapAt(GeoPoint point) {
        // noop
    }

    @Override
    public void dismissDetailView() {
        toggleMovableResize();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: requestCode = " + requestCode
                + " resultCode = " + resultCode);
        if (requestCode == Request.SELECT_WHEELCHAIRSTATE && resultCode == RESULT_OK && data != null) {
            if (data.hasExtra(WheelchairAccessibilityStateFragment.TAG)) {
                WheelchairFilterState state = WheelchairFilterState
                        .valueOf(data.getIntExtra(WheelchairAccessibilityStateFragment.TAG, Extra.UNKNOWN));
                if (state != null) {
                    updateDatabase(poiIdSelected, POIs.WHEELCHAIR, state);
                }
            } else if (data.hasExtra(WheelchairToiletStateFragment.TAG)) {
                WheelchairFilterState state = WheelchairFilterState
                        .valueOf(data.getIntExtra(WheelchairToiletStateFragment.TAG, Extra.UNKNOWN));
                if (state != null) {
                    updateDatabase(poiIdSelected, POIs.WHEELCHAIR_TOILET, state);
                }
            } else {
                return;
            }
            Log.d(TAG, "starting RestServiceHelper.executeUpdateServer");
            RestServiceHelper.executeUpdateServer(this, null);

        }
        if (requestCode == Request.REQUEST_CODE_LOGIN) {
            ImageView image = (ImageView) findViewById(R.id.menu_login);
            if (image != null) {
                image.setImageResource(resultCode == RESULT_OK
                        ? R.drawable.start_icon_logged_in
                        : R.drawable.start_icon_login);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateDatabase(long id, String poiColumnName, WheelchairFilterState state) {
        if (id == Extra.ID_UNKNOWN || state == null) {
            return;
        }
        Log.d(TAG,
                "updating id = " + id + " state = "
                        + state.asRequestParameter());

        ContentValues values = new ContentValues();
        values.put(poiColumnName, state.getId());
        values.put(POIs.DIRTY, POIs.DIRTY_STATE);

        PrepareDatabaseHelper.editCopy(getContentResolver(), id, values);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.button_movable_resize:
                toggleMovableResize();
        }
    }

    private AnimatorListener mMovableAnimatorListener = new AnimatorListener() {

        @Override
        public void onAnimationStart(Animator animation) {
            mMapFragment.setHeightFull(!mMovableVisible);
            if (mMovableVisible) {
                mMovableLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (!mMovableVisible) {
                mMovableLayout.setVisibility(View.GONE);
            }

            mMovableAnimationRunning = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }

    };

    private void setCollapseButtonImage(boolean toCollapse) {
        int buttonDrawableRes;
        if (toCollapse) {
            buttonDrawableRes = R.drawable.ic_detail_collapse;
        } else {
            buttonDrawableRes = R.drawable.ic_detail_expand;
        }
        mResizeButton.setImageResource(buttonDrawableRes);

    }

    private void setMovableGone() {
        mMovableVisible = false;
        Log.d(TAG, "setMovableGone height = " + (-mMovableLayout.getHeight()));
        mMovableLayout.setVisibility(View.INVISIBLE);
    }

    private void toggleMovableResize() {
        if (mMovableAnimationRunning) {
            return;
        }
        mMovableAnimationRunning = true;
        setCollapseButtonImage(!mMovableVisible);

        boolean land = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        String change = land ? "translationX" : "translationY";

        float startValue;
        float endValue;
        if (mMovableVisible) {
            startValue = 0.0f;
            if (land) {
                endValue = -mMovableLayout.getWidth();
            } else {
                endValue = -mMovableLayout.getHeight();
            }
            mMovableVisible = false;
        } else {
            if (land) {
                startValue = -mMovableLayout.getWidth();
            } else {
                startValue = -mMovableLayout.getHeight();
            }
            endValue = 0.0f;
            mMovableVisible = true;
        }

        ObjectAnimator anim = ObjectAnimator.ofFloat(mMovableLayout,
                change, startValue, endValue);
        anim.setInterpolator(SMOOTH_INTERPOLATOR);
        anim.setDuration(MOVABLE_ANIMATION_DURATION);
        anim.addListener(mMovableAnimatorListener);
        anim.start();
    }

    @Override
    public void onRefreshEnabled(boolean refreshEnabled) {

    }

    @Override
    public void refreshRegisterList(ListView listView) {

    }

    @Override
    public void onBackPressed() {
        WheelmapApp app = (WheelmapApp) this.getApplicationContext();

        if (app.isSaved()) {
            app.setSaved(false);
            mWorkerFragment.setSearchMode(false);
            mWorkerFragment.requestUpdate(null);
        } else {
            if (mMovableVisible && !mMovableAnimationRunning) {
                toggleMovableResize();
                return;
            }
            super.onBackPressed();
        }
    }


    @Override
    public void addProgressListener(Progress.Listener listener) {
        mProgressListener = listener;
    }
}
