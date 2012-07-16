package org.wheelmap.android.activity;

import org.wheelmap.android.fragment.InfoFragment.OnInfoListener;
import org.wheelmap.android.online.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class InfoActivity extends SherlockFragmentActivity implements
		OnInfoListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment_info);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
	}

	@Override
	public void onViewUri(Uri uri) {

		Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
		startActivity(intent);
	}

	@Override
	public void onNextView(String view) {
		Class<? extends SherlockFragmentActivity> clzz;
		if (view.equals("LegalNotice"))
			clzz = LegalNoticeActivity.class;
		else
			return;

		Intent intent = new Intent(this, clzz);
		startActivity(intent);

	}
}
