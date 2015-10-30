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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;

import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.WheelchairFilterState;
import org.wheelmap.android.online.R;

public class WheelchairAccessStateFragment extends WheelchairStateFragment {

    public static final String TAG = WheelchairAccessStateFragment.class
            .getSimpleName();

    public static WheelchairAccessStateFragment newInstance(WheelchairFilterState state) {
        Bundle b = new Bundle();
        b.putInt(Extra.WHEELCHAIR_STATE, state.getId());
        WheelchairAccessStateFragment f = new WheelchairAccessStateFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wheelchair_access_state,
                container, false);

        mRadioButtonsMap.put(WheelchairFilterState.YES,
                (RadioButton) v.findViewById(R.id.radio_enabled));
        mRadioButtonsMap.put(WheelchairFilterState.LIMITED,
                (RadioButton) v.findViewById(R.id.radio_limited));
        mRadioButtonsMap.put(WheelchairFilterState.NO,
                (RadioButton) v.findViewById(R.id.radio_disabled));
        mRadioButtonsMap.put(WheelchairFilterState.UNKNOWN,
                (RadioButton) v.findViewById(R.id.radio_unknown));

        for (WheelchairFilterState state : mRadioButtonsMap.keySet()) {
            mRadioButtonsMap.get(state).setOnClickListener(this);
        }


        v.findViewById(R.id.detail_save).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        /*
        v.findViewById(R.id.no).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });     */

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int newStateInt = getArguments().getInt(Extra.WHEELCHAIR_STATE,
                Extra.UNKNOWN);
        WheelchairFilterState newState = WheelchairFilterState.valueOf(newStateInt);
        setWheelchairState(newState);

    }

    protected void DeselectAllRadioButtons() {
        for (WheelchairFilterState state : mRadioButtonsMap.keySet()) {
            mRadioButtonsMap.get(state).setChecked(false);
        }
    }

    protected void setWheelchairState(WheelchairFilterState newState) {
        DeselectAllRadioButtons();
        mRadioButtonsMap.get(newState).setChecked(true);
    }

    protected WheelchairFilterState getWheelchairState() {
        for (WheelchairFilterState state : mRadioButtonsMap.keySet()) {
            if (mRadioButtonsMap.get(state).isChecked()) {
                return state;
            }
        }
        return WheelchairFilterState.UNKNOWN;
    }
}
