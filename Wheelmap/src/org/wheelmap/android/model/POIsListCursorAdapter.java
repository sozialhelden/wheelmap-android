package org.wheelmap.android.model;


import java.util.Formatter;

import org.wheelmap.android.R;
import org.wheelmap.android.ui.POIsListItemView;
import org.wheelmap.android.utils.GeocoordinatesMath;
import org.wheelmap.android.utils.GeocoordinatesMath.DistanceUnit;

import wheelmap.org.WheelchairState;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class POIsListCursorAdapter extends CursorAdapter {
	private final static String TAG = "poislist";
		
	public POIsListCursorAdapter(Context context, Cursor cursor) {
		super( context, cursor );
	}

	@Override
	public long getItemId(int position) {		
		return position;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		POIsListItemView  pliv = (POIsListItemView ) view;
		
		String name = POIHelper.getName(cursor);
		String category = POIHelper.getAddress(cursor);
		WheelchairState state = POIHelper.getWheelchair(cursor);

		pliv.setName(name);
		pliv.setCategory(category);		

		int index = cursor.getColumnIndex( POIsCursorWrapper.LOCATION_COLUMN_NAME );
		double distance = cursor.getDouble( index );
		pliv.setDistance( formatDistance( distance ) );
		
		switch (state) {
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
	
	private String formatDistance( double distance ) {
		if ( GeocoordinatesMath.DISTANCE_UNIT == DistanceUnit.KILOMETRES )
			if ( distance < 1.0 )
				return String.format( "%2.0f0m", distance * 100 );
			else
				return String.format( "%.1fkm", distance );
		else
			if ( distance < 1.0 )
				return String.format( "%0.2fmi", distance );
			else
				return String.format( "%.1fmi", distance );
	}
	

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return new POIsListItemView(context);
	}

}