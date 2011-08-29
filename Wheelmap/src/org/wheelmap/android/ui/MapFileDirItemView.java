package org.wheelmap.android.ui;

import org.wheelmap.android.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MapFileDirItemView extends FrameLayout {
	private TextView mDirectoryName;
	
	public MapFileDirItemView(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.map_list_item_dir, this, true);
		
		mDirectoryName = (TextView) findViewById( R.id.list_item_dir_name );
	}
	
	public void setDirName( String name ) {
		mDirectoryName.setText( name );
	}

}
