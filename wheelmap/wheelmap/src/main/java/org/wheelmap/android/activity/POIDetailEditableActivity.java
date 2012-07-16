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
import org.wheelmap.android.fragment.WheelchairStateFragment;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.online.R;

import wheelmap.org.WheelchairState;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class POIDetailEditableActivity extends MapsforgeMapActivity implements
		OnPOIDetailEditableListener, OnLoginDialogListener,
		OnEditPositionListener, OnNodetypeSelectListener {
	private final static String TAG = POIDetailEditableActivity.class
			.getSimpleName();

	// Definition of the one requestCode we use for receiving resuls.
	private static final int SELECT_WHEELCHAIRSTATE = 0;

	private Long poiID;
	private POIDetailEditableFragment mFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment_singleframe);
		poiID = getIntent().getLongExtra(Wheelmap.POIs.EXTRAS_POI_ID, -1);

		getSupportActionBar().setDisplayShowTitleEnabled(false);

		FragmentManager fm = getSupportFragmentManager();
		mFragment = (POIDetailEditableFragment) fm
				.findFragmentByTag(POIDetailEditableFragment.TAG);
		if (mFragment != null) {
			return;
		}

		if (poiID != -1) {
			mFragment = POIDetailEditableFragment.newInstance(poiID);
		}

		fm.beginTransaction().add(R.id.frame, mFragment, POIDetailFragment.TAG)
				.commit();

	}

	@Override
	public void onPause() {
		super.onPause();
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
					WheelchairState newState = WheelchairState.valueOf(Integer
							.parseInt(data.getAction()));
					mFragment.setWheelchairState(newState);

				}
			}
		}
	}

	@Override
	public void onClose() {
		finish();
	}

	@Override
	public void onEditWheelchairState(WheelchairState state) {
		Intent intent = new Intent(POIDetailEditableActivity.this,
				WheelchairStateActivity.class);
		intent.putExtra(WheelchairStateFragment.EXTRA_WHEELCHAIR_STATE,
				state.getId());
		startActivityForResult(intent, SELECT_WHEELCHAIRSTATE);
	}

	@Override
	public void onEditGeolocation(int latitude, int longitude) {
		Fragment f = EditPositionFragment.newInstance(latitude, longitude);

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.frame, f, EditPositionFragment.TAG);
		ft.addToBackStack(null);
		ft.commit();
	}

	@Override
	public void onEditPosition(int latitude, int longitude) {
		mFragment.setGeolocation(latitude, longitude);
		getSupportFragmentManager().popBackStack();
	}

	@Override
	public void onEditNodetype(int nodetype) {
		Fragment f = NodetypeSelectFragment.newInstance(nodetype);

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.frame, f, NodetypeSelectFragment.TAG);
		ft.addToBackStack(null);
		ft.commit();
	}

	@Override
	public void onSelect(int nodetype) {
		mFragment.setNodetype(nodetype);
		getSupportFragmentManager().popBackStack();
	}

	@Override
	public void onLoginSuccessful() {

	}

	@Override
	public void onLoginCancelled() {
		finish();
	}

}
