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
package org.wheelmap.android.ui;

import java.util.ArrayList;

import org.wheelmap.android.online.R;
import org.wheelmap.android.model.CategoryNodeTypesAdapter;
import org.wheelmap.android.model.CategoryOrNodeType;
import org.wheelmap.android.service.SyncService;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class SearchActivity extends Activity implements OnItemSelectedListener {
	private final static String TAG = "search";

	public final static int PERFORM_SEARCH = 1;
	public final static String EXTRA_SHOW_DISTANCE = "org.wheelmap.android.ui.EXTRA_SHOW_DISTANCE";
	public final static String EXTRA_SHOW_MAP_HINT = "org.wheelmap.android.ui.EXTRA_SHOW_MAP_HINT";

	private EditText mKeywordText;

	private int mCategorySelected = -1;
	private int mNodeTypeSelected = -1;
	private float mDistance = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		LinearLayout mapHintContainer = (LinearLayout) findViewById( R.id.search_map_hint);
		if (getIntent() != null && getIntent().getExtras() != null) {
			if (getIntent().getExtras().containsKey(EXTRA_SHOW_MAP_HINT))
				mapHintContainer.setVisibility(View.VISIBLE);
		}
		

		mKeywordText = (EditText) findViewById(R.id.search_keyword);

		Spinner categorySpinner = (Spinner) findViewById(R.id.search_spinner_categorie_nodetype);
		
		ArrayList<CategoryOrNodeType> searchTypes = CategoryOrNodeType.createTypesList(this, true);
		categorySpinner.setAdapter(new CategoryNodeTypesAdapter(this, searchTypes, CategoryNodeTypesAdapter.SEARCH_MODE));
		categorySpinner.setOnItemSelectedListener(this);

		Spinner distanceSpinner = (Spinner) findViewById(R.id.search_spinner_distance);

		MyCustomSpinnerAdapter distanceSpinnerAdapter = MyCustomSpinnerAdapter
				.createFromResource(this, R.array.distance_array,
						R.layout.simple_my_spinner_item);

		distanceSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		distanceSpinner.setAdapter(distanceSpinnerAdapter);
		distanceSpinner.setOnItemSelectedListener(this);
		distanceSpinner.setPromptId(R.string.search_distance);
		distanceSpinner.setSelection(3);

		LinearLayout distanceContainer = (LinearLayout) findViewById(R.id.search_spinner_distance_container);
		if (getIntent() != null && getIntent().getExtras() != null) {
			if (getIntent().getExtras().containsKey(EXTRA_SHOW_DISTANCE))
				distanceContainer.setVisibility(View.VISIBLE);
		}

		LinearLayout layout = (LinearLayout) findViewById(R.id.search_layout);
		Animation anim = AnimationUtils.loadAnimation(this,
				R.anim.move_in_from_top);
		LayoutAnimationController controller = new LayoutAnimationController(
				anim, 0.0f);
		layout.setLayoutAnimation(controller);
	}

	public void onSearch(View v) {
		Intent intent = new Intent();

		String keyword = mKeywordText.getText().toString();
		if (keyword.length() > 0)
			intent.putExtra(SearchManager.QUERY, keyword);

		Log.d(TAG, "mCategory = " + mCategorySelected + " mNodeType = "
				+ mNodeTypeSelected);
		if (mCategorySelected != -1)
			intent.putExtra(SyncService.EXTRA_CATEGORY, mCategorySelected);
		else if (mNodeTypeSelected != -1)
			intent.putExtra(SyncService.EXTRA_NODETYPE, mNodeTypeSelected);

		if (mDistance != -1)
			intent.putExtra(SyncService.EXTRA_DISTANCE_LIMIT, mDistance);

		setResult(Activity.RESULT_OK, intent);
		Log.d(TAG, "onSearch: setResult");
		finish();
	}

	public void onItemSelected(AdapterView<?> adapterView, View view,
			int position, long id) {
		int viewId = adapterView.getId();

		switch (viewId) {
		case R.id.search_spinner_categorie_nodetype: {
			CategoryOrNodeType search = (CategoryOrNodeType) adapterView.getAdapter().getItem(position);
			switch (search.type) {
			case CATEGORY:
				mCategorySelected = search.id;
				break;
			case NODETYPE:
				mNodeTypeSelected = search.id;
				break;
			}
			break;
		}
		case R.id.search_spinner_distance: {
			String distance = (String) adapterView.getItemAtPosition(position);
			try {
				mDistance = Float.valueOf(distance);
			} catch (NumberFormatException e) {
				// ignore
			}
			break;
		}
		default:
			// noop
		}

	}

	public void onNothingSelected(AdapterView<?> parent) {

	}

	private static class MyCustomSpinnerAdapter extends
			ArrayAdapter<CharSequence> {

		public MyCustomSpinnerAdapter(Context context, int textViewResourceId,
				CharSequence[] strings) {
			super(context, textViewResourceId, strings);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			TextView text;

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.simple_my_spinner_item,
						parent, false);
			} else {
				view = convertView;
			}

			text = (TextView) view.findViewById(android.R.id.text1);
			text.setText(getItem(position));
			return view;
		}

		public static MyCustomSpinnerAdapter createFromResource(
				Context context, int textArrayResId, int textViewResId) {
			CharSequence[] strings = context.getResources().getTextArray(
					textArrayResId);
			return new MyCustomSpinnerAdapter(context, textViewResId, strings);
		}

	}
}
