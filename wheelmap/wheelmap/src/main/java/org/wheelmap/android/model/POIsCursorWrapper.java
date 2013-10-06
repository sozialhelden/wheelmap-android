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

import org.wheelmap.android.utils.GeoMath;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.location.Location;

public class POIsCursorWrapper extends CursorWrapper {

    public final static String LOCATION_COLUMN_NAME = "location_column";

    public final static String DIRECTION_COLUMN_NAME = "direction_column";

    public final int LOCATION_COLUMN_INDEX;

    public final int DIRECTION_COLUMN_INDEX;

    public final float[] distanceResult = new float[1];

    private Location mLocation;

    public POIsCursorWrapper(Cursor cursor, Location location) {
        super(cursor);
        mLocation = location;
        LOCATION_COLUMN_INDEX = cursor.getColumnCount();
        DIRECTION_COLUMN_INDEX = cursor.getColumnCount() + 1;
    }

    public int getColumnCount() {
        return super.getColumnCount() + 2;
    }

    @Override
    public int getColumnIndex(String columnName) {
        if (columnName.equals(LOCATION_COLUMN_NAME)) {
            return LOCATION_COLUMN_INDEX;
        } else if (columnName.equals(DIRECTION_COLUMN_NAME)) {
            return DIRECTION_COLUMN_INDEX;
        } else {
            return super.getColumnIndex(columnName);
        }
    }

    @Override
    public float getFloat(int columnIndex) {
        if ( mLocation == null) {
            return 0f;
        }

        if (columnIndex == LOCATION_COLUMN_INDEX) {
            Location target = new Location("");
            target.setLatitude(POIHelper.getLatitude(this));
            target.setLongitude(POIHelper.getLongitude(this));
            return GeoMath.calculateDistance(mLocation, target);
        } else if (columnIndex == DIRECTION_COLUMN_INDEX) {
            Location target = new Location("");
            target.setLatitude(POIHelper.getLatitude(this));
            target.setLongitude(POIHelper.getLongitude(this));
            return mLocation.bearingTo(target);
        } else {
            return super.getFloat(columnIndex);
        }
    }
}
