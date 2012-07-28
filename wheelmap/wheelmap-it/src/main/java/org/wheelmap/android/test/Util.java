package org.wheelmap.android.test;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;

public class Util {

	public static void dumpCursorToLog( String tag,  Cursor cursor ) {
		cursor.moveToFirst();
		
		while( !cursor.isAfterLast()) {
			String row = DatabaseUtils.dumpCurrentRowToString(cursor);
			Log.d( tag, row );
			cursor.moveToNext();
		}
		
	}
}
