package org.wheelmap.android.model;

import org.wheelmap.android.model.Support.CategoriesContent;

import wheelmap.org.domain.categories.Categories;
import wheelmap.org.domain.categories.Category;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

public class DataOperationsCategories extends
		DataOperations<Categories, Category> {

	public DataOperationsCategories(ContentResolver resolver) {
		super(resolver);
	}

	@Override
	protected Category getItem(Categories item, int i) {
		return item.getCategories().get(i);
	}

	@Override
	public void copyToValues(Category item, ContentValues values) {
		values.clear();
		values.put(CategoriesContent.CATEGORY_ID, item.getId().intValue());
		values.put(CategoriesContent.LOCALIZED_NAME, item.getLocalizedName());
		values.put(CategoriesContent.IDENTIFIER, item.getIdentifier());
		values.put(CategoriesContent.SELECTED, CategoriesContent.SELECTED_YES);
	}

	@Override
	public Uri getUri() {
		return CategoriesContent.CONTENT_URI;
	}

}
