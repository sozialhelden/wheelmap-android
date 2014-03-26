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

import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import org.holoeverywhere.app.Activity;
import org.wheelmap.android.fragment.EditPositionFragment;
import org.wheelmap.android.fragment.EditPositionFragment.OnEditPositionListener;
import org.wheelmap.android.fragment.LoginDialogFragment.OnLoginDialogListener;
import org.wheelmap.android.fragment.NodetypeSelectFragment;
import org.wheelmap.android.fragment.NodetypeSelectFragment.OnNodetypeSelectListener;
import org.wheelmap.android.fragment.POIDetailEditableFragment;
import org.wheelmap.android.fragment.POIDetailEditableFragment.OnPOIDetailEditableListener;
import org.wheelmap.android.fragment.WheelchairStateFragment;
import org.wheelmap.android.fragment.WheelchairStateFragment.OnWheelchairState;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.WheelchairState;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.UtilsMisc;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.view.Display;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import de.akquinet.android.androlog.Log;

//@Activity.Addons(value = {Activity.ADDON_SHERLOCK, "MyRoboguice"})
public class POIDetailEditableActivity extends MapActivity implements
        OnPOIDetailEditableListener, OnLoginDialogListener,
        OnEditPositionListener, OnNodetypeSelectListener,
        OnBackStackChangedListener, OnWheelchairState {

    private final static String TAG = POIDetailEditableActivity.class.getSimpleName();

    private Fragment mFragment;

    private ExternalEditableState mExternalEditableState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (UtilsMisc.isTablet(getApplicationContext())) {
            UtilsMisc.showAsPopup(this);

        }
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_frame_empty);
        setSupportProgressBarIndeterminateVisibility(false);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setExternalEditableState(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();
        fm.addOnBackStackChangedListener(this);

        mFragment = fm.findFragmentById(R.id.content);
        if (mFragment != null) {
            return;
        }

        Long poiID = getIntent().getLongExtra(Extra.POI_ID, Extra.ID_UNKNOWN);
        if (poiID == Extra.ID_UNKNOWN) {
            Log.w(TAG, "poi id is not given - cant do anything");
            return;
        }

        mFragment = POIDetailEditableFragment.newInstance(poiID);
        fm.beginTransaction()
                .add(R.id.content, mFragment,
                        POIDetailEditableFragment.TAG).commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*private void showAsPopup(Activity activity) {
        activity.requestWindowFeature(Window.FEATURE_ACTION_BAR);
        activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        LayoutParams params = activity.getWindow().getAttributes();
        params.height = LayoutParams.WRAP_CONTENT;

        Display display = getWindowManager().getDefaultDisplay();
        int width = 600;
        if (android.os.Build.VERSION.SDK_INT < 13) {
            width = display.getWidth();
        } else {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
        }
        width /= 2;
        params.width = width < 600 ? 600 : width;
        params.alpha = 1.0f;
        params.dimAmount = 0.5f;
        activity.getWindow().setAttributes(params);
    }*/

    private void setExternalEditableState(Bundle state) {
        mExternalEditableState = new ExternalEditableState();
        if (state != null) {
            mExternalEditableState.restoreState(state);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mExternalEditableState.saveState(outState);
    }

    @Override
    public void onEditWheelchairState(WheelchairState state) {
        mFragment = WheelchairStateFragment.newInstance(state);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content, mFragment, WheelchairStateFragment.TAG);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onWheelchairStateSelect(WheelchairState state) {
        Log.d(TAG, "onWheelchairStateSelect: state = " + state.toString());
        mExternalEditableState.state = state;
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onEditSave() {
        finish();
    }

    @Override
    public void onEditGeolocation(double latitude, double longitude) {
        mFragment = EditPositionFragment.newInstance(latitude, longitude);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content, mFragment, EditPositionFragment.TAG);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onEditPosition(double latitude, double longitude) {
        mExternalEditableState.latitude = latitude;
        mExternalEditableState.longitude = longitude;
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onEditNodetype(int nodetype) {
        mFragment = NodetypeSelectFragment.newInstance(nodetype);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content, mFragment, NodetypeSelectFragment.TAG);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onSelect(int nodetype) {
        mExternalEditableState.nodetype = nodetype;
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onLoginSuccessful() {

    }

    @Override
    public void onLoginCancelled() {
        finish();
    }

    @Override
    public void onBackStackChanged() {
        FragmentManager fm = getSupportFragmentManager();
        mFragment = fm.findFragmentById(R.id.content);
    }

    @Override
    public void requestExternalEditedState(POIDetailEditableFragment fragment) {
        mExternalEditableState.setInFragment(fragment);
    }

    @Override
    public void onStoring(boolean storing) {
        setSupportProgressBarIndeterminateVisibility(storing);
    }

    public static class ExternalEditableState {

        WheelchairState state = null;

        int nodetype = SupportManager.UNKNOWN_TYPE;

        double latitude = Extra.UNKNOWN;

        double longitude = Extra.UNKNOWN;

        void saveState(Bundle bundle) {
            if (state != null) {
                bundle.putInt(Extra.WHEELCHAIR_STATE, state.getId());
            }
            bundle.putInt(Extra.NODETYPE, nodetype);
            bundle.putDouble(Extra.LATITUDE, latitude);
            bundle.putDouble(Extra.LONGITUDE, longitude);
        }

        void restoreState(Bundle bundle) {
            int stateId = bundle.getInt(Extra.WHEELCHAIR_STATE, Extra.UNKNOWN);
            if (stateId != Extra.UNKNOWN) {
                state = WheelchairState.valueOf(stateId);
            }

            nodetype = bundle.getInt(Extra.NODETYPE,
                    SupportManager.UNKNOWN_TYPE);
            latitude = bundle.getDouble(Extra.LATITUDE, Extra.UNKNOWN);
            longitude = bundle.getDouble(Extra.LONGITUDE, Extra.UNKNOWN);
        }

        void clear() {
            state = null;
            nodetype = SupportManager.UNKNOWN_TYPE;
            latitude = Extra.UNKNOWN;
            longitude = Extra.UNKNOWN;
        }

        void setInFragment(POIDetailEditableFragment fragment) {
            fragment.setWheelchairState(state);
            fragment.setNodetype(nodetype);
            fragment.setGeolocation(latitude, longitude);
        }
    }

}
