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

import org.wheelmap.android.activity.base.BaseActivity;
import org.wheelmap.android.fragment.WheelchairAccessibilityStateFragment;
import org.wheelmap.android.fragment.WheelchairStateFragment.OnWheelchairState;
import org.wheelmap.android.fragment.WheelchairToiletStateFragment;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.WheelchairFilterState;
import org.wheelmap.android.online.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import de.akquinet.android.androlog.Log;

public class WheelchairStateActivity extends BaseActivity implements
        OnWheelchairState {

    private final static String TAG = WheelchairStateActivity.class
            .getSimpleName();

    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_empty);
        Log.d(TAG, "onCreate");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }

        Intent intent = getIntent();
        if(intent.hasExtra(Extra.WHEELCHAIR_STATE)){
            setFragmentForWheelchairAccessState(intent.getIntExtra(Extra.WHEELCHAIR_STATE, Extra.UNKNOWN));
        } else if(intent.hasExtra(Extra.WHEELCHAIR_TOILET_STATE)){
            setFragmentForWheelchairToiletState(intent.getIntExtra(Extra.WHEELCHAIR_TOILET_STATE, Extra.UNKNOWN));
        } else {
            finishByUnknownData();
        }
    }


    private void setFragmentForWheelchairAccessState(int stateId){
        if(stateId == Extra.UNKNOWN){
            finishByUnknownData();
        }

        FragmentManager fm = getSupportFragmentManager();
        mFragment = fm.findFragmentByTag(WheelchairAccessibilityStateFragment.TAG);

        if (mFragment != null) {
            return;
        }

        mFragment = WheelchairAccessibilityStateFragment.newInstance(WheelchairFilterState.valueOf(stateId));
        fm.beginTransaction()
                .add(R.id.content, mFragment, WheelchairAccessibilityStateFragment.TAG).commit();
    }

    private void setFragmentForWheelchairToiletState(int stateId){
        if(stateId == Extra.UNKNOWN){
            finishByUnknownData();
        }

        FragmentManager fm = getSupportFragmentManager();
        mFragment = fm.findFragmentByTag(WheelchairToiletStateFragment.TAG);

        if (mFragment != null) {
            return;
        }

        mFragment = WheelchairToiletStateFragment.newInstance(WheelchairFilterState.valueOf(stateId));
        fm.beginTransaction()
                .add(R.id.content, mFragment, WheelchairToiletStateFragment.TAG).commit();

    }

    private void finishByUnknownData(){
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public void onWheelchairStateSelect(WheelchairFilterState state) {
        Intent intent = new Intent();
        intent.putExtra(mFragment.getTag(), state.getId());
        setResult(RESULT_OK, intent);
        finish();
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

}
