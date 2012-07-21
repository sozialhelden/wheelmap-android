package org.wheelmap.android.model;

import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.model.Wheelmap.POIs;

import android.net.Uri;
import android.support.v4.content.CursorLoader;

public class CursorLoaderHelper {
	private CursorLoaderHelper() {

	}

	public static CursorLoader createPOIIdLoader(long id) {
		Uri uri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI_POI_ID,
				Long.toString(id));
		return new CursorLoader(WheelmapApp.get(), uri, null, null, null, null);
	}

	public static CursorLoader createWMIdLoader(String id) {
		Uri uri = Wheelmap.POIs.CONTENT_URI;
		String whereClause = "( " + POIs.WM_ID + " = ? )";
		String whereValues[] = new String[] { id };

		return new CursorLoader(WheelmapApp.get(), uri, null, whereClause,
				whereValues, null);
	}

	public static CursorLoader createTemporaryPOILoader() {
		Uri uri = Wheelmap.POIs.CONTENT_URI;
		String whereClause = "( " + Wheelmap.POIs.UPDATE_TAG + " = ? )";
		String[] whereValues = new String[] { Integer
				.toString(Wheelmap.UPDATE_TEMPORARY_STORE) };

		return new CursorLoader(WheelmapApp.get(), uri, null, whereClause,
				whereValues, null);
	}

}
