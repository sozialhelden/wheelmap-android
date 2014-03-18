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

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.preference.PreferenceManager;
import com.google.inject.Inject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.LayoutParams;
import com.actionbarsherlock.internal.view.menu.MenuItemImpl;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ObjectAnimator;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;
import org.mapsforge.android.maps.GeoPoint;
import org.wheelmap.android.fragment.*;
import org.wheelmap.android.fragment.POIDetailFragment.OnPOIDetailListener;
import org.wheelmap.android.manager.MyLocationManager;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.MapModeType;
import org.wheelmap.android.model.PrepareDatabaseHelper;
import org.wheelmap.android.model.WheelchairState;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.modules.IAppProperties;
import org.wheelmap.android.online.R;
import org.wheelmap.android.popup.FilterWindow;
import org.wheelmap.android.service.RestServiceException;
import org.wheelmap.android.service.RestServiceHelper;
import org.wheelmap.android.utils.MapActivityUtils;
import org.wheelmap.android.utils.PressSelector;
import org.wheelmap.android.utils.SmoothInterpolator;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import de.akquinet.android.androlog.Log;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import roboguice.inject.InjectView;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Activity.Addons(value = {Activity.ADDON_SHERLOCK, "MyRoboguice"})
public class MainMultiPaneActivity extends MapActivity implements
        DisplayFragmentListener, WorkerFragmentListener, OnPOIDetailListener,
        OnClickListener {

    private static final String TAG = MainMultiPaneActivity.class
            .getSimpleName();

    static final private int SELECT_WHEELCHAIRSTATE = 0;

    @Inject
    IAppProperties appProperties;

    private POIsListFragment mListFragment;

    private POIsMapsforgeFragment mMapFragment;

    private POIDetailFragment mDetailFragment;

    private CombinedWorkerFragment mWorkerFragment;

    @InjectView(R.id.movable_layout)
    private ViewGroup mMovableLayout;

    @InjectView(R.id.button_movable_resize)
    private ImageButton mResizeButton;

    private static final Interpolator SMOOTH_INTERPOLATOR = new SmoothInterpolator();

    private static final long MOVABLE_ANIMATION_DURATION = 800;

    private boolean mMovableVisible;

    private boolean mMovableAnimationRunning;

    private boolean mMovableGoneByButton;

    Long poiIdSelected = Extra.ID_UNKNOWN;

    private MapModeType mapModeType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setSupportProgressBarIndeterminateVisibility(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setLogo(R.drawable.title_logo_shadow_tablet);

        setContentView(R.layout.activity_multipane);

        // FragmentManager.enableDebugLogging(true);

        if (savedInstanceState != null) {
            executeState(savedInstanceState);
        } else {
            executeDefaultInstanceState();
        }

        Bundle extras = getIntent().getExtras();
        if(extras.containsKey(Extra.MAP_MODE_ENGAGE)) {
            mapModeType = MapModeType.MAP_MODE_ENGAGE;
        } else {
            mapModeType = MapModeType.MAP_MODE_NORMAL;
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        createSearchModeCustomView(actionBar);

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

        mMapFragment = (POIsMapsforgeFragment) fm
                .findFragmentById(R.id.map_layout);
        if (mMapFragment == null) {
            mMapFragment = POIsMapsforgeFragment.newInstance(false, true);
            t.add(R.id.map_layout, mMapFragment, POIsMapsforgeFragment.TAG);
        }

        mDetailFragment = (POIDetailFragment) fm
                .findFragmentById(R.id.detail_layout);
        if (mDetailFragment == null) {
            mDetailFragment = POIDetailFragment.newInstance();
            t.add(R.id.detail_layout, mDetailFragment);
        }

        t.commit();

        /*
        LinearLayout layout = (LinearLayout) getWindowDecorView().getParent().getParent();
        View view = layout.getChildAt(0);
        layout.removeViewAt(0);
        layout.addView(view);*/
    }

    public WorkerFragment getWorkerFragment(){
        return mWorkerFragment;
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

        boolean isPortraitMode = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if(isPortraitMode){
            ActionBar bar = getSupportActionBar();
            //g.setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
            LayoutInflater inflater = LayoutInflater.from(this);
            View customView = inflater.inflate(R.layout.actionbar_tablet,
                    null);

            final ImageView switchView = (ImageView)  customView.findViewById(R.id.show_list);
            switchView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleMovableResize();
                }
            });

            LinearLayout l = (LinearLayout)findViewById(R.id.actionbar_bottom);
            for(int i=0;i<l.getChildCount();i++){
                l.getChildAt(i).setOnTouchListener(new PressSelector());
            }

                   //LayoutParams.MATCH_PARENT
            bar.setCustomView(customView, new ActionBar.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
                    Gravity.CENTER_VERTICAL | Gravity.RIGHT));
            bar.setDisplayShowCustomEnabled(true);
            View v = findViewById(R.id.menu_filter);
            MapActivityUtils.setFilterDrawable(this, null, v);
        }else{
            MenuInflater inflaterMenu = getSupportMenuInflater();
            inflaterMenu.inflate(R.menu.ab_multi_activity, menu);
            MenuItem item = menu.findItem(R.id.menu_filter);
            MapActivityUtils.setFilterDrawable(this, item, null);
        }

        if(mapModeType == MapModeType.MAP_MODE_ENGAGE) {
            MenuItem itemFilterWheelChairs = menu.findItem(R.id.menu_filter);
            itemFilterWheelChairs.setEnabled(false);
            //TODO Disable it - doesn't work yet
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       boolean b = onOptionItemClicked(item.getItemId(),null,item);
       return b ? b : super.onOptionsItemSelected(item);
    }


    public void onOptionItemBottomClicked(View v){
        onOptionItemClicked(v.getId(),v,null);
    }

    /*
     * combined method to handle actionbar menu and custom bottom menu
     */
    public boolean onOptionItemClicked(int id,View v, MenuItem item){

        switch (id) {
            case R.id.menu_search:
                showSearch();
                return true;
            case R.id.menu_filter_kategorie:
                showFilterCategories();
                return true;
            case R.id.menu_filter:
                View anchor = v;
                if(anchor == null){
                    anchor = item.getActionView();
                }
                showFilterSettings(item,v,anchor);
                //MapActivityUtils.setFilterDrawable(this,item,v);
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
            default:
                return false;
        }
    }

    private void showAccount(){
        /*FragmentManager fm = getSupportFragmentManager();
        LoginDialogFragment loginDialog = new LoginDialogFragment();
        loginDialog.show(fm);       */
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }

    @SuppressLint("WrongViewCast")
    private void createSearchModeCustomView(final ActionBar bar) {
        // not used anymore
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
                        CombinedWorkerFragment.TAG);
                if (f == null) {
                    return;
                }

                ((CombinedWorkerFragment) f).setSearchMode(false);
                bar.setDisplayShowCustomEnabled(false);
            }
        });

        bar.setCustomView(customView, new ActionBar.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
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

    private void showFilterSettings(MenuItem menuItem, View menuView,View anchor) {
        //Intent intent = new Intent(this, NewSettingsActivity.class);
        //startActivity(intent);
        FilterWindow filter = new FilterWindow(this,menuView,menuItem);
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
            Crouton.makeText(this, e.getRessourceString(), Style.ALERT).show();
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
        setSupportProgressBarIndeterminateVisibility(isRefreshing);
    }

    @Override
    public void onSearchModeChange(boolean isSearchMode) {
        Log.d(TAG, "onSearchModeChange: showing custom view in actionbar");
        getSupportActionBar().setDisplayShowCustomEnabled(true);
    }

    @Override
    public void onEdit(long poiId) {
        Intent intent = new Intent(this, POIDetailEditableActivity.class);
        intent.putExtra(Extra.POI_ID, poiId);
        startActivity(intent);
    }

    @Override
    public void onEditWheelchairState(WheelchairState wState) {
        Intent intent = new Intent(this, WheelchairStateActivity.class);
        intent.putExtra(Extra.WHEELCHAIR_STATE, wState.getId());
        startActivityForResult(intent, SELECT_WHEELCHAIRSTATE);
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
        if (requestCode == SELECT_WHEELCHAIRSTATE) {
            if (resultCode == RESULT_OK) {
                // newly selected wheelchair state as action data
                if (data != null) {
                    WheelchairState state = WheelchairState
                            .valueOf(data.getIntExtra(Extra.WHEELCHAIR_STATE,
                                    Extra.UNKNOWN));
                    updateDatabase(poiIdSelected, state);
                    Log.d(TAG, "starting RestServiceHelper.executeUpdateServer");
                    RestServiceHelper.executeUpdateServer(this, null);
                }
            }
        }
    }

    private void updateDatabase(long id, WheelchairState state) {
        if (id == Extra.ID_UNKNOWN || state == null) {
            return;
        }
        Log.d(TAG,
                "updating id = " + id + " state = "
                        + state.asRequestParameter());

        ContentValues values = new ContentValues();
        values.put(POIs.WHEELCHAIR, state.getId());
        values.put(POIs.DIRTY, POIs.DIRTY_STATE);

        PrepareDatabaseHelper.editCopy(getContentResolver(), id, values);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.button_movable_resize:
                toggleMovableResize();
                mMovableGoneByButton = !mMovableVisible;
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

    private boolean mIsFirstMoveIn;

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
            if(land){
                endValue = -mMovableLayout.getWidth();
            }else{
                endValue = -mMovableLayout.getHeight();
            }
            mMovableVisible = false;
        } else {
            if(land){
                startValue = -mMovableLayout.getWidth();
            }else{
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
        if(mMovableVisible && !mMovableAnimationRunning){
            toggleMovableResize();
            return;
        }
        super.onBackPressed();
    }
}
