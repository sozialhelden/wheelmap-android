package org.wheelmap.android.utils;

import java.io.IOException;
import java.io.InputStream;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;

public class UtilsMisc {
	
	private static final String TAG = UtilsMisc.class.getSimpleName();
	
	public static void dumpCursorToLog(String tag, Cursor cursor) {
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			String row = DatabaseUtils.dumpCurrentRowToString(cursor);
			Log.d(tag, row);
			cursor.moveToNext();
		}

	}
	
	public static void closeSilently(final InputStream inStream)
	{
		if (inStream == null)
		{
			return;
		}

		try
		{
			inStream.close();
		}
		catch (final IOException e)
		{
			// do nothing because we close silently
			Log.w(TAG, "cannot close inStream stream", e);
		}
	}
}