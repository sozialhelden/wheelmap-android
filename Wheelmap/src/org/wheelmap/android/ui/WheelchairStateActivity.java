package org.wheelmap.android.ui;

import java.util.HashMap;

import org.wheelmap.android.R;

import wheelmap.org.WheelchairState;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;

public class WheelchairStateActivity extends Activity {

        private HashMap<WheelchairState, RadioButton> mRadioButtonsMap = new HashMap<WheelchairState, RadioButton>();


	private void DeselectAllRadioButtons() {
		for( WheelchairState state: mRadioButtonsMap.keySet() ) {
                	mRadioButtonsMap.get(state).setChecked(false);
		}	
	}

	private void setWheeChairState(WheelchairState newState) {
		DeselectAllRadioButtons();
		mRadioButtonsMap.get(newState).setChecked(true);
	}

	private WheelchairState getWheeChairState() {
		for( WheelchairState state: mRadioButtonsMap.keySet() ) {
                	if (mRadioButtonsMap.get(state).isChecked())
                		return state;
		}	
		return WheelchairState.UNKNOWN;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wheelchair_state);

        // fill hashmap with radio buttons
		mRadioButtonsMap.put(WheelchairState.YES, (RadioButton) findViewById( R.id.radio_enabled ));
		mRadioButtonsMap.put(WheelchairState.LIMITED, (RadioButton) findViewById( R.id.radio_limited ));
		mRadioButtonsMap.put(WheelchairState.NO, (RadioButton) findViewById( R.id.radio_disabled ));
		mRadioButtonsMap.put(WheelchairState.UNKNOWN, (RadioButton) findViewById( R.id.radio_unknown ));
		
		// set onCLick listener
		for( WheelchairState state: mRadioButtonsMap.keySet() ) {
                	mRadioButtonsMap.get(state).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					DeselectAllRadioButtons();
					final RadioButton a = (RadioButton)v;
					a.setChecked(true);
					 // To send a result, simply call setResult() before your
		            // activity is finished.
		            setResult(RESULT_OK, (new Intent()).setAction(Integer.toString(getWheeChairState().getId())));
 				    finish();

				}
			});
		}	
	}
}
