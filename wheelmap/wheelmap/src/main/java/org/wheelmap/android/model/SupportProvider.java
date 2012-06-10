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

import java.util.HashMap;

import org.wheelmap.android.model.Support.CategoriesContent;
import org.wheelmap.android.model.Support.LastUpdateContent;
import org.wheelmap.android.model.Support.LocalesContent;
import org.wheelmap.android.model.Support.NodeTypesContent;
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
import android.util.Log;

public class SupportProvider extends ContentProvider {

	private static final String TAG = "support";
	private static final String DATABASE_NAME = "support.db";
	private static final int DATABASE_VERSION = 6;

	private static final UriMatcher sUriMatcher;
	private static HashMap<String, String> sLocalesProjectionMap;
	private static HashMap<String, String> sCategoriesProjectionMap;
	private static HashMap<String, String> sNodeTypesProjectionMap;
	private static HashMap<String, String> sLastUpdateProjectionMap;

	private static final int LOCALES = 1;
	private static final int CATEGORIES = 2;
	private static final int NODETYPES = 3;
	private static final int LASTUPDATE = 4;

	private static final String LOCALES_TABLE_NAME = "locales";
	private static final String CATEGORIES_TABLE_NAME = "categories";
	private static final String NODETYPES_TABLE_NAME = "nodetypes";
	private static final String LASTUPDATE_TABLE_NAME = "lastupdate";

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + LASTUPDATE_TABLE_NAME + " ("
					+ LastUpdateContent._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ LastUpdateContent.DATE + " TEXT)");

			db.execSQL("CREATE TABLE " + LOCALES_TABLE_NAME + " ("
					+ LocalesContent._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ LocalesContent.LOCALE_ID + " TEXT, "
					+ LocalesContent.LOCALIZED_NAME + " TEXT)");

			db.execSQL("CREATE TABLE " + CATEGORIES_TABLE_NAME + " ("
					+ CategoriesContent._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ CategoriesContent.CATEGORY_ID + " INTEGER, "
					+ CategoriesContent.LOCALIZED_NAME + " TEXT, "
					+ CategoriesContent.IDENTIFIER + " TEXT,"
					+ CategoriesContent.SELECTED + " INTEGER )");

			db.execSQL("CREATE TABLE " + NODETYPES_TABLE_NAME + " ("
					+ NodeTypesContent._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ NodeTypesContent.NODETYPE_ID + " INTEGER, "
					+ NodeTypesContent.IDENTIFIER + " TEXT, "
					+ NodeTypesContent.ICON_URL + " TEXT, "
					+ NodeTypesContent.LOCALIZED_NAME + " TEXT, "
					+ NodeTypesContent.CATEGORY_ID + " INTEGER, "
					+ NodeTypesContent.CATEGORY_IDENTIFIER + " TEXT)");

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + LOCALES_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + CATEGORIES_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + NODETYPES_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + LASTUPDATE_TABLE_NAME );
			onCreate(db);
		}
	}

	private DatabaseHelper mOpenHelper;

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

		// Log.v(TAG, "SupportProvder.query: url=" + uri + ", match is " +
		// match);
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c;
		// If no sort order is specified use the default
		switch (match) {
		case LASTUPDATE:
			qb.setTables(LASTUPDATE_TABLE_NAME);
			qb.setProjectionMap(sLastUpdateProjectionMap);
			break;
		case LOCALES:
			qb.setTables(LOCALES_TABLE_NAME);
			qb.setProjectionMap(sLocalesProjectionMap);
			break;
		case CATEGORIES:
			qb.setTables(CATEGORIES_TABLE_NAME);
			qb.setProjectionMap(sCategoriesProjectionMap);
			break;
		case NODETYPES:
			qb.setTables(NODETYPES_TABLE_NAME);
			qb.setProjectionMap(sNodeTypesProjectionMap);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		c = qb.query(db, projection, selection, selectionArgs, null, null,
				sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case LASTUPDATE:
			return LastUpdateContent.CONTENT_TYPE;
		case LOCALES:
			return LocalesContent.CONTENT_TYPE;
		case CATEGORIES:
			return CategoriesContent.CONTENT_TYPE;
		case NODETYPES:
			return NodeTypesContent.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Log.v(TAG, "SupportProvider.insert: url=" + uri );
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		String tableName;
		String nullColumnHack;

		int match = sUriMatcher.match(uri);
		switch (match) {
		case LASTUPDATE:
			tableName = LASTUPDATE_TABLE_NAME;
			nullColumnHack = LastUpdateContent.DATE;
			break;
		case LOCALES:
			tableName = LOCALES_TABLE_NAME;
			nullColumnHack = LocalesContent.LOCALIZED_NAME;
			break;
		case CATEGORIES:
			tableName = CATEGORIES_TABLE_NAME;
			nullColumnHack = CategoriesContent.IDENTIFIER;
			break;
		case NODETYPES:
			tableName = NODETYPES_TABLE_NAME;
			nullColumnHack = NodeTypesContent.NODETYPE_ID;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		long rowId = db.insert(tableName, nullColumnHack, values);
		if (rowId < 0)
			throw new SQLException("Failed to insert row into " + uri);

		getContext().getContentResolver().notifyChange(uri, null);

		Uri placeUri = ContentUris.withAppendedId(uri, rowId);
		return placeUri;
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		// Log.v(TAG, "SupportProvider.delete: url=" + uri);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		String tableName;
		switch (sUriMatcher.match(uri)) {
		case LASTUPDATE:
			tableName = LASTUPDATE_TABLE_NAME;
			break;
		case LOCALES:
			tableName = LOCALES_TABLE_NAME;
			break;
		case CATEGORIES:
			tableName = CATEGORIES_TABLE_NAME;
			break;
		case NODETYPES:
			tableName = NODETYPES_TABLE_NAME;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		int count = db.delete(tableName, where, whereArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		// Log.v(TAG, "SupportProvider.update: url=" + uri);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		String tableName;
		switch (sUriMatcher.match(uri)) {
		case LASTUPDATE:
			tableName = LASTUPDATE_TABLE_NAME;
			break;
		case LOCALES:
			tableName = LOCALES_TABLE_NAME;
			break;
		case CATEGORIES:
			tableName = CATEGORIES_TABLE_NAME;
			break;
		case NODETYPES:
			tableName = NODETYPES_TABLE_NAME;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		int count = db.update(tableName, values, where, whereArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] valuesArray) {

		int result;
		switch (sUriMatcher.match(uri)) {
		case LOCALES:
			result = bulkInsertLocales(valuesArray);
			break;
		case CATEGORIES:
			result = bulkInsertCategories(valuesArray);
			break;
		case NODETYPES:
			result = bulkInsertNodeTypes(valuesArray);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(POIs.CONTENT_URI, null);
		return result;
	}

	private int bulkInsertLocales(ContentValues[] values) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		DatabaseUtils.InsertHelper inserter = new DatabaseUtils.InsertHelper(
				db, LOCALES_TABLE_NAME);

		final int localeIdColumn = inserter
				.getColumnIndex(LocalesContent.LOCALE_ID);
		final int localizedNameColumn = inserter
				.getColumnIndex(LocalesContent.LOCALIZED_NAME);

		int count = 0;
		db.beginTransaction();
		int i;
		try {
			for (i = 0; i < values.length; i++) {
				inserter.prepareForInsert();
				String localeId = values[i]
						.getAsString(LocalesContent.LOCALE_ID);
				inserter.bind(localeIdColumn, localeId);
				String localizedName = values[i]
						.getAsString(LocalesContent.LOCALIZED_NAME);
				inserter.bind(localizedNameColumn, localizedName);

				long rowId = inserter.execute();

				count++;
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			inserter.close();
		}
		return count;
	}
	
	private int bulkInsertCategories( ContentValues[] values) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		DatabaseUtils.InsertHelper inserter = new DatabaseUtils.InsertHelper(
				db, CATEGORIES_TABLE_NAME);
		
		final int categoryIdColumn = inserter.getColumnIndex( CategoriesContent.CATEGORY_ID );
		final int localizedNameColumn = inserter.getColumnIndex( CategoriesContent.LOCALIZED_NAME );
		final int identifierColumn = inserter.getColumnIndex( CategoriesContent.IDENTIFIER );
		final int selectedColumn = inserter.getColumnIndex( CategoriesContent.SELECTED );

		int count = 0;
		db.beginTransaction();
		int i;
		try {
			for (i = 0; i < values.length; i++) {
				inserter.prepareForInsert();
				
				int categoryId = values[i].getAsInteger( CategoriesContent.CATEGORY_ID );
				inserter.bind( categoryIdColumn, categoryId);
				
				String localizeName = values[i].getAsString( CategoriesContent.LOCALIZED_NAME );
				inserter.bind( localizedNameColumn, localizeName );
				
				String identifier = values[i].getAsString( CategoriesContent.IDENTIFIER );
				inserter.bind( identifierColumn, identifier );
				
				int selected = values[i].getAsInteger( CategoriesContent.SELECTED);
				inserter.bind( selectedColumn, selected );

				long rowId = inserter.execute();

				count++;
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			inserter.close();
		}
		return count;
	}

	private int bulkInsertNodeTypes( ContentValues[] values) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		DatabaseUtils.InsertHelper inserter = new DatabaseUtils.InsertHelper(
				db, NODETYPES_TABLE_NAME);

		final int nodeTypeIdColumn = inserter.getColumnIndex( NodeTypesContent.NODETYPE_ID );
		final int identifierColumn = inserter.getColumnIndex( NodeTypesContent.IDENTIFIER );
		final int iconUrlColumn = inserter.getColumnIndex( NodeTypesContent.ICON_URL );
		final int localizedNameColumn = inserter.getColumnIndex( NodeTypesContent.LOCALIZED_NAME );
		final int categoryIdColumn = inserter.getColumnIndex( NodeTypesContent.CATEGORY_ID );
		final int categoryIdentifierColumn = inserter.getColumnIndex( NodeTypesContent.CATEGORY_IDENTIFIER  );
		
		int count = 0;
		db.beginTransaction();
		int i;
		try {
			for (i = 0; i < values.length; i++) {
				inserter.prepareForInsert();
				int nodeTypeId = values[i].getAsInteger( NodeTypesContent.NODETYPE_ID );
				inserter.bind( nodeTypeIdColumn, nodeTypeId );
				String identifier = values[i].getAsString( NodeTypesContent.IDENTIFIER );
				inserter.bind( identifierColumn, identifier );
				String iconUrl = values[i].getAsString( NodeTypesContent.ICON_URL );
				inserter.bind( iconUrlColumn, iconUrl );
				String localizedName = values[i].getAsString( NodeTypesContent.LOCALIZED_NAME );
				inserter.bind( localizedNameColumn, localizedName );
				int categoryId = values[i].getAsInteger( NodeTypesContent.CATEGORY_ID );
				inserter.bind( categoryIdColumn, categoryId );
				String categoryIdentifier = values[i].getAsString( NodeTypesContent.CATEGORY_IDENTIFIER );
				inserter.bind( categoryIdentifierColumn, categoryIdentifier );
				
				long rowId = inserter.execute();

				count++;
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			inserter.close();
		}
		return count;
	}


	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(Support.AUTHORITY, "lastupdate", LASTUPDATE);
		sUriMatcher.addURI(Support.AUTHORITY, "locales", LOCALES);
		sUriMatcher.addURI(Support.AUTHORITY, "categories", CATEGORIES);
		sUriMatcher.addURI(Support.AUTHORITY, "nodetypes", NODETYPES);

		sLastUpdateProjectionMap = new HashMap<String, String>();
		sLastUpdateProjectionMap.put(LastUpdateContent._ID,
				LastUpdateContent._ID);
		sLastUpdateProjectionMap.put(LastUpdateContent.DATE,
				LastUpdateContent.DATE);

		sLocalesProjectionMap = new HashMap<String, String>();
		sLocalesProjectionMap.put(LocalesContent._ID, LocalesContent._ID);
		sLocalesProjectionMap.put(LocalesContent.LOCALE_ID,
				LocalesContent.LOCALE_ID);
		sLocalesProjectionMap.put(LocalesContent.LOCALIZED_NAME,
				LocalesContent.LOCALIZED_NAME);

		sCategoriesProjectionMap = new HashMap<String, String>();
		sCategoriesProjectionMap.put(CategoriesContent._ID,
				CategoriesContent._ID);
		sCategoriesProjectionMap.put(CategoriesContent.CATEGORY_ID,
				CategoriesContent.CATEGORY_ID);
		sCategoriesProjectionMap.put(CategoriesContent.LOCALIZED_NAME,
				CategoriesContent.LOCALIZED_NAME);
		sCategoriesProjectionMap.put(CategoriesContent.IDENTIFIER,
				CategoriesContent.IDENTIFIER);
		sCategoriesProjectionMap.put(CategoriesContent.SELECTED,
				CategoriesContent.SELECTED);

		sNodeTypesProjectionMap = new HashMap<String, String>();
		sNodeTypesProjectionMap.put(NodeTypesContent._ID, NodeTypesContent._ID);
		sNodeTypesProjectionMap.put(NodeTypesContent.NODETYPE_ID,
				NodeTypesContent.NODETYPE_ID);
		sNodeTypesProjectionMap.put(NodeTypesContent.IDENTIFIER,
				NodeTypesContent.IDENTIFIER);
		sNodeTypesProjectionMap.put(NodeTypesContent.ICON_URL,
				NodeTypesContent.ICON_URL);
		sNodeTypesProjectionMap.put(NodeTypesContent.LOCALIZED_NAME,
				NodeTypesContent.LOCALIZED_NAME);
		sNodeTypesProjectionMap.put(NodeTypesContent.CATEGORY_ID,
				NodeTypesContent.CATEGORY_ID);
		sNodeTypesProjectionMap.put(NodeTypesContent.CATEGORY_IDENTIFIER,
				NodeTypesContent.CATEGORY_IDENTIFIER);
	}

}
