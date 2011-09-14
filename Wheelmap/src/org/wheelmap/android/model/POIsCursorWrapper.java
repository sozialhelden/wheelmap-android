package org.wheelmap.android.model;

import org.wheelmap.android.utils.GeocoordinatesMath;

import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;
import android.database.Cursor;
import android.database.CursorWrapper;

public class POIsCursorWrapper extends CursorWrapper {
	public static final String TAG = "poislist";

	public final static String LOCATION_COLUMN_NAME = "location_column";
	public int LOCATION_COLUMN_INDEX;

	private Wgs84GeoCoordinates mLocation;

	public POIsCursorWrapper(Cursor cursor, Wgs84GeoCoordinates location) {
		super(cursor);
		mLocation = location;
		LOCATION_COLUMN_INDEX = cursor.getColumnCount();
	}
	
	public int getColumnCount() {
		return super.getColumnCount() + 1;
	}
	
	public void setLocation( Wgs84GeoCoordinates location ) {
		mLocation = location;
		super.requery();
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
					new Wgs84GeoCoordinates(POIHelper.getLongitude(this),
							POIHelper.getLatitude(this)));
		}
		else
			return super.getDouble(columnIndex);
	}
}
