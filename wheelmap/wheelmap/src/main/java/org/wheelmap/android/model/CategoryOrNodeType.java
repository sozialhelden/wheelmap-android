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
package org.wheelmap.android.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.wheelmap.android.online.R;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.manager.SupportManager.Category;
import org.wheelmap.android.manager.SupportManager.NodeType;

import android.content.Context;

public class CategoryOrNodeType {
	public enum Types {
		NO_SELECTION, CATEGORY, NODETYPE
	}

	public Types type;
	public String text;
	public int id;

	public CategoryOrNodeType(String text, int id, Types type) {
		this.text = text;
		this.id = id;
		this.type = type;
	}
	
	public static ArrayList<CategoryOrNodeType> createTypesList( Context context, boolean addAll ) {
		SupportManager support = WheelmapApp.getSupportManager();
	
		ArrayList<CategoryOrNodeType> types = new ArrayList<CategoryOrNodeType>();
		if ( addAll )
			types.add(new CategoryOrNodeType(context.getResources().getString(
					R.string.search_no_selection), -1, Types.NO_SELECTION));

		List<Category> categories = support.getCategoryList();
		Collections.sort(categories, new SupportManager.CategoryComparator());
		for (Category category : categories) {
			types.add(new CategoryOrNodeType(category.localizedName, category.id,
					Types.CATEGORY));
			List<NodeType> nodeTypes = support
					.getNodeTypeListByCategory(category.id);
			Collections
					.sort(nodeTypes, new SupportManager.NodeTypeComparator());
			for (NodeType nodeType : nodeTypes) {
				types.add(new CategoryOrNodeType(nodeType.localizedName, nodeType.id,
						Types.NODETYPE));
			}
		}

		return types;
	}
}