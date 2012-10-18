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

import java.util.ArrayList;

import org.wheelmap.android.adapter.InfoWidgetsAdapter;
import org.wheelmap.android.model.Info;
import org.wheelmap.android.model.InfoTypes;
import org.wheelmap.android.online.R;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

public class InfoFragment extends SherlockListFragment {
	ArrayList<Info> infoList = new ArrayList<Info>();

	private OnInfoListener mListener;

	public interface OnInfoListener {
		public void onNextView(String view);

		public void onViewUri(Uri uri);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof OnInfoListener)
			mListener = (OnInfoListener) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Info info = null;
		info = new Info(R.string.info_wheelmap_android_one,
				createVersionString(), "", InfoTypes.SIMPLE_TEXT);
		infoList.add(info);

		// web version
		info = new Info(R.string.info_web_version,
				R.string.info_web_version_one, "http://www.wheelmap.org",
				InfoTypes.SIMPLE_TEXT);
		infoList.add(info);

		// mapdata
		info = new Info(R.string.info_kartendaten,
				R.string.info_kartendaten_one, R.string.info_kartendaten_two,
				"http://www.openstreetmap.org", InfoTypes.DOUBLE_TEXT);
		infoList.add(info);

		// android version
		info = new Info(R.string.info_android_development,
				R.string.info_android_development_one, "http://fiwio.com",
				R.string.info_android_development_two,
				"http://studiorutton.de", InfoTypes.WITH_TWO_LINKS);
		infoList.add(info);

		info = new Info(R.string.info_clientdevelopment,
				R.string.info_clientdevelopment_one, "", InfoTypes.SIMPLE_TEXT);
		infoList.add(info);

		// web development
		info = new Info(R.string.info_webdevelopment,
				R.string.info_webdevelopment_one,
				"http://www.christophbuente.de", InfoTypes.SIMPLE_TEXT);
		infoList.add(info);
		// legal notice
		info = new Info(R.string.btn_legal_notice, "LegalNotice",
				InfoTypes.NEXT_ACTIVITY);
		infoList.add(info);

		// project by sozialhelden
		info = new Info(R.string.info_a_project_of,
				R.drawable.logo_sozialhelden_232x47,
				"http://www.sozialhelden.de", InfoTypes.WITH_IMAGE);
		infoList.add(info);
		// thanks stiftung
		info = new Info(R.string.info_stiftung_text_one, R.drawable.logo_fds,
				"http://www.fdst.de/", InfoTypes.WITH_IMAGE);
		infoList.add(info);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_info, container, false);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		InfoWidgetsAdapter infoAdapter = new InfoWidgetsAdapter(getActivity(),
				infoList);
		setListAdapter(infoAdapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Info info = (Info) getListAdapter().getItem(position);
		switch (info.getInfoType()) {
		case NEXT_ACTIVITY:
			if (mListener != null)
				mListener.onNextView(info.getNextView());
			break;
		default:

			if (mListener != null)
				mListener.onViewUri(Uri.parse(info.getUrl()));
		}
	}

	private String createVersionString() {
		PackageInfo packageInfo;
		String version;
		try {
			packageInfo = getActivity().getPackageManager().getPackageInfo(
					getActivity().getPackageName(), 0);
			version = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			version = "Unknown";
		}
		return String.format("%s: %s",
				getResources().getString(R.string.info_wheelmap_android_two),
				version);
	}
}
