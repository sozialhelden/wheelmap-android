package org.wheelmap.android.ui;

import org.wheelmap.android.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MapFileSelectItemView extends FrameLayout {
	TextView mMapName;
	TextView mMapDirectory;
	CheckBox mMapCheckbox;

	public MapFileSelectItemView(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.select_map_list_item, this, true);
		
		mMapName = (TextView) findViewById( R.id.list_item_select_name );
		mMapDirectory = (TextView) findViewById( R.id.list_item_select_dir );
		mMapCheckbox = (CheckBox) findViewById( R.id.list_item_select_checkbox );
		mMapCheckbox.setClickable( false );
		mMapCheckbox.setFocusable( false );
	}
	
	public void setName( String name ) {
		mMapName.setText( name );
	}
	
	public void setDirectory( String directory ) {
		mMapDirectory.setText( directory );
	}
	
	public void setCheckboxChecked( boolean checked ) {
		mMapCheckbox.setChecked( checked );
	}
}
