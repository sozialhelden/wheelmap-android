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