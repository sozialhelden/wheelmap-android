package org.wheelmap.android.activity;

import org.wheelmap.android.fragment.InfoFragment.OnInfoFragmentListener;
import org.wheelmap.android.online.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;

public class InfoActivity extends SherlockActivity implements
		OnInfoFragmentListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_fragment_info);

	}

	@Override
	public void onViewUri(Uri uri) {

		Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
		startActivity(intent);
	}

	@Override
	public void onNextView(String view) {
		Class<? extends Activity> clzz;
		if (view.equals("LegalNotice"))
			clzz = LegalNoticeActivity.class;
		else
			return;

		Intent intent = new Intent(this, clzz);
		startActivity(intent);

	}
}
