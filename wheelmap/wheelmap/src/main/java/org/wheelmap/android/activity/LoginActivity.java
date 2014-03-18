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

import com.google.inject.Inject;

import com.actionbarsherlock.view.MenuItem;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.FrameLayout;
import org.holoeverywhere.widget.Toast;
import org.wheelmap.android.fragment.LoginFragment;
import org.wheelmap.android.fragment.LogoutFragment;
import org.wheelmap.android.fragment.WheelchairStateFragment;
import org.wheelmap.android.fragment.WheelchairStateFragment.OnWheelchairState;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.WheelchairState;
import org.wheelmap.android.modules.ICredentials;
import org.wheelmap.android.modules.UserCredentials;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.UtilsMisc;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import de.akquinet.android.androlog.Log;

@Activity.Addons(Activity.ADDON_SHERLOCK)
public class LoginActivity extends Activity {

    private final static String TAG = WheelchairStateActivity.class
            .getSimpleName();


    private ICredentials mCredentials;

    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCredentials = new UserCredentials(getApplicationContext());

        if (UtilsMisc.isTablet(getApplicationContext())) {
            UtilsMisc.showAsPopup(this);
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_frame_empty);

        Log.d(TAG, "onCreate");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FragmentManager fm = getSupportFragmentManager();

        if(!mCredentials.isLoggedIn()){
            mFragment = new LoginFragment();
            setTitle(R.string.login_activity_title);
        }else{
            setTitle(R.string.logout_activity_title);
            mFragment = new LogoutFragment();
        }

        fm.beginTransaction()
                .add(R.id.content, mFragment,
                        LoginFragment.TAG).commit();

        if(UtilsMisc.isTablet(getApplicationContext())){
            View v = findViewById(R.id.content);
            while(v != null && v instanceof ViewGroup){
                v.setBackgroundColor(Color.TRANSPARENT);
                if(v.getParent() instanceof View){
                    v = (View) v.getParent();
                }else{
                    break;
                }
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
