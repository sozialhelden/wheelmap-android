package org.wheelmap.android.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.wheelmap.android.model.MapFileInfo.MapFileInfos;

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
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class MapFileInfoProvider extends ContentProvider {
	private final static String TAG = "mapfileinfoprovider";

	private static final UriMatcher sUriMatcher;
	public static HashMap<String, String> dirsProjectionMap;
	public static HashMap<String, String> filesProjectionMap;

	/**
	 * this is suitable for use by insert/update/delete/query and may be passed
	 * as a method call parameter. Only insert/update/delete/query should call
	 * .clear() on it
	 */
	private final ContentValues mValues = new ContentValues();

	public static final int DIRS = 1;
	public static final int FILES = 2;
	public static final int DIRSNFILES = 3;

	public static final String ID = BaseColumns._ID;
	public static final String VALUE = "value";

	private static final String DATABASE_NAME = "mapfileinfo.db";
	private static final int DATABASE_VERSION = 5;
	private static final String ENTITIES_TABLE_NAME = "files";

	private DatabaseHelper mOpenHelper;

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + ENTITIES_TABLE_NAME + " ("
					+ MapFileInfos._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ MapFileInfos.SCREEN_NAME + " TEXT, " + MapFileInfos.NAME
					+ " TEXT, " + MapFileInfos.PARENT_NAME + " TEXT, "
					+ MapFileInfos.TYPE + " INTEGER, "
					+ MapFileInfos.REMOTE_NAME + " TEXT, "
					+ MapFileInfos.REMOTE_PARENT_NAME + " TEXT, "
					+ MapFileInfos.REMOTE_TIMESTAMP + " STRING, "
					+ MapFileInfos.REMOTE_SIZE + " NUMBER, "
					+ MapFileInfos.VERSION + " TEXT, "
					+ MapFileInfos.LOCAL_TIMESTAMP + " STRING, "
					+ MapFileInfos.LOCAL_AVAILABLE + " NUMBER DEFAULT \'"
					+ MapFileInfo.FILE_NOT_LOCAL + "\',"
					+ MapFileInfos.UPDATE_TAG + " NUMBER )");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + ENTITIES_TABLE_NAME);
			onCreate(db);
		}
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case DIRS:
			return MapFileInfos.DIR_TYPE;
		case FILES:
			return MapFileInfos.FILE_TYPE;
		case DIRSNFILES:
			return MapFileInfos.DIRNFILE_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case DIRS:
			count = db.delete(ENTITIES_TABLE_NAME,
					MapFileInfos.TYPE
							+ "="
							+ DIRS
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);

			break;

		case FILES:
			count = db.delete(
					ENTITIES_TABLE_NAME,
					MapFileInfos.TYPE
							+ "="
							+ FILES
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case DIRSNFILES:
			count = db.delete(ENTITIES_TABLE_NAME,
					(!TextUtils.isEmpty(where) ? '(' + where + ')' : ""),
					whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		getContext().getContentResolver().notifyChange(
				MapFileInfos.CONTENT_URI_DIRSNFILES, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case DIRS:
			count = db.update(ENTITIES_TABLE_NAME, values,
					MapFileInfos.TYPE
							+ "="
							+ DIRS
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case FILES:
			count = db.update(
					ENTITIES_TABLE_NAME,
					values,
					MapFileInfos.TYPE
							+ "="
							+ FILES
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case DIRSNFILES:
			count = db.update(ENTITIES_TABLE_NAME, values,
					(!TextUtils.isEmpty(where) ? '(' + where + ')' : ""),
					whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		getContext().getContentResolver().notifyChange(
				MapFileInfos.CONTENT_URI_DIRSNFILES, null);
		return count;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		int match = sUriMatcher.match(uri);

		// Log.v(TAG, "PlacessProvider.query: url=" + uri + ", match is " +
		// match);

		switch (match) {
		case DIRS: {
			mValues.clear();
			mValues.putAll(initialValues);
			mValues.put(MapFileInfos.TYPE, DIRS);
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			long rowId = db.insert(ENTITIES_TABLE_NAME, MapFileInfos.NAME,
					mValues);
			if (rowId > 0) {
				Uri placeUri = ContentUris.withAppendedId(
						MapFileInfos.CONTENT_URI_DIRS, rowId);
				getContext().getContentResolver().notifyChange(placeUri, null);
				getContext().getContentResolver().notifyChange(
						MapFileInfos.CONTENT_URI_DIRSNFILES, null);
				return placeUri;
			}
			throw new SQLException("Failed to insert row into " + uri);
		}
		case FILES: {
			mValues.clear();
			mValues.putAll(initialValues);
			mValues.put(MapFileInfos.TYPE, FILES);
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			long rowId = db.insert(ENTITIES_TABLE_NAME, MapFileInfos.NAME,
					mValues);
			if (rowId > 0) {
				Uri placeUri = ContentUris.withAppendedId(
						MapFileInfos.CONTENT_URI_FILES, rowId);
				getContext().getContentResolver().notifyChange(placeUri, null);
				getContext().getContentResolver().notifyChange(
						MapFileInfos.CONTENT_URI_DIRSNFILES, null);
				return placeUri;
			}
			throw new SQLException("Failed to insert row into " + uri);
		}
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
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		int match = sUriMatcher.match(uri);
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = MapFileInfos.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}
		// If no sort order is specified use the default
		switch (match) {
		case DIRS:
			qb.setTables(ENTITIES_TABLE_NAME);
			qb.setProjectionMap(dirsProjectionMap);
			qb.appendWhere(" (" + MapFileInfos.TYPE + "=" + DIRS + ") ");
			break;
		case FILES:
			qb.setTables(ENTITIES_TABLE_NAME);
			qb.setProjectionMap(filesProjectionMap);
			qb.appendWhere(" (" + MapFileInfos.TYPE + "=" + FILES + ") ");
			break;
		case DIRSNFILES:
			qb.setTables(ENTITIES_TABLE_NAME);
			qb.setProjectionMap(filesProjectionMap);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Get the database and run the query
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, orderBy);

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
		sUriMatcher.addURI(MapFileInfo.AUTHORITY, "dirs", DIRS);
		sUriMatcher.addURI(MapFileInfo.AUTHORITY, "files", FILES);
		sUriMatcher.addURI(MapFileInfo.AUTHORITY, "dirsnfiles", DIRSNFILES);

		dirsProjectionMap = new HashMap<String, String>();
		dirsProjectionMap.put(MapFileInfos._ID, MapFileInfos._ID);
		dirsProjectionMap.put(MapFileInfos.SCREEN_NAME,
				MapFileInfos.SCREEN_NAME);
		dirsProjectionMap.put(MapFileInfos.NAME, MapFileInfos.NAME);
		dirsProjectionMap.put(MapFileInfos.PARENT_NAME,
				MapFileInfos.PARENT_NAME);
		dirsProjectionMap.put(MapFileInfos.TYPE, MapFileInfos.TYPE);
		dirsProjectionMap.put(MapFileInfos.REMOTE_NAME,
				MapFileInfos.REMOTE_NAME);
		dirsProjectionMap.put(MapFileInfos.REMOTE_PARENT_NAME,
				MapFileInfos.REMOTE_PARENT_NAME);
		dirsProjectionMap.put(MapFileInfos.REMOTE_TIMESTAMP,
				MapFileInfos.REMOTE_TIMESTAMP);
		dirsProjectionMap.put(MapFileInfos.UPDATE_TAG, MapFileInfos.UPDATE_TAG);

		filesProjectionMap = new HashMap<String, String>();
		filesProjectionMap.put(MapFileInfos._ID, MapFileInfos._ID);
		filesProjectionMap.put(MapFileInfos.SCREEN_NAME,
				MapFileInfos.SCREEN_NAME);
		filesProjectionMap.put(MapFileInfos.NAME, MapFileInfos.NAME);
		filesProjectionMap.put(MapFileInfos.PARENT_NAME,
				MapFileInfos.PARENT_NAME);
		filesProjectionMap.put(MapFileInfos.TYPE, MapFileInfos.TYPE);
		filesProjectionMap.put(MapFileInfos.REMOTE_NAME,
				MapFileInfos.REMOTE_NAME);
		filesProjectionMap.put(MapFileInfos.REMOTE_PARENT_NAME,
				MapFileInfos.REMOTE_PARENT_NAME);
		filesProjectionMap.put(MapFileInfos.REMOTE_TIMESTAMP,
				MapFileInfos.REMOTE_TIMESTAMP);
		filesProjectionMap.put(MapFileInfos.REMOTE_SIZE,
				MapFileInfos.REMOTE_SIZE);
		filesProjectionMap.put(MapFileInfos.VERSION, MapFileInfos.VERSION);
		filesProjectionMap.put(MapFileInfos.LOCAL_TIMESTAMP,
				MapFileInfos.LOCAL_TIMESTAMP);
		filesProjectionMap.put(MapFileInfos.LOCAL_AVAILABLE,
				MapFileInfos.LOCAL_AVAILABLE);
		filesProjectionMap
				.put(MapFileInfos.UPDATE_TAG, MapFileInfos.UPDATE_TAG);

	}

}
