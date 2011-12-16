/*
Copyright (C) 2011 Michal Harakal and Michael Kroez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS-IS" BASIS
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
        
 */

package org.wheelmap.android.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.Inflater;

import org.wheelmap.android.R;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.manager.SupportManager.Category;
import org.wheelmap.android.manager.SupportManager.NodeType;
import org.wheelmap.android.service.SyncService;

import wheelmap.org.WheelchairState;

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
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class SearchActivity extends Activity implements OnItemSelectedListener {
	private final static String TAG = "search";

	public final static int PERFORM_SEARCH = 1;
	public final static String EXTRA_SHOW_DISTANCE = "org.wheelmap.android.ui.EXTRA_SHOW_DISTANCE";

	private enum SearchTypes {
		NO_SELECTION, CATEGORY, NODETYPE
	}

	private EditText mKeywordText;

	private int mCategorySelected = -1;
	private int mNodeTypeSelected = -1;
	private float mDistance = -1;
	private WheelchairState mWheelchairState;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		mKeywordText = (EditText) findViewById(R.id.search_keyword);
		Spinner categorySpinner = (Spinner) findViewById(R.id.search_spinner_categorie_nodetype);
		ArrayList<Search> searchTypes = createSearchTypesList();
		categorySpinner.setAdapter(new SearchTypesAdapter(searchTypes));
		categorySpinner.setOnItemSelectedListener(this);

		Spinner wheelchairSpinner = (Spinner) findViewById(R.id.search_spinner_wheelchair_state);
		ArrayAdapter<CharSequence> wheelchairSpinnerAdapter = ArrayAdapter
				.createFromResource(this, R.array.wheelchair_state,
						android.R.layout.simple_spinner_item);
		wheelchairSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		wheelchairSpinner.setAdapter(wheelchairSpinnerAdapter);
		wheelchairSpinner.setOnItemSelectedListener(this);
		wheelchairSpinner.setPromptId(R.string.settings_wheelchair_state);
		wheelchairSpinner.setSelection(4);

		Spinner distanceSpinner = (Spinner) findViewById(R.id.search_spinner_distance);

		ArrayAdapter<CharSequence> distanceSpinnerAdapter = ArrayAdapter
				.createFromResource(this, R.array.distance_array,
						android.R.layout.simple_spinner_item);

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

		RelativeLayout layout = (RelativeLayout) findViewById(R.id.search_layout);
		Animation anim = AnimationUtils.loadAnimation(this,
				R.anim.move_in_from_top);
		LayoutAnimationController controller = new LayoutAnimationController(
				anim, 0.0f);
		layout.setLayoutAnimation(controller);
	}

	private ArrayList<Search> createSearchTypesList() {
		SupportManager support = WheelmapApp.getSupportManager();
		ArrayList<Search> searchTypes = new ArrayList<Search>();
		searchTypes.add(new Search(getResources().getString(
				R.string.search_no_selection), -1, SearchTypes.NO_SELECTION));

		List<Category> categories = support.getCategoryList();
		Collections.sort(categories, new SupportManager.CategoryComparator());
		for (Category category : categories) {
			searchTypes.add(new Search(category.localizedName, category.id,
					SearchTypes.CATEGORY));
			List<NodeType> nodeTypes = support
					.getNodeTypeListByCategory(category.id);
			Collections
					.sort(nodeTypes, new SupportManager.NodeTypeComparator());
			for (NodeType nodeType : nodeTypes) {
				searchTypes.add(new Search(nodeType.localizedName, nodeType.id,
						SearchTypes.NODETYPE));
			}
		}

		return searchTypes;
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

		if (mWheelchairState != null)
			intent.putExtra(SyncService.EXTRA_WHEELCHAIR_STATE,
					mWheelchairState.getId());

		setResult(Activity.RESULT_OK, intent);
		Log.d(TAG, "onSearch: setResult");
		finish();
	}

	public void onItemSelected(AdapterView<?> adapterView, View view,
			int position, long id) {
		int viewId = adapterView.getId();

		switch (viewId) {
		case R.id.search_spinner_categorie_nodetype: {
			Search search = (Search) adapterView.getAdapter().getItem(position);
			switch (search.searchType) {
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

		case R.id.search_spinner_wheelchair_state: {
			mWheelchairState = WheelchairState.valueOf(position);
			break;
		}
		default:
			// noop
		}

	}

	public void onNothingSelected(AdapterView parent) {

	}

	private static class Search {
		public SearchTypes searchType;
		public String text;
		public int id;

		public Search(String text, int id, SearchTypes searchType) {
			this.text = text;
			this.id = id;
			this.searchType = searchType;

		}
	}

	private class SearchTypesAdapter extends BaseAdapter {

		private ArrayList<Search> items;

		public SearchTypesAdapter(ArrayList<Search> items) {
			this.items = items;
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
	        TextView text;

	        if (convertView == null) {
	        	LayoutInflater inflater = (LayoutInflater) SearchActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
	            view = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
	        } else {
	            view = convertView;
	        }
	        
	        text = (TextView) view.findViewById( android.R.id.text1);
	        text.setText(items.get(position).text);
	        return view;
		}
		
		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			View useView;
			Search item = items.get(position);

			if (item.searchType == SearchTypes.CATEGORY
					|| item.searchType == SearchTypes.NO_SELECTION)
				useView = new CategorySearchItemView(SearchActivity.this);
			else
				useView = new NodeTypeSearchItemView(SearchActivity.this);

			((ItemViewText) useView).setText(item.text);

			return useView;
		}
	}

	private interface ItemViewText {
		public void setText(String text);
	}

	private static class CategorySearchItemView extends FrameLayout implements
			ItemViewText {
		private CheckedTextView mText;

		public CategorySearchItemView(Context context) {
			super(context);
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			inflater.inflate(R.layout.search_category, this, true);
			mText = (CheckedTextView) findViewById(R.id.search_type);
		}

		public void setText(String text) {
			mText.setText(text);
		}
	}

	private static class NodeTypeSearchItemView extends FrameLayout implements
			ItemViewText {
		private CheckedTextView mText;

		public NodeTypeSearchItemView(Context context) {
			super(context);
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			inflater.inflate(R.layout.search_nodetype, this, true);

			mText = (CheckedTextView) findViewById(R.id.search_type);
		}

		public void setText(String text) {
			mText.setText(text);
		}

	}

}
