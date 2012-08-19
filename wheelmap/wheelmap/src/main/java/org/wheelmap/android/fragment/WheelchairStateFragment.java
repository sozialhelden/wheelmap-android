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
package org.wheelmap.android.fragment;

import java.util.HashMap;

import org.wheelmap.android.model.Extra;
import org.wheelmap.android.online.R;

import wheelmap.org.WheelchairState;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.actionbarsherlock.app.SherlockFragment;

public class WheelchairStateFragment extends SherlockFragment implements
		OnClickListener {
	public static final String TAG = WheelchairStateFragment.class
			.getSimpleName();

	private HashMap<WheelchairState, RadioButton> mRadioButtonsMap = new HashMap<WheelchairState, RadioButton>();
	private OnWheelchairState mListener;

	public interface OnWheelchairState {
		public void onWheelchairStateSelect(WheelchairState state);
	}

	public static WheelchairStateFragment newInstance(WheelchairState state) {
		Bundle b = new Bundle();
		b.putInt(Extra.WHEELCHAIR_STATE, state.getId());
		WheelchairStateFragment f = new WheelchairStateFragment();
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof OnWheelchairState)
			mListener = (OnWheelchairState) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_wheelchair_state,
				container, false);

		mRadioButtonsMap.put(WheelchairState.YES,
				(RadioButton) v.findViewById(R.id.radio_enabled));
		mRadioButtonsMap.put(WheelchairState.LIMITED,
				(RadioButton) v.findViewById(R.id.radio_limited));
		mRadioButtonsMap.put(WheelchairState.NO,
				(RadioButton) v.findViewById(R.id.radio_disabled));
		mRadioButtonsMap.put(WheelchairState.UNKNOWN,
				(RadioButton) v.findViewById(R.id.radio_unknown));

		for (WheelchairState state : mRadioButtonsMap.keySet()) {
			mRadioButtonsMap.get(state).setOnClickListener(this);
		}

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		int newStateInt = getArguments().getInt(Extra.WHEELCHAIR_STATE,
				Extra.UNKNOWN);
		WheelchairState newState = WheelchairState.valueOf(newStateInt);
		setWheelchairState(newState);

	}

	private void DeselectAllRadioButtons() {
		for (WheelchairState state : mRadioButtonsMap.keySet()) {
			mRadioButtonsMap.get(state).setChecked(false);
		}
	}

	private void setWheelchairState(WheelchairState newState) {
		DeselectAllRadioButtons();
		mRadioButtonsMap.get(newState).setChecked(true);
	}

	private WheelchairState getWheelchairState() {
		for (WheelchairState state : mRadioButtonsMap.keySet()) {
			if (mRadioButtonsMap.get(state).isChecked())
				return state;
		}
		return WheelchairState.UNKNOWN;
	}

	@Override
	public void onClick(View v) {
		DeselectAllRadioButtons();
		final RadioButton a = (RadioButton) v;
		a.setChecked(true);

		if (mListener != null)
			mListener.onWheelchairStateSelect(getWheelchairState());
	}

}
