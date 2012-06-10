/*
 * #%L
 * Wheelmap - App
 * %%
 * Copyright (C) 2011 - 2012 Michal Harakal - Michael Kroez - Sozialhelden e.V.
 * %%
 * Wheelmap App based on the Wheelmap Service by Sozialhelden e.V.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
