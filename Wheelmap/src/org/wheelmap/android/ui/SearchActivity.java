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

import org.wheelmap.android.R;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.manager.SupportManager.Category;
import org.wheelmap.android.manager.SupportManager.NodeType;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class SearchActivity extends Activity {
	private final static String TAG = "search";

	private enum SearchTypes {
		NO_SELECTION, CATEGORY, NODETYPE
	}

	private Spinner mSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		mSpinner = (Spinner) findViewById(R.id.search_spinner);
		ArrayList<Search> searchTypes = createSearchTypesList();
		mSpinner.setAdapter(new SearchTypesAdapter(searchTypes));
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
			View useView;
			Search item = items.get(position);

			if (item.searchType == SearchTypes.CATEGORY || item.searchType == SearchTypes.NO_SELECTION )
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
		private TextView mText;

		public CategorySearchItemView(Context context) {
			super(context);
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			inflater.inflate(R.layout.search_category, this, true);
			mText = (TextView) findViewById(R.id.search_type);
		}

		public void setText(String text) {
			mText.setText(text);
		}
	}

	private static class NodeTypeSearchItemView extends FrameLayout implements
			ItemViewText {
		private TextView mText;

		public NodeTypeSearchItemView(Context context) {
			super(context);
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			inflater.inflate(R.layout.search_nodetype, this, true);

			mText = (TextView) findViewById(R.id.search_type);
		}

		public void setText(String text) {
			mText.setText(text);
		}

	}

}
