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
