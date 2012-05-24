/*
Copyright (C) 2011 Michal Harakal and Michael Kroez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS-IS" BASIS
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
        
*/

package org.wheelmap.android.model;

import java.util.HashMap;

import org.wheelmap.android.model.Wheelmap.POIs;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
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

	/**
	 * this is suitable for use by insert/update/delete/query and may be passed
	 * as a method call parameter. Only insert/update/delete/query should call
	 * .clear() on it
	 */
	private final ContentValues mValues = new ContentValues();

	private static final int POIS = 1;
	private static final int POI_ID = 2;
	private static final int POIS_SORTED = 3;

	public static final String ID = BaseColumns._ID;
	public static final String VALUE = "value";

	private static final String TAG = "POIsProvider";

	private static final String DATABASE_NAME = "wheelmap.db";
	private static final int DATABASE_VERSION = 11;
	private static final String POIS_TABLE_NAME = "pois";

	private static class DistanceQueryBuilder {
		public String buildRawQuery(double longitude, double latitude, String whereParams) {
			double sin_lat_rad = Math.sin(latitude * Math.PI / 180);
			double sin_lon_rad = Math.sin(longitude * Math.PI / 180);
			double cos_lat_rad = Math.cos(latitude * Math.PI / 180);
			double cos_lon_rad = Math.cos(longitude * Math.PI / 180);
			StringBuilder a = new StringBuilder("SELECT *,(");
			a.append(sin_lat_rad);
			a.append("*\"sin_lat_rad\"+");
			a.append(cos_lat_rad);
			a.append("*\"cos_lat_rad\"*(");
			a.append(cos_lon_rad);
			a.append("*\"cos_lon_rad\"+");
			a.append(sin_lon_rad);
			a.append("*\"sin_lon_rad\")) AS \"distance_acos\" FROM \"pois\"");
			if (whereParams != null) {
				if (whereParams.trim().length() > 0) {
					a.append(" WHERE ");
					a.append(whereParams);
				}
			}
			a.append(" ORDER BY \"distance_acos\" DESC");

			//	SELECT *,(0.7934863768539137*"sin_lat_rad"+0.608588013147851*"cos_lat_rad"*(0.9726493751927453*"cos_lon_rad"+0.23227826617478065*"sin_lon_rad")) AS "distance_acos" FROM "pois" where category_id=8 OR category_id=10 ORDER BY "distance_acos" DESC
			// TODO maybe is a Formatter better
			// return 'SELECT *, (%(sin_lat_rad)f * "sin_lat_rad" +
			// %(cos_lat_rad)f * "cos_lat_rad" * (%(cos_lon_rad)f *
			// "cos_lon_rad" + %(sin_lon_rad)f * "sin_lon_rad")) AS
			// "distance_acos" FROM "pois" GROUP BY "id" HAVING "distance_acos"
			// < 1.25 ORDER BY "distance_acos" DESC' % {'sin_lat_rad':
			// sin_lat_rad, "cos_lat_rad": cos_lat_rad, 'sin_lon_rad':
			// sin_lon_rad, "cos_lon_rad": cos_lon_rad}

			Log.d(TAG, "query select argument for distance " + a.toString());

			return a.toString();
		}

	}

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + POIS_TABLE_NAME + " (" + POIs._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + POIs.WM_ID
					+ " VARCHAR(20), " + POIs.NAME + " TEXT," + POIs.COORD_LAT
					+ " VARCHAR(15)," + POIs.COORD_LON + " VARCHAR(15),"
					+ POIs.COS_LAT_RAD + " NUMERIC," + POIs.SIN_LAT_RAD
					+ " NUMERIC," + POIs.COS_LON_RAD + " NUMERIC,"
					+ POIs.SIN_LON_RAD + " NUMERIC," + POIs.STREET + " TEXT,"
					+ POIs.HOUSE_NUM + " TEXT," + POIs.POSTCODE + " TEXT,"
					+ POIs.CITY + " TEXT," + POIs.PHONE + " TEXT, "
					+ POIs.WEBSITE + " TEXT, " + POIs.WHEELCHAIR + " NUMERIC, "
					+ POIs.WHEELCHAIR_DESC + " TEXT,"
					+ POIs.CATEGORY_ID + " INTEGER, "
					+ POIs.CATEGORY_IDENTIFIER + " TEXT, "
					+ POIs.NODETYPE_ID + " INTEGER, "
					+ POIs.NODETYPE_IDENTIFIER + " TEXT, "
					+ POIs.UPDATE_TAG + " NUMERIC, "
					+ POIs.UPDATE_TIMESTAMP + " NUMERIC)");

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
	private DistanceQueryBuilder mQueryBuilder;

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

			count = db.delete(POIS_TABLE_NAME,
					POIs._ID
					+ "="
					+ placeId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where
							+ ')' : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		getContext().getContentResolver().notifyChange(
				POIs.CONTENT_URI_POI_SORTED, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case POIS:
			return POIs.CONTENT_TYPE;
		case POI_ID:
			return POIs.CONTENT_ITEM_TYPE;
		case POIS_SORTED:
			return POIs.CONTENT_TYPE_SORTED;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case POIS:
			count = db.update(POIS_TABLE_NAME, values, where, whereArgs);
			break;

		case POI_ID:
			String placeId = uri.getPathSegments().get(1);
			// TODO recalculate sin, cos values
			count = db.update(POIS_TABLE_NAME, values,
					POIs._ID
					+ "="
					+ placeId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where
							+ ')' : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		getContext().getContentResolver().notifyChange(
				POIs.CONTENT_URI_POI_SORTED, null);
		return count;
	}

	private void preCalculateLatLon(ContentValues values) {
		// pre calcutes sin and cos values of lat/lon
		// see wikipage
		// https://github.com/sozialhelden/wheelmap-android/wiki/Sqlite,-Distance-calculations
		if (values.containsKey(POIs.COORD_LAT)) {
			double lat = values.getAsFloat(POIs.COORD_LAT) / (double) 1E6;
			double sin_lat_rad = Math.sin(Math.toRadians(lat));
			double cos_lat_rad = Math.cos(Math.toRadians(lat));
			values.put(POIs.COS_LAT_RAD, cos_lat_rad);
			values.put(POIs.SIN_LAT_RAD, sin_lat_rad);
		} else {
			values.put(POIs.COS_LAT_RAD, 0);
			values.put(POIs.SIN_LAT_RAD, 0);
		}

		if (values.containsKey(POIs.COORD_LON)) {
			double lon = values.getAsFloat(POIs.COORD_LON) / (double) 1E6;
			double sin_lon_rad = Math.sin(Math.toRadians(lon));
			double cos_lon_rad = Math.cos(Math.toRadians(lon));
			values.put(POIs.COS_LON_RAD, cos_lon_rad);
			values.put(POIs.SIN_LON_RAD, sin_lon_rad);
		} else {
			values.put(POIs.COS_LON_RAD, 0);
			values.put(POIs.SIN_LON_RAD, 0);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		int match = sUriMatcher.match(uri);

		// Log.v(TAG, "PlacessProvider.query: url=" + uri + ", match is " +
		// match);

		switch (match) {
		case POIS:
			mValues.clear();
			if (initialValues == null) {
				initialValues = new ContentValues();
				// dummy POI
				initialValues.put(POIs.NAME, "New POI");
			}
			mValues.putAll(initialValues);
			preCalculateLatLon(mValues);

			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			long rowId = db.insert(POIS_TABLE_NAME, POIs.NAME, mValues);
			if (rowId > 0) {
				Uri placeUri = ContentUris.withAppendedId(
						Wheelmap.POIs.CONTENT_URI_POI_ID, rowId);
				getContext().getContentResolver().notifyChange(placeUri, null);
				getContext().getContentResolver().notifyChange(
						POIs.CONTENT_URI_POI_SORTED, null);
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
		mQueryBuilder = new DistanceQueryBuilder();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		int match = sUriMatcher.match(uri);

		Log.v(TAG, "POISProvider.query: url=" + uri + ", match is " + match);
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c;
		// If no sort order is specified use the default
		switch (match) {
		case POIS:
			qb.setTables(POIS_TABLE_NAME);
			qb.setProjectionMap(sPOIsProjectionMap);
			c = qb.query(db, projection, selection, selectionArgs, null, null,
					sortOrder);
			break;
		case POI_ID:
			qb.setTables(POIS_TABLE_NAME);
			qb.setProjectionMap(sPOIsProjectionMap);
			qb.appendWhere(" (" + POIs._ID + " = "
					+ uri.getPathSegments().get(1) + ") ");
			c = qb.query(db, projection, selection, selectionArgs, null, null,
					sortOrder);
			break;
		case POIS_SORTED:
			double longitude = Double.valueOf(selectionArgs[0]);
			double latitude = Double.valueOf(selectionArgs[1]);
			c = db.rawQuery(mQueryBuilder.buildRawQuery(longitude, latitude, selection),
					null);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] valuesArray) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int match = sUriMatcher.match(uri);

		DatabaseUtils.InsertHelper inserter = new DatabaseUtils.InsertHelper( db, POIS_TABLE_NAME);

		final int wmIdColumn = inserter.getColumnIndex( Wheelmap.POIs.WM_ID);
		final int nameColumn = inserter.getColumnIndex(Wheelmap.POIs.NAME);
		final int latColumn = inserter.getColumnIndex(Wheelmap.POIs.COORD_LAT);
		final int lonColumn = inserter.getColumnIndex(Wheelmap.POIs.COORD_LON);
		final int streetColumn = inserter.getColumnIndex(Wheelmap.POIs.STREET);
		final int houseNumColumn = inserter.getColumnIndex(Wheelmap.POIs.HOUSE_NUM);
		final int postcodeColumn = inserter.getColumnIndex(Wheelmap.POIs.POSTCODE);
		final int cityColumn = inserter.getColumnIndex(Wheelmap.POIs.CITY);
		final int phoneColumn = inserter.getColumnIndex(Wheelmap.POIs.PHONE);
		final int websiteColumn = inserter.getColumnIndex(Wheelmap.POIs.WEBSITE);
		final int wheelchairColumn = inserter.getColumnIndex(Wheelmap.POIs.WHEELCHAIR);
		final int wheelchairDescColumn = inserter.getColumnIndex( Wheelmap.POIs.WHEELCHAIR_DESC);
		final int categoryIdColumn = inserter.getColumnIndex( Wheelmap.POIs.CATEGORY_ID );
		final int categoryIdentifierColumn = inserter.getColumnIndex( Wheelmap.POIs.CATEGORY_IDENTIFIER);
		final int nodetypeIdColumn = inserter.getColumnIndex( Wheelmap.POIs.NODETYPE_ID);
		final int nodetypeIdentifierColumn = inserter.getColumnIndex( Wheelmap.POIs.NODETYPE_IDENTIFIER);

		final int sinLatColumn = inserter.getColumnIndex(Wheelmap.POIs.SIN_LAT_RAD);
		final int cosLatColumn = inserter.getColumnIndex(Wheelmap.POIs.COS_LAT_RAD);
		final int sinLonColumn = inserter.getColumnIndex(Wheelmap.POIs.SIN_LON_RAD);
		final int cosLonColumn = inserter.getColumnIndex(Wheelmap.POIs.COS_LON_RAD);
		final int updateColumn = inserter.getColumnIndex(Wheelmap.POIs.UPDATE_TAG );

		switch (match) {
		case POIS:{
			int count = 0;
			db.beginTransaction();
			int i;
			try {
				for( i = 0; i < valuesArray.length; i++ ) {
					inserter.prepareForInsert();
					preCalculateLatLon(valuesArray[i]);

					long wmId = valuesArray[i].getAsLong( Wheelmap.POIs.WM_ID );
					inserter.bind( wmIdColumn, wmId );
					String name = valuesArray[i].getAsString( Wheelmap.POIs.NAME );
					inserter.bind( nameColumn, name );
					double lat = valuesArray[i].getAsDouble( Wheelmap.POIs.COORD_LAT );
					inserter.bind( latColumn, lat );
					double lon = valuesArray[i].getAsDouble( Wheelmap.POIs.COORD_LON );
					inserter.bind( lonColumn, lon );
					String street = valuesArray[i].getAsString( Wheelmap.POIs.STREET );
					inserter.bind( streetColumn, street );
					String houseNum = valuesArray[i].getAsString( Wheelmap.POIs.HOUSE_NUM );
					inserter.bind( houseNumColumn, houseNum );
					String postCode = valuesArray[i].getAsString( Wheelmap.POIs.POSTCODE );
					inserter.bind( postcodeColumn, postCode );
					String city = valuesArray[i].getAsString( Wheelmap.POIs.CITY );
					inserter.bind( cityColumn, city );
					String phone = valuesArray[i].getAsString( Wheelmap.POIs.PHONE );
					inserter.bind( phoneColumn, phone );
					String website = valuesArray[i].getAsString( Wheelmap.POIs.WEBSITE );
					inserter.bind( websiteColumn, website );
					int wheelchair = valuesArray[i].getAsInteger( Wheelmap.POIs.WHEELCHAIR );
					inserter.bind( wheelchairColumn, wheelchair);
					String wheelchairDesc = valuesArray[i].getAsString( Wheelmap.POIs.WHEELCHAIR_DESC);
					inserter.bind( wheelchairDescColumn, wheelchairDesc );
					int categoryId = valuesArray[i].getAsInteger( Wheelmap.POIs.CATEGORY_ID );
					inserter.bind( categoryIdColumn, categoryId );
					String categoryIdentifier = valuesArray[i].getAsString( Wheelmap.POIs.CATEGORY_IDENTIFIER);
					inserter.bind( categoryIdentifierColumn, categoryIdentifier);
					int nodetypeId = valuesArray[i].getAsInteger( Wheelmap.POIs.NODETYPE_ID );
					inserter.bind( nodetypeIdColumn, nodetypeId );
					String nodetypeIdentifier = valuesArray[i].getAsString( Wheelmap.POIs.NODETYPE_IDENTIFIER);
					inserter.bind( nodetypeIdentifierColumn, nodetypeIdentifier );
					double sinLat = valuesArray[i].getAsDouble( Wheelmap.POIs.SIN_LAT_RAD );
					inserter.bind( sinLatColumn, sinLat );
					double cosLat = valuesArray[i].getAsDouble( Wheelmap.POIs.COS_LAT_RAD );
					inserter.bind( cosLatColumn, cosLat );
					double sinLon = valuesArray[i].getAsDouble( Wheelmap.POIs.SIN_LON_RAD );
					inserter.bind( sinLonColumn, sinLon );
					double cosLon = valuesArray[i].getAsDouble( Wheelmap.POIs.COS_LON_RAD );
					inserter.bind( cosLonColumn, cosLon );
					int update = valuesArray[i].getAsInteger( Wheelmap.POIs.UPDATE_TAG );
					inserter.bind( updateColumn, update );

					long rowId = inserter.execute();

					if (rowId > 0) {
						Uri placeUri = ContentUris.withAppendedId(
								Wheelmap.POIs.CONTENT_URI_POI_ID, rowId);
						getContext().getContentResolver().notifyChange(placeUri, null);
					}
					count++;	
				}
				db.setTransactionSuccessful();
			}
			finally {
				db.endTransaction();
				inserter.close();
			}
			getContext().getContentResolver().notifyChange( POIs.CONTENT_URI_POI_SORTED, null );
			getContext().getContentResolver().notifyChange( POIs.CONTENT_URI, null );
			return count;

		}
		default:{
			throw new IllegalArgumentException( "Unknown URI - POIS supported. " + uri );
		}

		}
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(Wheelmap.AUTHORITY, "pois", POIS);
		sUriMatcher.addURI(Wheelmap.AUTHORITY, "poi_id/#", POI_ID);
		sUriMatcher.addURI(Wheelmap.AUTHORITY, "poissorted", POIS_SORTED);

		// POIs
		sPOIsProjectionMap = new HashMap<String, String>();
		sPOIsProjectionMap.put(POIs._ID, POIs._ID);
		sPOIsProjectionMap.put(POIs.WM_ID, POIs.WM_ID);
		sPOIsProjectionMap.put(POIs.NAME, POIs.NAME);
		sPOIsProjectionMap.put(POIs.COORD_LAT, POIs.COORD_LAT);
		sPOIsProjectionMap.put(POIs.COORD_LON, POIs.COORD_LON);
		sPOIsProjectionMap.put(POIs.STREET, POIs.STREET);
		sPOIsProjectionMap.put(POIs.HOUSE_NUM, POIs.HOUSE_NUM);
		sPOIsProjectionMap.put(POIs.POSTCODE, POIs.POSTCODE);
		sPOIsProjectionMap.put(POIs.CITY, POIs.CITY);
		sPOIsProjectionMap.put(POIs.PHONE, POIs.PHONE);
		sPOIsProjectionMap.put(POIs.WEBSITE, POIs.WEBSITE);
		sPOIsProjectionMap.put(POIs.WHEELCHAIR, POIs.WHEELCHAIR);
		sPOIsProjectionMap.put(POIs.WHEELCHAIR_DESC, POIs.WHEELCHAIR_DESC);
		sPOIsProjectionMap.put(POIs.CATEGORY_ID, POIs.CATEGORY_ID);
		sPOIsProjectionMap.put(POIs.CATEGORY_IDENTIFIER, POIs.CATEGORY_IDENTIFIER);
		sPOIsProjectionMap.put(POIs.NODETYPE_ID, POIs.NODETYPE_ID);
		sPOIsProjectionMap.put(POIs.NODETYPE_IDENTIFIER, POIs.NODETYPE_IDENTIFIER);
		sPOIsProjectionMap.put(POIs.UPDATE_TAG, POIs.UPDATE_TAG );
		sPOIsProjectionMap.put(POIs.UPDATE_TIMESTAMP, POIs.UPDATE_TIMESTAMP );

	}
}
