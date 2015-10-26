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

import org.wheelmap.android.online.R;

import android.os.Bundle;
import android.view.MenuItem;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import de.akquinet.android.androlog.Log;

public class WrapperActivity extends MapActivity{

    private final static String TAG = WrapperActivity.class
            .getSimpleName();

    public static final String EXTRA_FRAGMENT_CLASS_NAME = "EXTRA_FRAGMENT_CLASS_NAME";

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

        FragmentManager fm = getSupportFragmentManager();

        String className = getIntent().getExtras().getString(EXTRA_FRAGMENT_CLASS_NAME);
        try{
             mFragment = (Fragment) Class.forName(className).newInstance();

             mFragment = (Fragment) fm
                    .findFragmentByTag(WrapperActivity.TAG);
            if (mFragment != null) {
                return;
            }
            mFragment = (Fragment) Class.forName(className).newInstance();
            mFragment.setArguments(getIntent().getExtras());

            fm.beginTransaction()
                .add(R.id.content, mFragment,
                        WrapperActivity.TAG).commit();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
