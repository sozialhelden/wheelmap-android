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
package org.wheelmap.android.model;

import java.util.ArrayList;
import java.util.List;

import org.wheelmap.android.online.R;

import wheelmap.org.WheelchairState;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class WheelchairStateAdapter extends BaseAdapter {
	private Context mContext;
	private List<WheelchairStateItem> mItems;
	private SharedPreferences mPrefs;

	public static class WheelchairStateItem {
		public WheelchairStateItem(WheelchairState state, String text,
				Drawable icon, String prefsKey, int color) {
			this.state = state;
			this.text = text;
			this.icon = icon;
			this.prefsKey = prefsKey;
			this.color = color;
		}

		public WheelchairState state;
		public String text;
		public Drawable icon;
		public String prefsKey;
		public int color;
	}

	public WheelchairStateAdapter(Context context) {
		super();
		mContext = context;
		mItems = new ArrayList<WheelchairStateItem>();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		WheelchairStateItem stateItem;
		stateItem = new WheelchairStateItem(
				WheelchairState.YES,
				mContext.getString(R.string.settings_wheelchair_full_accessible),
				mContext.getResources().getDrawable(
						R.drawable.wheelchair_state_enabled),
				QueriesBuilderHelper.PREF_KEY_WHEELCHAIR_STATE_FULL, mContext
						.getResources().getColor(R.color.wheel_enabled));
		mItems.add(stateItem);
		stateItem = new WheelchairStateItem(
				WheelchairState.LIMITED,
				mContext.getString(R.string.settings_wheelchair_limited_accessible),
				mContext.getResources().getDrawable(
						R.drawable.wheelchair_state_limited),
				QueriesBuilderHelper.PREF_KEY_WHEELCHAIR_STATE_LIMITED,
				mContext.getResources().getColor(R.color.wheel_limited));
		mItems.add(stateItem);
		stateItem = new WheelchairStateItem(WheelchairState.NO,
				mContext.getString(R.string.settings_wheelchair_no_accessible),
				mContext.getResources().getDrawable(
						R.drawable.wheelchair_state_disabled),
				QueriesBuilderHelper.PREF_KEY_WHEELCHAIR_STATE_NO, mContext
						.getResources().getColor(R.color.wheel_disabled));
		mItems.add(stateItem);
		stateItem = new WheelchairStateItem(
				WheelchairState.UNKNOWN,
				mContext.getString(R.string.settings_wheelchair_unknown_accessible),
				mContext.getResources().getDrawable(
						R.drawable.wheelchair_state_unknown),
				QueriesBuilderHelper.PREF_KEY_WHEELCHAIR_STATE_UNKNOWN,
				mContext.getResources().getColor(R.color.wheel_unknown));
		mItems.add(stateItem);

	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		WheelchairStateItemView itemView;
		if (convertView != null)
			itemView = (WheelchairStateItemView) convertView;
		else
			itemView = new WheelchairStateItemView(mContext);

		WheelchairStateItem item = mItems.get(position);
		itemView.setIcon(item.icon);
		itemView.setText(item.text);
		itemView.setTextColor(item.color);

		itemView.setCheckboxChecked(mPrefs.getBoolean(item.prefsKey, true));
		return itemView;

	}

	private static class WheelchairStateItemView extends FrameLayout {
		ImageView mWheelStateIcon;
		TextView mWheelStateText;
		CheckBox mWheelStateCheckBox;

		public WheelchairStateItemView(Context context) {
			super(context);
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			inflater.inflate(R.layout.settings_wheelstate_select_list_item, this, true);

			mWheelStateIcon = (ImageView) findViewById(R.id.list_item_wheelstate_icon);
			mWheelStateText = (TextView) findViewById(R.id.list_item_wheelstate_text);
			mWheelStateCheckBox = (CheckBox) findViewById(R.id.list_item_wheelstate_checkbox);

			mWheelStateCheckBox.setClickable(false);
			mWheelStateCheckBox.setFocusable(false);
		}

		public void setIcon(Drawable icon) {
			mWheelStateIcon.setImageDrawable(icon);
		}

		public void setText(String text) {
			mWheelStateText.setText(text);
		}

		public void setTextColor(int color) {
			mWheelStateText.setTextColor(color);
		}

		public void setCheckboxChecked(boolean checked) {
			mWheelStateCheckBox.setChecked(checked);
		}
	}
}
