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
package org.wheelmap.android.adapter;

import java.util.ArrayList;

import org.wheelmap.android.model.CategoryOrNodeType;
import org.wheelmap.android.online.R;
import org.wheelmap.android.view.CategoryItemView;
import org.wheelmap.android.view.NodeTypeItemView;
import org.wheelmap.android.view.TypeItemView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class TypesAdapter extends BaseAdapter implements
		SpinnerAdapter {
	public static final int SEARCH_MODE = 0;
	public static final int SELECT_MODE = 1;

	private int mType;

	private Context mContext;
	private ArrayList<CategoryOrNodeType> items;

	public TypesAdapter(Context context,
			ArrayList<CategoryOrNodeType> items, int type) {
		mContext = context;
		this.items = items;
		mType = type;
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
	public int getItemViewType(int position) {
		return items.get(position).type.ordinal();
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (mType == SELECT_MODE)
			return getDropDownView(position, convertView, parent);
		else
			return getSelectedItemView(position, convertView, parent);
	}

	private View getSelectedItemView(int position, View convertView,
			ViewGroup parent) {
		View view;
		TextView text;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.simple_my_spinner_item, parent,
					false);
		} else {
			view = convertView;
		}

		text = (TextView) view.findViewById(android.R.id.text1);
		text.setText(items.get(position).text);
		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View useView;
		CategoryOrNodeType item = items.get(position);

		if (item.type == CategoryOrNodeType.Types.CATEGORY
				|| item.type == CategoryOrNodeType.Types.NO_SELECTION)
			if (convertView instanceof CategoryItemView)
				useView = convertView;
			else
				useView = new CategoryItemView(mContext, mType);
		else if (convertView instanceof NodeTypeItemView)
			useView = convertView;
		else
			useView = new NodeTypeItemView(mContext);

		((TypeItemView) useView).setText(item.text);

		return useView;
	}
}
