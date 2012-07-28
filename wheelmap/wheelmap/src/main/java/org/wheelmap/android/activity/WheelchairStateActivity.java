package org.wheelmap.android.activity;

import org.wheelmap.android.fragment.WheelchairStateFragment;
import org.wheelmap.android.fragment.WheelchairStateFragment.OnWheelchairState;
import org.wheelmap.android.model.Extra;

import wheelmap.org.WheelchairState;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.akquinet.android.androlog.Log;

public class WheelchairStateActivity extends SherlockFragmentActivity implements
		OnWheelchairState {
	private final static String TAG = WheelchairStateActivity.class
			.getSimpleName();
	private Fragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		FragmentManager fm = getSupportFragmentManager();
		mFragment = (WheelchairStateFragment) fm
				.findFragmentByTag(WheelchairStateFragment.TAG);
		if (mFragment != null) {
			return;
		}

		int stateId = getIntent().getIntExtra(Extra.WHEELCHAIR_STATE,
				Extra.UNKNOWN);
		mFragment = WheelchairStateFragment.newInstance(WheelchairState
				.valueOf(stateId));

		fm.beginTransaction()
				.add(android.R.id.content, mFragment,
						WheelchairStateFragment.TAG).commit();
	}

	@Override
	public void onWheelchairStateSelect(WheelchairState state) {
		Intent intent = new Intent();
		intent.putExtra(Extra.WHEELCHAIR_STATE, state.getId());
		setResult(RESULT_OK, intent);
		finish();
	}

}
