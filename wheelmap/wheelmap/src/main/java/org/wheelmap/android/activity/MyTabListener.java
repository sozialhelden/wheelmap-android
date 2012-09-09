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

import org.wheelmap.android.fragment.OnExecuteBundle;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.akquinet.android.androlog.Log;

public class MyTabListener<T extends Fragment> implements TabListener {
	private final static String TAG = MyTabListener.class.getSimpleName();
	private Fragment mFragment;
	private final SherlockFragmentActivity mActivity;
	private final TabHolder mTag;
	private final Class<T> mClass;
	private final OnStateListener listener;

	/**
	 * Constructor used each time a new tab is created.
	 * 
	 * @param activity
	 *            The host Activity, used to instantiate the fragment
	 * @param tag
	 *            The identifier tag for the fragment
	 * @param clz
	 *            The fragment's Class, used to instantiate the fragment
	 */
	public MyTabListener(SherlockFragmentActivity activity, TabHolder tag,
			Class<T> clz) {
		mActivity = activity;
		mTag = tag;
		mClass = clz;

		if (activity instanceof OnStateListener)
			listener = (OnStateListener) activity;
		else
			listener = null;

		FragmentManager fm = mActivity.getSupportFragmentManager();
		mFragment = fm.findFragmentByTag(mTag.name);
	}

	/* The following are each of the ActionBar.TabListener callbacks */

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		Log.d(TAG, "onTabSelected tag = " + mTag.name);

		if (mFragment == null) {
			mFragment = SherlockFragment.instantiate(mActivity,
					mClass.getName(), new Bundle());
			FragmentManager fm = mActivity.getSupportFragmentManager();
			fm.beginTransaction()
					.replace(android.R.id.content, mFragment, mTag.name)
					.commit();

		} else {
			Log.d(TAG, "Fragment mFragment = " + mFragment.toString());
		}

		Bundle executeBundle = mTag.getExecuteBundle();
		if (executeBundle != null && mFragment instanceof OnExecuteBundle) {
			((OnExecuteBundle) mFragment).executeBundle(executeBundle);
		}

		if (listener != null)
			listener.onStateChange(mTag.name);
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		Log.d(TAG, "onTabUnselected: tag = " + mTag.name);
		if (mFragment != null) {
			Log.d(TAG, "removing tab");
			FragmentManager fm = mActivity.getSupportFragmentManager();
			fm.beginTransaction().remove(mFragment).commit();

			Fragment workerFragment = fm.findFragmentByTag(mTag.workerName);
			if (workerFragment != null)
				fm.beginTransaction().remove(workerFragment).commit();

			mFragment = null;
		}
	}

	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		Log.d(TAG, "onTabReselected: tag = " + mTag.name);

		Bundle executeBundle = mTag.getExecuteBundle();
		if (executeBundle != null && mFragment instanceof OnExecuteBundle) {
			((OnExecuteBundle) mFragment).executeBundle(executeBundle);
		}
	}

	public interface OnStateListener {
		public void onStateChange(String tag);
	}

	public static class TabHolder {
		public String name;
		public String workerName;
		private Bundle bundle;

		public TabHolder(String name, String workerName) {
			this.name = name;
			this.workerName = workerName;
		}

		public void setExecuteBundle(Bundle extras) {
			bundle = extras;
		}

		public Bundle getExecuteBundle() {
			Bundle newBundle = bundle;
			bundle = null;
			return newBundle;
		}
	}

}
