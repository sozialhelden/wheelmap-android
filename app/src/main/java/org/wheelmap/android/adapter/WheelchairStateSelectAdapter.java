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
package org.wheelmap.android.adapter;

import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.manager.SupportManager.WheelchairAttributes;
import org.wheelmap.android.model.WheelchairFilterState;
import org.wheelmap.android.view.WheelchairFilterStateItemView;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WheelchairStateSelectAdapter extends BaseAdapter {

    protected Context mContext;

    protected List<SupportManager.AccessFilterAttributes> mItems;

    protected SharedPreferences mPrefs;

    public WheelchairStateSelectAdapter(Context context) {
        super();
        mContext = context;

        mItems = new ArrayList();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        Map<WheelchairFilterState, WheelchairAttributes> attributes = SupportManager.wsAttributes;

        mItems.add(attributes.get(WheelchairFilterState.YES));
        mItems.add(attributes.get(WheelchairFilterState.LIMITED));
        mItems.add(attributes.get(WheelchairFilterState.NO));
        mItems.add(attributes.get(WheelchairFilterState.UNKNOWN));
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public SupportManager.AccessFilterAttributes getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public WheelchairFilterStateItemView getView(int position, View convertView, ViewGroup parent) {
        WheelchairFilterStateItemView itemView;
        if (convertView != null) {
            itemView = (WheelchairFilterStateItemView) convertView;
        } else {
            itemView = new WheelchairFilterStateItemView(mContext);
        }

        SupportManager.AccessFilterAttributes attributes = mItems.get(position);

        itemView.setIcon(attributes.drawableId);
        itemView.setText(mContext.getString(attributes.settingsStringId));
        itemView.setTextColor(mContext.getResources().getColor(
                attributes.colorId));
        itemView.setCheckboxChecked(mPrefs
                .getBoolean(attributes.prefsKey, true));

        return itemView;

    }
}
