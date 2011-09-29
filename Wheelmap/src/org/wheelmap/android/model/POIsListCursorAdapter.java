package org.wheelmap.android.model;

import org.wheelmap.android.R;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.manager.SupportManager.NodeType;
import org.wheelmap.android.ui.POIsListItemView;
import org.wheelmap.android.utils.GeocoordinatesMath;
import org.wheelmap.android.utils.GeocoordinatesMath.DistanceUnit;

import wheelmap.org.WheelchairState;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class POIsListCursorAdapter extends CursorAdapter {
	private final static String TAG = "poislist";
	private DistanceFormatter mDistanceFormatter;
		
	public POIsListCursorAdapter(Context context, Cursor cursor) {
		super( context, cursor );
		if ( GeocoordinatesMath.DISTANCE_UNIT == DistanceUnit.KILOMETRES )
			mDistanceFormatter = new DistanceFormatterMetric();
		else
			mDistanceFormatter = new DistanceFormatterAnglo();
	}

	@Override
	public long getItemId(int position) {		
		return position;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		POIsListItemView  pliv = (POIsListItemView ) view;
		SupportManager manager = WheelmapApp.getSupportManager();
		if ( manager == null ) {
			Log.d( TAG, "SupportManager is null - how can that be?");
		}
		
		String name = POIHelper.getName(cursor);
		WheelchairState state = POIHelper.getWheelchair(cursor);
		int index = cursor.getColumnIndex( POIsCursorWrapper.LOCATION_COLUMN_NAME );
		double distance = cursor.getDouble( index );
		int categoryId = POIHelper.getCategoryId( cursor );
		int nodeTypeId = POIHelper.getNodeTypeId( cursor );
		NodeType nodeType = manager.lookupNodeType(nodeTypeId);

		if ( name.length() > 0 )
			pliv.setName( name );
		else {
			String nodeTypeName = nodeType.localizedName;
			pliv.setName( nodeTypeName );
		}
		String category = manager.lookupCategory( categoryId ).localizedName;
		pliv.setCategory( category + " - " + nodeType.localizedName );

		pliv.setDistance( mDistanceFormatter.format( distance ));
		Drawable marker = manager.lookupWheelDrawable(state.getId());
		pliv.setIcon( marker );
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return new POIsListItemView(context);
	}
	
	private interface DistanceFormatter {
		String format( double distance );
	}
	
	private class DistanceFormatterMetric implements DistanceFormatter {
		@Override
		public String format(double distance) {
			if ( distance < 1.0 )
				return String.format( "%2.0f0m", distance * 100.0 );
			else
				return String.format( "%.1fkm", distance );
		}
	}
	
	private class DistanceFormatterAnglo implements DistanceFormatter {
		@Override
		public String format(double distance) {
			if ( distance < 1.0 )
				return String.format( "%0.2fmi", distance );
			else
				return String.format( "%.1fmi", distance );
		}	
	}
	
}