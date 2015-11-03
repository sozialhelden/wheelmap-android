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

import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.fragment.POIDetailEditableFragment;
import org.wheelmap.android.fragment.POIDetailEditableFragment.OnPOIDetailEditableListener;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.WheelchairFilterState;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.UtilsMisc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.view.MenuItem;
import android.view.Window;

import de.akquinet.android.androlog.Log;

public class POIDetailEditableActivity extends MapActivity implements
        OnPOIDetailEditableListener,
        OnBackStackChangedListener {

    private final static String TAG = POIDetailEditableActivity.class.getSimpleName();

    // Definition of the one requestCode we use for receiving resuls.
    static final private int SELECT_WHEELCHAIRSTATE = 0;

    private Fragment mFragment;

    private ExternalEditableState mExternalEditableState;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        if (UtilsMisc.isTablet(getApplicationContext())) {
            UtilsMisc.showAsPopup(this);

        }
        setContentView(R.layout.activity_frame_empty);
        setSupportProgressBarIndeterminateVisibility(false);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_editor);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
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

        int focus = getIntent().getIntExtra("Focus", 0);

        mFragment = POIDetailEditableFragment.newInstance(poiID, focus);
        fm.beginTransaction()
                .add(R.id.content, mFragment,
                        POIDetailEditableFragment.TAG).commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

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
    public void onEditWheelchairState(WheelchairFilterState state) {
        Log.d(TAG, "onWheelchairStateSelect: state = " + state.toString());
        mExternalEditableState.acessState = state;
    }

    @Override
    public void onEditWheelchairToiletState(WheelchairFilterState state) {
        Log.d(TAG, "onWheelchairToiletStateSelect: state = " + state.toString());
        mExternalEditableState.toiletState = state;
    }

    @Override
    public void onEditSave(boolean quit) {
        if(!quit)
            startDialog();
        else
            goBack();
    }

    private void startDialog() {
        WheelmapApp app = (WheelmapApp) this.getApplicationContext();

        if(app.isChangedText()){
            app.setChangedText(false);
            /*AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(getResources().getString(R.string.dialog_close_editable));

            builder.setPositiveButton(R.string.btn_okay, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    finish();
                }
            });
            builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    return;
                }
            });

            AlertDialog dialog = builder.create();  */
            goBack();
        }
        else
            goBack();

    }

    @Override
    public void onEditGeolocation(double latitude, double longitude) {
        mExternalEditableState.latitude = latitude;
        mExternalEditableState.longitude = longitude;
    }

    @Override
    public void onEditNodetype(int nodetype) {

        mExternalEditableState.nodetype = nodetype;
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

        WheelchairFilterState acessState = null;
        WheelchairFilterState toiletState = null;

        int nodetype = SupportManager.UNKNOWN_TYPE;

        double latitude = Extra.UNKNOWN;

        double longitude = Extra.UNKNOWN;

        void saveState(Bundle bundle) {
            if (acessState != null) {
                bundle.putInt(Extra.WHEELCHAIR_STATE, acessState.getId());
            }
            if (toiletState != null) {
                bundle.putInt(Extra.WHEELCHAIR_TOILET_STATE, toiletState.getId());
            }

            bundle.putInt(Extra.NODETYPE, nodetype);
            bundle.putDouble(Extra.LATITUDE, latitude);
            bundle.putDouble(Extra.LONGITUDE, longitude);
        }

        void restoreState(Bundle bundle) {
            int stateId = bundle.getInt(Extra.WHEELCHAIR_STATE, Extra.UNKNOWN);
            if (stateId != Extra.UNKNOWN) {
                acessState = WheelchairFilterState.valueOf(stateId);
            }

            stateId = bundle.getInt(Extra.WHEELCHAIR_TOILET_STATE, Extra.UNKNOWN);
            if (stateId != Extra.UNKNOWN) {
                toiletState = WheelchairFilterState.valueOf(stateId);
            }

            nodetype = bundle.getInt(Extra.NODETYPE,
                    SupportManager.UNKNOWN_TYPE);
            latitude = bundle.getDouble(Extra.LATITUDE, Extra.UNKNOWN);
            longitude = bundle.getDouble(Extra.LONGITUDE, Extra.UNKNOWN);
        }

        void clear() {
            acessState = null;
            toiletState = null;
            nodetype = SupportManager.UNKNOWN_TYPE;
            latitude = Extra.UNKNOWN;
            longitude = Extra.UNKNOWN;
        }

        void setInFragment(POIDetailEditableFragment fragment) {
            fragment.setWheelchairState(acessState);
            fragment.setWheelchairToiletState(toiletState);
            fragment.setNodetype(nodetype);
            fragment.setGeolocation(latitude, longitude);
        }
    }

    public void goBack(){
        WheelmapApp app = (WheelmapApp) this.getApplicationContext();
        app.setChangedText(false);
        finish();
    }


    @Override
    public void onBackPressed() {
        WheelmapApp app = (WheelmapApp) this.getApplicationContext();

        if(app.isChangedText()){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(getResources().getString(R.string.dialog_close_editable));
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.btn_okay, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button

                    goBack();
                }
            });
            builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    return;
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else
            goBack();
    }
}
