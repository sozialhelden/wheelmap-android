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

import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.model.Support.CategoriesContent;
import org.wheelmap.android.model.Support.LastUpdateContent;

import wheelmap.org.domain.categories.Categories;
import wheelmap.org.domain.categories.Category;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import java.math.BigInteger;

import static org.wheelmap.android.model.Support.*;

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
		BigInteger id = item.getId();
		if ( id != null )
			values.put(CategoriesContent.CATEGORY_ID, id.intValue());

		values.put(CategoriesContent.LOCALIZED_NAME, item.getLocalizedName());
		values.put(CategoriesContent.IDENTIFIER, item.getIdentifier());
		values.put(CategoriesContent.SELECTED, CategoriesContent.SELECTED_YES);
	}

	@Override
	public Uri getUri() {
		return CategoriesContent.CONTENT_URI;
	}

}
