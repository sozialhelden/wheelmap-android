package org.wheelmap.android.ui;

import org.wheelmap.android.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class CategorySelectItemView extends FrameLayout {
	ImageView mCategoryIcon;
	TextView mCategoryText;
	CheckBox mCategoryCheckBox;

	public CategorySelectItemView(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.category_select_list_item, this, true);
		
		mCategoryIcon = (ImageView) findViewById( R.id.list_item_category_icon );
		mCategoryText = (TextView) findViewById( R.id.list_item_category_text );
		mCategoryCheckBox = (CheckBox) findViewById( R.id.list_item_category_checkbox);
		
		mCategoryCheckBox.setClickable( false );
		mCategoryCheckBox.setFocusable( false );
	}
	
	public void setIcon( Drawable icon ) {
		mCategoryIcon.setImageDrawable( icon );
	}
	
	public void setName( String name ) {
		mCategoryText.setText( name );
	}
	
	public void setCheckboxChecked( boolean checked ) {
		mCategoryCheckBox.setChecked( checked );
	}
}
