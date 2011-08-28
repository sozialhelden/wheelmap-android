package org.wheelmap.android.ui;

import org.wheelmap.android.R;

import wheelmap.org.WheelchairState;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;

public class WheelchairStateActivity extends Activity {

	private RadioButton mEnabled;
	private RadioButton mLimited;
	private RadioButton mDisabled;
	private RadioButton mUnknown;


	private void DeselectAllRadioButtons() {
		mEnabled.setChecked(false);
		mLimited.setChecked(false);
		mDisabled.setChecked(false);
		mUnknown.setChecked(false);
	}

	private void setWheeChairState(WheelchairState newState) {
		DeselectAllRadioButtons();
		switch (newState) {
		case UNKNOWN: 
			mUnknown.setChecked(true);
			break;
		case YES:
			mEnabled.setChecked(true);
			break;
		case LIMITED:
			mLimited.setChecked(true);
			break;
		case NO:
			mDisabled.setChecked(true);
			break;
		default:
			mUnknown.setChecked(true);
			break;
		}

	}

	private WheelchairState getWheeChairState() {
		if (mEnabled.isChecked())
			return WheelchairState.YES;

		if (mLimited.isChecked())
			return WheelchairState.LIMITED;

		if (mDisabled.isChecked())
			return WheelchairState.NO;

		if (mUnknown.isChecked())
			return WheelchairState.UNKNOWN;
		else
			return WheelchairState.UNKNOWN;

	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wheelchair_state);

		mEnabled = (RadioButton) findViewById( R.id.radio_enabled );
		mLimited = (RadioButton) findViewById( R.id.radio_limited );
		mDisabled = (RadioButton) findViewById( R.id.radio_disabled );
		mUnknown = (RadioButton) findViewById( R.id.radio_unknown );

		mEnabled.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				DeselectAllRadioButtons();
				final RadioButton a = (RadioButton)v;
				a.setChecked(true);
			}
		});

		mLimited.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				DeselectAllRadioButtons();
				final RadioButton a = (RadioButton)v;
				a.setChecked(true);
			}
		});

		mDisabled.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				DeselectAllRadioButtons();
				final RadioButton a = (RadioButton)v;
				a.setChecked(true);
			}
		});

		mUnknown.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				DeselectAllRadioButtons();
				final RadioButton a = (RadioButton)v;
				a.setChecked(true);
			}
		});
	}


}
