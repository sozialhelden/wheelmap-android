package org.wheelmap.android.model;

import org.wheelmap.android.utils.GeocoordinatesMath;

import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;
import android.database.Cursor;
import android.database.CursorWrapper;

public class POIsCursorWrapper extends CursorWrapper {

	public final static String LOCATION_COLUMN_NAME = "location_column";
	public final static int LOCATION_COLUMN_INDEX = 20;

	private Wgs84GeoCoordinates mLocation;
	private Cursor mCursor;

	public POIsCursorWrapper(Cursor cursor, Wgs84GeoCoordinates location) {
		super(cursor);
		mCursor = cursor;
		mLocation = location;
	}
	
	public void setLocation( Wgs84GeoCoordinates location ) {
		mLocation = location;
		mCursor.requery();
	}

	@Override
	public int getColumnIndex(String columnName) {
		if (columnName.equals(LOCATION_COLUMN_NAME))
			return LOCATION_COLUMN_INDEX;
		else
			return super.getColumnIndex(columnName);
	}

	@Override
	public double getDouble(int columnIndex) {
		if (columnIndex == LOCATION_COLUMN_INDEX) {
			return GeocoordinatesMath.calculateDistance(mLocation,
					new Wgs84GeoCoordinates(POIHelper.getLongitude(mCursor),
							POIHelper.getLatitude(mCursor)));
		}
		else
			return super.getDouble(columnIndex);
	}
}
