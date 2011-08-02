package org.wheelmap.android.model;

import org.wheelmap.android.R;
import org.wheelmap.android.ui.POIsListItemView;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class POIsCursorAdapter extends CursorAdapter {
	Context mContext;	
	POIHelper mPOIHelper;

	public POIsCursorAdapter(Context context, Cursor c) {
		super(context, c);
		mContext = context;   
		mPOIHelper = new POIHelper();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		POIsListItemView  pliv = (POIsListItemView ) view;


		pliv.setCategory(mPOIHelper.getAddress(cursor));		
		pliv.setName(mPOIHelper.getName(cursor));

		// TODO calculate distance from current location
		pliv.setDistance("123 m");
		
		switch (mPOIHelper.getWheelchair(cursor)) {
		case UNKNOWN: 
			pliv.setIcon(R.drawable.marker_unknown);
			break;
		case YES:
			pliv.setIcon(R.drawable.marker_yes);
			break;
		case LIMITED:
			pliv.setIcon(R.drawable.marker_limited);
			break;
		case NO:
			pliv.setIcon(R.drawable.marker_no);
			break;
		default:
			pliv.setIcon(R.drawable.marker_unknown);
			break;
		}
		
		
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return new POIsListItemView(context);
	}
}