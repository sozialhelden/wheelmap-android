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

import org.wheelmap.android.model.Support.CategoriesContent;
import org.wheelmap.android.ui.CategorySelectItemView;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class CategorySelectCursorAdapter extends CursorAdapter {

	public CategorySelectCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public void bindView(View view, Context ctx, Cursor cursor) {
		CategorySelectItemView itemView = (CategorySelectItemView) view;
		
		String name = Support.CategoriesContent.getLocalizedName( cursor );
		boolean selected = CategoriesContent.getSelected( cursor );			
		itemView.setName( name );
		itemView.setCheckboxChecked( selected );
	}

	@Override
	public View newView(Context ctx, Cursor cursor, ViewGroup parent) {
		return new CategorySelectItemView(ctx);
	}

}
