package org.wheelmap.android.model;

import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.ui.POIsListItemView;
import org.wheelmap.android.R;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class POIsCursorAdapter extends CursorAdapter {
	Context mContext;	

	public POIsCursorAdapter(Context context, Cursor c) {
		super(context, c);
		mContext = context;    
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		POIsListItemView  pliv = (POIsListItemView ) view;


		String name = cursor.getString(cursor
				.getColumnIndexOrThrow(POIs.NAME));
		/*String description = cursor.getString(cursor
				.getColumnIndexOrThrow(POIs.DESCRIPTION));
		int icon_type = cursor.getInt(cursor
				.getColumnIndexOrThrow(POIs.CATEGORY));
*/
		if (TextUtils.isEmpty(name)) {
			name = context.getString(android.R.string.untitled);
		}

		pliv.setName(name);
		pliv.setDistance("cafe");
		pliv.setIcon(R.drawable.wheelmap_1);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return new POIsListItemView(context);
	}
}