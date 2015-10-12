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
import org.wheelmap.android.fragment.POIsOsmdroidFragment;
import org.wheelmap.android.online.R;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.akquinet.android.androlog.Log;

public class MyTabListener implements TabListener {

    private final static String TAG = MyTabListener.class.getSimpleName();

    public final static int TAB_LIST = 0;

    public final static int TAB_MAP = 1;

    private final static Map<String, TabHolder> sTagToTabHolder;

    private final Activity mActivity;

    private OnStateListener mListener;

    private TabHolder currentTab;

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

    public TabHolder getCurrentTab(){
        return currentTab;
    }

    public TabHolder getTabHolder( String tag ) {
        return sTagToTabHolder.get(tag);
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        TabHolder holder = sTagToTabHolder.get(tab.getTag());
        currentTab = holder;
        Log.d(TAG, "onTabSelected tag = " + holder.tag);

        FragmentManager fm = mActivity.getSupportFragmentManager();
        if (holder.fragment == null) {
            Log.d( TAG, "Instantiating holder fragment");

            if(holder.clazz == POIsListFragment.class){

            }

            holder.fragment = Fragment.instantiate( holder.clazz, new Bundle());
        }
        Log.d(TAG, "Fragment holder.fragment = " + holder.fragment.toString());
        FragmentTransaction t = fm.beginTransaction();
        t.replace(R.id.content, holder.fragment, holder.tag);
        t.commit();

        if (holder.hasExecuteBundle() && holder.fragment instanceof OnExecuteBundle) {
            Log.d(TAG, "onTabSelected: executing bundle");
            ((OnExecuteBundle) holder.fragment).executeBundle(holder.getExecuteBundle());
        }

        if (mListener != null) {
            mListener.onStateChange(holder.tag);
        }
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        TabHolder holder = sTagToTabHolder.get(tab.getTag());
        Log.d(TAG, "onTabUnselected: tag = " + holder.tag);
        if (holder.fragment == null) {
            return;
        }

        Log.d(TAG, "removing tab");
        FragmentManager fm = mActivity.getSupportFragmentManager();
        FragmentTransaction t = fm.beginTransaction();
        if ( holder.fragment != null) {
            ft.remove(holder.fragment);
        }
        Fragment workerFragment = (Fragment) fm.findFragmentByTag(holder.workerTag);
        if ( workerFragment != null) {
            t.remove(workerFragment);
        }
        t.commit();
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        TabHolder holder = sTagToTabHolder.get(tab.getTag());
        Log.d(TAG, "onTabReselected: tag = " + holder.tag);

        if (holder.hasExecuteBundle() && holder.fragment instanceof OnExecuteBundle) {
            Log.d(TAG, "onTabReselected: executing bundle");
            ((OnExecuteBundle) holder.fragment).executeBundle(holder.getExecuteBundle());
        }
    }

    public static TabHolder getHolder(String tag) {
        return sTagToTabHolder.get(tag);
    }

    public interface OnStateListener {

        public void onStateChange(String tag);
    }

    public static class TabHolder {

        public final Class<? extends Fragment> clazz;

        public Fragment fragment;

        public final int position;

        public final String tag;

        public final String workerTag;

        private Bundle bundle;

        private boolean active;

        public TabHolder(Class<? extends Fragment> clazz, int position, String tag, String workerTag, boolean active) {
            this.clazz = clazz;
            this.tag = tag;
            this.workerTag = workerTag;
            this.position = position;
            this.active = active;
        }

        public void setActive( boolean active ) {
            this.active = active;
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

        public static TabHolder findActiveHolderByTab(int selectedTab) {
            for( Entry<String, TabHolder> tabEntry: sTagToTabHolder.entrySet()) {
                TabHolder holder = tabEntry.getValue();
                if ( holder.active && holder.position == selectedTab) {
                    return holder;
                }
            }

            return null;
        }
    }

    static {
        sTagToTabHolder = new HashMap<String, TabHolder>();
        sTagToTabHolder.put(POIsListFragment.TAG,
                new TabHolder(POIsListFragment.class, 0, POIsListFragment.TAG, POIsListWorkerFragment.TAG, true));
        sTagToTabHolder.put(POIsOsmdroidFragment.TAG,
                new TabHolder(POIsOsmdroidFragment.class, 1, POIsOsmdroidFragment.TAG, POIsMapWorkerFragment.TAG, true));
    }
}
