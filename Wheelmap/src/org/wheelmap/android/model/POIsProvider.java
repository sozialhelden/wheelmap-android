package org.wheelmap.android.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.utils.CurrentLocation;
import org.wheelmap.android.utils.CurrentLocation.LocationResult;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.location.Location;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class POIsProvider extends ContentProvider {

	private static final UriMatcher sUriMatcher;
	private static HashMap<String, String> sPOIsProjectionMap;
	
	// TODO quick hack, the Content provider has its own current location instance
	// there should be only one in the whole project ???
	private CurrentLocation mCurrentLocation;


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
	private static final int DATABASE_VERSION = 6;
	private static final String POIS_TABLE_NAME = "pois";
	
	private Location mLastLocation;
	
	private static class DistanceQueryBuilder {
		public String buildRawQuery(double longitude,double latitude) {
			 double  sin_lat_rad =  Math.sin(latitude*Math.PI/180);
			 double   sin_lon_rad =  Math.sin(longitude*Math.PI/180);
			 double   cos_lat_rad =  Math.cos(latitude*Math.PI/180);
			 double   cos_lon_rad =  Math.cos(longitude*Math.PI/180);
			 StringBuilder a = new StringBuilder("SELECT *,(");
			 a.append(sin_lat_rad);
			 a.append("*\"sin_lat_rad\"+");
			 a.append(cos_lat_rad);
			 a.append("*\"cos_lat_rad\"*(");
			 a.append(cos_lon_rad);
			 a.append("*\"cos_lon_rad\"+");
			 a.append(sin_lon_rad);
			 a.append("*\"sin_lon_rad\")) " +
			 
			 		"" +
			 		"" +
			 		"" +
			 		"AS \"distance_acos\" FROM \"pois\" ORDER BY \"distance_acos\" DESC");
			 
				 
            // TODO maybe is a Formatter better
		    //return 'SELECT *, (%(sin_lat_rad)f * "sin_lat_rad" + %(cos_lat_rad)f * "cos_lat_rad" * (%(cos_lon_rad)f * "cos_lon_rad" + %(sin_lon_rad)f * "sin_lon_rad")) AS "distance_acos" FROM "pois" GROUP BY "id" HAVING "distance_acos" < 1.25 ORDER BY "distance_acos" DESC' % {'sin_lat_rad': sin_lat_rad, "cos_lat_rad": cos_lat_rad, 'sin_lon_rad': sin_lon_rad, "cos_lon_rad": cos_lon_rad}
			 
			Log.d(TAG, "query select argument for distance " +  a.toString());

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
			// places
			db.execSQL("CREATE TABLE " + POIS_TABLE_NAME + " (" 
			        + POIs._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
					+ POIs.WM_ID + " INTEGER, " 
					+ POIs.NAME + " TEXT," 
					+ POIs.COORD_LAT + " VARCHAR(15)," 
					+ POIs.COORD_LON + " VARCHAR(15),"
					+ POIs.COS_LAT_RAD + " NUMERIC,"
					+ POIs.SIN_LAT_RAD + " NUMERIC,"
					+ POIs.COS_LON_RAD + " NUMERIC,"
					+ POIs.SIN_LON_RAD + " NUMERIC,"
					+ POIs.STREET + " TEXT," 
					+ POIs.HOUSE_NUM + " TEXT,"
					+ POIs.POSTCODE + " TEXT," 
					+ POIs.CITY + " TEXT,"
					+ POIs.PHONE + " TEXT, " 
					+ POIs.WEBSITE + " TEXT, "
					+ POIs.WHEELCHAIR + " NUMERIC, " 
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
		getContext().getContentResolver().notifyChange( POIs.CONTENT_URI_POI_SORTED, null );
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
		getContext().getContentResolver().notifyChange( POIs.CONTENT_URI_POI_SORTED, null );
		return count;
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

			// pre calcutes sin and cos values of lat/lon
			// see wikipage https://github.com/sozialhelden/wheelmap-android/wiki/Sqlite,-Distance-calculations
			if (mValues.containsKey(POIs.COORD_LAT)) {
				double lat  = mValues.getAsFloat(POIs.COORD_LAT) / (double)1E6;
				double sin_lat_rad = Math.sin(Math.toRadians(lat));
				double cos_lat_rad = Math.cos(Math.toRadians(lat));
				mValues.put(POIs.COS_LAT_RAD, cos_lat_rad);
				mValues.put(POIs.SIN_LAT_RAD, sin_lat_rad);
			}
			else {
				mValues.put(POIs.COS_LAT_RAD, 0);
				mValues.put(POIs.SIN_LAT_RAD, 0);	
			}

			if (mValues.containsKey(POIs.COORD_LON)) {
				double lon  = mValues.getAsFloat(POIs.COORD_LON)/ (double)1E6;
				double sin_lon_rad = Math.sin(Math.toRadians(lon));
				double cos_lon_rad = Math.cos(Math.toRadians(lon));
				mValues.put(POIs.COS_LON_RAD, cos_lon_rad);
				mValues.put(POIs.SIN_LON_RAD, sin_lon_rad);
			}
			else {
				mValues.put(POIs.COS_LON_RAD, 0);
				mValues.put(POIs.SIN_LON_RAD, 0);	
			}

			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			long rowId = db.insert(POIS_TABLE_NAME, POIs.NAME, mValues);
			if (rowId > 0) {
				Uri placeUri = ContentUris.withAppendedId(
						Wheelmap.POIs.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(placeUri, null);
				getContext().getContentResolver().notifyChange( POIs.CONTENT_URI_POI_SORTED, null );
				return placeUri;
			}

			throw new SQLException("Failed to insert row into " + uri);
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

	}
	
	final class MyLocationResult implements LocationResult {
    	@Override
		public void gotLocation(final Location location){
    		mLastLocation = location;
    		Log.v(TAG, "new current location" +mLastLocation.toString());

		}
    }

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		mQueryBuilder = new DistanceQueryBuilder();
		// current location
		mCurrentLocation = new CurrentLocation();
		
		MyLocationResult locationResult = new MyLocationResult();
		
		mLastLocation = new Location("");
		// Berlin
		mLastLocation.setLatitude(52.519842);
		mLastLocation.setLongitude(13.439484);
	    
		mCurrentLocation.getLocation(getContext(), locationResult);
		
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
			c = qb.query(db, projection, selection, selectionArgs, null,
					null, sortOrder);
			break;
		case POI_ID:
			qb.setTables(POIS_TABLE_NAME);
			qb.setProjectionMap(sPOIsProjectionMap);
			qb.appendWhere(" (" + POIs._ID + " = " + uri.getPathSegments().get(1) + ") ");
			c = qb.query(db, projection, selection, selectionArgs, null,
					null, sortOrder);
			break;
		case POIS_SORTED:
			// get asynchronously current location from location manager and execute request
			c = db.rawQuery(mQueryBuilder.buildRawQuery(mLastLocation.getLongitude(), mLastLocation.getLatitude()), null);
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
	public ContentProviderResult[] applyBatch(
			ArrayList<ContentProviderOperation> operations)
	throws OperationApplicationException {
		ContentProviderResult[] results = new ContentProviderResult[operations
		                                                            .size()];

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();

		try {
			int i;
			for (i = 0; i < operations.size(); i++) {
				ContentProviderOperation operation = operations.get(i);
				operation.apply(this, results, 2);
			}
			db.setTransactionSuccessful();
		} catch (SQLException e) {
		} finally {
			db.endTransaction();
		}

		return results;
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(Wheelmap.AUTHORITY, "pois", POIS);
		sUriMatcher.addURI(Wheelmap.AUTHORITY, "pois/#", POI_ID);
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

	}
}
