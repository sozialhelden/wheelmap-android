package org.wheelmap.android.model;

import java.util.HashMap;

import org.wheelmap.android.model.Wheelmap.POIs;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class POIsProvider extends ContentProvider {

	private static final UriMatcher sUriMatcher;
	private static HashMap<String, String> sPOIsProjectionMap;
	
	/** this is suitable for use by insert/update/delete/query and may be passed
	 * as a method call parameter. Only insert/update/delete/query should call .clear() on it */
	private final ContentValues mValues = new ContentValues();


	private static final int POIS = 1;
	private static final int POI_ID = 2;	
	

	public static final String ID    = BaseColumns._ID;
	public static final String VALUE = "value";

	private static final String TAG = "POIsProvider";

	private static final String DATABASE_NAME = "wheelmap.db";
	private static final int DATABASE_VERSION = 5;
	private static final String POIS_TABLE_NAME = "pois";

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// places 
			db.execSQL("CREATE TABLE " + POIS_TABLE_NAME 
					+ " ("
					+ POIs._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ POIs.WM_ID + " INTEGER, "
					+ POIs.NAME + " TEXT," 
					+ POIs.COORD_LAT + " VARCHAR(15),"
					+ POIs.COORD_LON + " VARCHAR(15)," 
					+ POIs.STREET + " TEXT," 
					+ POIs.HOUSE_NUM + " TEXT,"
					+ POIs.POSTCODE + " TEXT,"
					+ POIs.CITY + " TEXT,"
					+ POIs.PHONE + " TEXT, "
					+ POIs.WEBSITE + " TEXT, "
					+ POIs.WHEELCHAIR + " TEXT, "
					+ POIs.WHEELCHAIR_DESC + " TEXT )");
					
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + POIS_TABLE_NAME);
			onCreate(db);
		}
	}

	private DatabaseHelper mOpenHelper;

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		Log.v(TAG, "PlacessProvider.delete: url=" + uri);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case POIS:
			count = db.delete(POIS_TABLE_NAME, where, whereArgs);
			break;

		case POI_ID:
			// delete hours assigned to places
			
			String placeId = uri.getPathSegments().get(1);
			
			
			count = db.delete(POIS_TABLE_NAME, POIs._ID + "=" + placeId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case POIS:
			return POIs.CONTENT_TYPE;
		case POI_ID:
			return POIs.CONTENT_ITEM_TYPE;		
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case POIS:
			count = db.update(POIS_TABLE_NAME, values, where, whereArgs);
			break;

		case POI_ID:
			String placeId = uri.getPathSegments().get(1);
			count = db.update(POIS_TABLE_NAME, values, POIs._ID + "=" + placeId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;
				default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}


	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		int match = sUriMatcher.match(uri);

		Log.v(TAG, "PlacessProvider.query: url=" + uri + ", match is " + match);

		switch (match) {
		case POIS:
			mValues.clear();
			if (initialValues == null)  {
				initialValues = new ContentValues();
				// dummy POI
				initialValues.put(POIs.NAME, "New POI"); 
			}				
			mValues.putAll( initialValues);
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			long rowId = db.insert(POIS_TABLE_NAME, POIs.NAME, mValues);
			if (rowId > 0) {
				Uri placeUri = ContentUris.withAppendedId(Wheelmap.POIs.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(placeUri, null);
				return placeUri;
			}

			throw new SQLException("Failed to insert row into " + uri);
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		int match = sUriMatcher.match(uri);

		Log.v(TAG, "ContactsProvider.query: url=" + uri + ", match is " + match);

		String orderBy;

		// If no sort order is specified use the default
		switch (match) {
		case POIS:
			qb.setTables(POIS_TABLE_NAME);
			qb.setProjectionMap(sPOIsProjectionMap);
			if (TextUtils.isEmpty(sortOrder)) {
				orderBy = Wheelmap.POIs.DEFAULT_SORT_ORDER;
			} else {
				orderBy = sortOrder;
			}

			break;

		case POI_ID:
			qb.setTables(POIS_TABLE_NAME);
			qb.setProjectionMap(sPOIsProjectionMap);
			qb.appendWhere(POIs._ID + "=" + uri.getPathSegments().get(1));

			if (TextUtils.isEmpty(sortOrder)) {
				orderBy = POIs.DEFAULT_SORT_ORDER;
			} else {
				orderBy = sortOrder;
			}
			break;		
		default:
			throw new IllegalArgumentException("Unknown URI " + uri + "by inserting");
		}


		// Get the database and run the query
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

		// Tell the cursor what uri to watch, so it knows when its source data changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(Wheelmap.AUTHORITY, "pois", POIS);
		sUriMatcher.addURI(Wheelmap.AUTHORITY, "pois/#", POI_ID);
		
		//  POIs
		sPOIsProjectionMap = new HashMap<String, String>();
		sPOIsProjectionMap.put(POIs._ID, POIs._ID);
		sPOIsProjectionMap.put(POIs.WM_ID, POIs.WM_ID );
		sPOIsProjectionMap.put(POIs.NAME, POIs.NAME);
		sPOIsProjectionMap.put(POIs.COORD_LAT, POIs.COORD_LAT);
		sPOIsProjectionMap.put(POIs.COORD_LON, POIs.COORD_LON);
		sPOIsProjectionMap.put(POIs.STREET, POIs.STREET );
		sPOIsProjectionMap.put(POIs.HOUSE_NUM, POIs.HOUSE_NUM);
		sPOIsProjectionMap.put(POIs.POSTCODE, POIs.POSTCODE);
		sPOIsProjectionMap.put(POIs.CITY, POIs.CITY);
		sPOIsProjectionMap.put(POIs.PHONE, POIs.PHONE );
		sPOIsProjectionMap.put(POIs.WEBSITE, POIs.WEBSITE );
		sPOIsProjectionMap.put(POIs.WHEELCHAIR, POIs.WHEELCHAIR );
		sPOIsProjectionMap.put(POIs.WHEELCHAIR_DESC, POIs.WHEELCHAIR_DESC );
		
	}
}
