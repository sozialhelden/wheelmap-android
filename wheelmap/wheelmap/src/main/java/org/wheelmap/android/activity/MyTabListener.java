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

import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.wheelmap.android.fragment.OnExecuteBundle;
import org.wheelmap.android.fragment.POIsListFragment;
import org.wheelmap.android.fragment.POIsListWorkerFragment;
import org.wheelmap.android.fragment.POIsMapWorkerFragment;
import org.wheelmap.android.fragment.POIsMapsforgeFragment;
import org.wheelmap.android.fragment.POIsOsmdroidFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.ArrayList;

import de.akquinet.android.androlog.Log;

public class MyTabListener implements TabListener {

    private final static String TAG = MyTabListener.class.getSimpleName();

    public final static int TAB_LIST = 0;

    public final static int TAB_MAP = 1;

    private final static ArrayList<TabHolder> sIndexToTab;

    private final Activity mActivity;

    private Fragment mFragment;

    private OnStateListener mListener;

    /**
     * Constructor used each time a new tab is created.
     *
     * @param activity The host Activity, used to instantiate the fragment
     */
    public MyTabListener(Activity activity) {
        mActivity = activity;

        if (activity instanceof OnStateListener) {
            mListener = (OnStateListener) activity;
        }
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        TabHolder holder = sIndexToTab.get(tab.getPosition());
        Log.d(TAG, "onTabSelected tag = " + holder.tag);

        if (mFragment == null) {
            mFragment = Fragment.instantiate(holder.clazz, new Bundle());
            ft.replace(android.R.id.content, mFragment, holder.tag).commit();
        } else {
            Log.d(TAG, "Fragment mFragment = " + mFragment.toString());
        }

        if (holder.hasExecuteBundle() && mFragment instanceof OnExecuteBundle) {
            Log.d(TAG, "onTabSelected: executing bundle");
            ((OnExecuteBundle) mFragment).executeBundle(holder.getExecuteBundle());
        }

        if (mListener != null) {
            mListener.onStateChange(holder.tag);
        }
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        TabHolder holder = sIndexToTab.get(tab.getPosition());
        Log.d(TAG, "onTabUnselected: tag = " + holder.tag);
        if (mFragment == null) {
            return;
        }

        Log.d(TAG, "removing tab");
        ft.remove(mFragment);

        FragmentManager fm = mActivity.getSupportFragmentManager();
        Fragment workerFragment = (Fragment) fm.findFragmentByTag(holder.workerTag);
        if (workerFragment != null) {
            ft.remove(workerFragment);
        }

        ft.commit();
        mFragment = null;
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        TabHolder holder = sIndexToTab.get(tab.getPosition());
        Log.d(TAG, "onTabReselected: tag = " + holder.tag);

        if (holder.hasExecuteBundle() && mFragment instanceof OnExecuteBundle) {
            Log.d(TAG, "onTabReselected: executing bundle");
            ((OnExecuteBundle) mFragment).executeBundle(holder.getExecuteBundle());
        }
    }

    public TabHolder getHolder(int index) {
        return sIndexToTab.get(index);
    }

    public interface OnStateListener {

        public void onStateChange(String tag);
    }

    public static class TabHolder {

        public Class<? extends Fragment> clazz;

        public String tag;

        public String workerTag;

        private Bundle bundle;

        public TabHolder(Class<? extends Fragment> clazz, String tag, String workerTag) {
            this.clazz = clazz;
            this.tag = tag;
            this.workerTag = workerTag;
        }

        public Bundle getExecuteBundle() {
            Bundle newBundle = bundle;
            bundle = null;
            return newBundle;
        }

        public void setExecuteBundle(Bundle extras) {
            bundle = extras;
        }

        public boolean hasExecuteBundle() {
            return bundle != null;
        }
    }

    static {
        sIndexToTab = new ArrayList<TabHolder>();
        sIndexToTab.add(new TabHolder(POIsListFragment.class, POIsListFragment.TAG,
                POIsListWorkerFragment.TAG));
        sIndexToTab.add(new TabHolder(POIsMapsforgeFragment.class, POIsMapsforgeFragment.TAG,
                POIsMapWorkerFragment.TAG));
        sIndexToTab.add(new TabHolder(POIsOsmdroidFragment.class, POIsOsmdroidFragment.TAG,
                POIsMapWorkerFragment.TAG));
    }
}
