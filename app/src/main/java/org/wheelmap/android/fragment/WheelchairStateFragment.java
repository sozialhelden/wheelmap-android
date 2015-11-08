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

import org.wheelmap.android.model.WheelchairFilterState;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;

import java.util.HashMap;

public abstract class WheelchairStateFragment extends Fragment implements
        OnClickListener {

    public static final String TAG = WheelchairStateFragment.class
            .getSimpleName();

    protected HashMap<WheelchairFilterState, RadioButton> mRadioButtonsMap = new HashMap<WheelchairFilterState, RadioButton>();

    protected OnWheelchairState mListener;

    public interface OnWheelchairState {

        public void onWheelchairStateSelect(WheelchairFilterState state);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnWheelchairState) {
            mListener = (OnWheelchairState) activity;
        }
    }

    protected abstract void DeselectAllRadioButtons();

    protected abstract void setWheelchairState(WheelchairFilterState newState);

    protected abstract WheelchairFilterState getWheelchairState();

    @Override
    public void onClick(View v) {
        DeselectAllRadioButtons();
        final RadioButton a = (RadioButton) v;
        a.setChecked(true);
    }

    protected void dismiss(){
        if (mListener != null) {
            mListener.onWheelchairStateSelect(getWheelchairState());
        }
    }

}
