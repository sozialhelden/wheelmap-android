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
		inflater.inflate(R.layout.settings_item_wheelchair, this, true);

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