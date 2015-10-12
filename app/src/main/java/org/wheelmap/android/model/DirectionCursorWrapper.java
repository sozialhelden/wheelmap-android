package org.wheelmap.android.model;

import android.database.Cursor;
import android.database.CursorWrapper;

public class DirectionCursorWrapper extends CursorWrapper {

    public final static String SHOW_DIRECTION_COLUMN_NAME = "show_direction_column";

    public final int SHOW_DIRECTION_COLUMN_INDEX;

    private float mDeviceDirection;

    private final int DIRECTION_COLUMN_INDEX;

    public DirectionCursorWrapper(Cursor cursor) {
        super(cursor);
        mDeviceDirection = 0;
        SHOW_DIRECTION_COLUMN_INDEX = cursor.getColumnCount();

        DIRECTION_COLUMN_INDEX = cursor
                .getColumnIndex(POIsCursorWrapper.DIRECTION_COLUMN_NAME);
    }

    public int getColumnCount() {
        return super.getColumnCount() + 1;
    }

    @Override
    public int getColumnIndex(String columnName) {
        if (columnName.equals(SHOW_DIRECTION_COLUMN_NAME)) {
            return SHOW_DIRECTION_COLUMN_INDEX;
        } else {
            return super.getColumnIndex(columnName);
        }
    }

    public void setDeviceDirection(float direction) {
        mDeviceDirection = direction;
    }

    @Override
    public float getFloat(int columnIndex) {
        if (columnIndex == SHOW_DIRECTION_COLUMN_INDEX) {
            float direction = super.getFloat(DIRECTION_COLUMN_INDEX);
            return direction - mDeviceDirection;
        } else {
            return super.getFloat(columnIndex);
        }
    }
}
