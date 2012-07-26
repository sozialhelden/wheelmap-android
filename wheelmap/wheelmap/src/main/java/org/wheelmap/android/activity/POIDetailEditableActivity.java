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

import org.wheelmap.android.fragment.EditPositionFragment;
import org.wheelmap.android.fragment.EditPositionFragment.OnEditPositionListener;
import org.wheelmap.android.fragment.LoginDialogFragment.OnLoginDialogListener;
import org.wheelmap.android.fragment.NodetypeSelectFragment;
import org.wheelmap.android.fragment.NodetypeSelectFragment.OnNodetypeSelectListener;
import org.wheelmap.android.fragment.POIDetailEditableFragment;
import org.wheelmap.android.fragment.POIDetailEditableFragment.OnPOIDetailEditableListener;
import org.wheelmap.android.fragment.POIDetailFragment;
import org.wheelmap.android.model.Extra;

import wheelmap.org.WheelchairState;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;

public class POIDetailEditableActivity extends MapsforgeMapActivity implements
		OnPOIDetailEditableListener, OnLoginDialogListener,
		OnEditPositionListener, OnNodetypeSelectListener,
		OnBackStackChangedListener {
	private final static String TAG = POIDetailEditableActivity.class
			.getSimpleName();

	private static final int SELECT_WHEELCHAIRSTATE = 0;

	private Fragment mFragment;
	private ExternalEditableState mExternalEditableState;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setExternalEditableState(savedInstanceState);

		getSupportActionBar().setDisplayShowTitleEnabled(false);

		FragmentManager fm = getSupportFragmentManager();
		fm.addOnBackStackChangedListener(this);

		mFragment = fm.findFragmentById(android.R.id.content);
		if (mFragment != null) {
			return;
		}

		Long poiID = getIntent().getLongExtra(Extra.POI_ID, Extra.ID_UNKNOWN);
		if (poiID != Extra.ID_UNKNOWN) {
			mFragment = POIDetailEditableFragment.newInstance(poiID);
		}

		fm.beginTransaction()
				.add(android.R.id.content, mFragment, POIDetailFragment.TAG)
				.commit();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private void setExternalEditableState(Bundle state) {
		mExternalEditableState = new ExternalEditableState();
		if (state != null)
			mExternalEditableState.restoreState(state);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mExternalEditableState.saveState(outState);
	}

	@Override
	public void onEditWheelchairState(WheelchairState state) {
		Intent intent = new Intent(POIDetailEditableActivity.this,
				WheelchairStateActivity.class);
		intent.putExtra(Extra.WHEELCHAIR_STATE, state.getId());
		startActivityForResult(intent, SELECT_WHEELCHAIRSTATE);
	}

	/**
	 * This method is called when the sending activity has finished, with the
	 * result it supplied.
	 * 
	 * @param requestCode
	 *            The original request code as given to startActivity().
	 * @param resultCode
	 *            From sending activity as per setResult().
	 * @param data
	 *            From sending activity as per setResult().
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// You can use the requestCode to select between multiple child
		// activities you may have started. Here there is only one thing
		// we launch.
		if (requestCode == SELECT_WHEELCHAIRSTATE) {
			// This is a standard resultCode that is sent back if the
			// activity doesn't supply an explicit result. It will also
			// be returned if the activity failed to launch.
			if (resultCode == RESULT_OK) {
				// newly selected wheelchair state as action data
				if (data != null) {
					WheelchairState state = WheelchairState
							.valueOf(data.getIntExtra(Extra.WHEELCHAIR_STATE,
									Extra.UNKNOWN));
					mExternalEditableState.state = state;
				}
			}
		}
	}

	@Override
	public void onEditSave() {
		finish();
	}

	@Override
	public void onEditGeolocation(int latitude, int longitude) {
		mFragment = EditPositionFragment.newInstance(latitude, longitude);

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(android.R.id.content, mFragment, EditPositionFragment.TAG);
		ft.addToBackStack(null);
		ft.commit();
	}

	@Override
	public void onEditPosition(int latitude, int longitude) {
		mExternalEditableState.latitude = latitude;
		mExternalEditableState.longitude = longitude;
		getSupportFragmentManager().popBackStack();
	}

	@Override
	public void onEditNodetype(int nodetype) {
		mFragment = NodetypeSelectFragment.newInstance(nodetype);

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(android.R.id.content, mFragment, NodetypeSelectFragment.TAG);
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
		mFragment = fm.findFragmentById(android.R.id.content);
	}

	public static class ExternalEditableState {
		WheelchairState state = null;
		int nodetype = Extra.UNKNOWN;
		int latitude = Extra.UNKNOWN;
		int longitude = Extra.UNKNOWN;

		void saveState(Bundle bundle) {
			if (state != null)
				bundle.putInt(Extra.WHEELCHAIR_STATE, state.getId());
			bundle.putInt(Extra.NODETYPE, nodetype);
			bundle.putInt(Extra.LATITUDE, latitude);
			bundle.putInt(Extra.LONGITUDE, longitude);
		}

		void restoreState(Bundle bundle) {
			int stateId = bundle.getInt(Extra.WHEELCHAIR_STATE, Extra.UNKNOWN);
			if (stateId != Extra.UNKNOWN)
				state = WheelchairState.valueOf(stateId);

			nodetype = bundle.getInt(Extra.NODETYPE, Extra.UNKNOWN);
			latitude = bundle.getInt(Extra.LATITUDE, Extra.UNKNOWN);
			longitude = bundle.getInt(Extra.LONGITUDE, Extra.UNKNOWN);
		}

		void clear() {
			state = null;
			nodetype = Extra.UNKNOWN;
			latitude = Extra.UNKNOWN;
			longitude = Extra.UNKNOWN;
		}

		void setInFragment(POIDetailEditableFragment fragment) {
			fragment.setWheelchairState(state);
			fragment.setNodetype(nodetype);
			fragment.setGeolocation(latitude, longitude);

			clear();
		}
	}

	@Override
	public void requestExternalEditedState(POIDetailEditableFragment fragment) {
		mExternalEditableState.setInFragment(fragment);
	}

}
