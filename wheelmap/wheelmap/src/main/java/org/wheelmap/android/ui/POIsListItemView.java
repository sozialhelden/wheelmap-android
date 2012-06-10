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

import org.wheelmap.android.online.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class POIsListItemView extends RelativeLayout {

	private TextView poiName;
	private TextView poiCategory;
	private TextView poiNodeType;
	private TextView poiDistance;
	private ImageView poiIcon;

	/**
	 * constructor creates a new line item of list
	 * @param context 
	 */
	public POIsListItemView(Context context) {
		super(context);
		// inflate rating
		LayoutInflater inflater = (LayoutInflater) context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.pois_list_item, this, true);

		poiName = (TextView) findViewById(R.id.list_item_place_name);
		poiCategory = (TextView) findViewById(R.id.list_item_category);
		poiNodeType = (TextView ) findViewById( R.id.list_item_nodetype);
		poiDistance = (TextView) findViewById(R.id.list_item_distance);
		poiIcon = (ImageView)findViewById(R.id.place_type_icon);
	}

	/**
	 * Convenience method to set the properties of POI
	 */
	public void setName(String text) {
		poiName.setText(text);
	}

	public void setCategory(String text) {
		poiCategory.setText(text);
	}
	
	public void setNodeType(String text) {
		poiNodeType.setText(text);
	}

	public void setDistance(String text) {
		poiDistance.setText(text);
	}

	public void setIcon(Drawable drawable) {
		poiIcon.setImageDrawable( drawable );
	}
}
