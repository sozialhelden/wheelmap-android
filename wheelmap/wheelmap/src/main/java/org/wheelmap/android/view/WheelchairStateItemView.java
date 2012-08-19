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
package org.wheelmap.android.view;

import org.wheelmap.android.online.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class WheelchairStateItemView extends FrameLayout {
	ImageView mWheelStateIcon;
	TextView mWheelStateText;
	CheckBox mWheelStateCheckBox;

	public WheelchairStateItemView(Context context) {
		super(context);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.item_settings_wheelchair, this, true);

		mWheelStateIcon = (ImageView) findViewById(R.id.image);
		mWheelStateText = (TextView) findViewById(R.id.text);
		mWheelStateCheckBox = (CheckBox) findViewById(R.id.checkbox);

		mWheelStateCheckBox.setClickable(false);
		mWheelStateCheckBox.setFocusable(false);
	}

	public void setIcon(int resourceId) {
		mWheelStateIcon.setImageResource(resourceId);
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