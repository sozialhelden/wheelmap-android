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
import android.text.TextUtils;

import java.util.HashMap;
import java.util.List;

import de.akquinet.android.androlog.Log;

public class POIsProvider extends ContentProvider {

    private static final String TAG = POIsProvider.class.getSimpleName();

    private static final UriMatcher sUriMatcher;

    private static HashMap<String, String> sPOIsProjectionMap;

    private static final int POIS_ALL = 0;

    private static final int POIS_RETRIEVED = 1;

    private static final int POIS_RETRIEVED_ID = 2;

    private static final int POIS_COPY = 3;

    private static final int POIS_COPY_ID = 4;

    private static final int POIS_TMP = 5;

    private static final String DATABASE_NAME = "wheelmap.db";

    private static final int DATABASE_VERSION = 15;

    private static final String POIS_TABLE_NAME = "pois";

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
            db.execSQL("CREATE TABLE " + POIS_TABLE_NAME + " ("
                    // @formatter:off
                    + POIs._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + POIs.WM_ID + " VARCHAR(20), "
                    + POIs.NAME + " TEXT,"

                    + POIs.CATEGORY_ID + " INTEGER, "
                    + POIs.CATEGORY_IDENTIFIER + " TEXT, "
                    + POIs.NODETYPE_ID + " INTEGER, "
                    + POIs.NODETYPE_IDENTIFIER + " TEXT, "
                    + POIs.ICON + " ICON, "

                    + POIs.LATITUDE + " VARCHAR(15),"
                    + POIs.LONGITUDE + " VARCHAR(15),"

                    + POIs.STREET + " TEXT,"
                    + POIs.HOUSE_NUM + " TEXT,"
                    + POIs.POSTCODE + " TEXT,"
                    + POIs.CITY + " TEXT,"

                    + POIs.PHONE + " TEXT, "
                    + POIs.WEBSITE + " TEXT, "

                    + POIs.WHEELCHAIR + " NUMERIC, "
                    + POIs.WHEELCHAIR_TOILET + " NUMERIC, "
                    + POIs.DESCRIPTION + " TEXT,"

                    + POIs.COS_LAT_RAD + " NUMERIC,"
                    + POIs.SIN_LAT_RAD + " NUMERIC,"
                    + POIs.COS_LON_RAD + " NUMERIC,"
                    + POIs.SIN_LON_RAD + " NUMERIC,"

                    + POIs.TAG + " NUMERIC, "
                    + POIs.STATE + " NUMERIC, "
                    + POIs.DIRTY + " NUMERIC, "
                    + POIs.STORE_TIMESTAMP + " NUMERIC"


                    + POIs.PHOTO_ID + "NUMERIC"
                    + POIs.TAKEN_ON + "NUMERIC"
                    + POIs.TYPE + "TEXT"
                    + POIs.WIDTH + "NUMERIC"
                    + POIs.HEIGHT + "NUMERIC"
                    + POIs.URL + "TEXT)");
            // @formatter:on

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + POIS_TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case POIS_ALL:
            case POIS_RETRIEVED:
            case POIS_COPY:
            case POIS_TMP:
                return POIs.CONTENT_TYPE_DIR;
            case POIS_RETRIEVED_ID:
            case POIS_COPY_ID:
                return POIs.CONTENT_TYPE_ITEM;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        Log.v(TAG, "POIsProvider.delete: uri=" + uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        String whereClause = calcWhereClause(uri);

        int count;
        switch (match) {
            case POIS_ALL:
            case POIS_RETRIEVED:
            case POIS_RETRIEVED_ID:
            case POIS_COPY:
            case POIS_COPY_ID:
            case POIS_TMP:
                whereClause = concatenateWhere(whereClause, where);
                Log.d(TAG, "whereClause = " + whereClause + " whereArgs = " + whereArgs);
                count = db.delete(POIS_TABLE_NAME, whereClause, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        notifyCheck(uri);

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
            String[] whereArgs) {
        Log.v(TAG, "POIsProvider.update: uri=" + uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        int match = sUriMatcher.match(uri);
        preCalculateLatLon(values);
        String whereClause = calcWhereClause(uri);

        switch (match) {
            case POIS_ALL:
            case POIS_RETRIEVED:
            case POIS_RETRIEVED_ID:
            case POIS_COPY:
            case POIS_COPY_ID:
            case POIS_TMP:
                whereClause = concatenateWhere(whereClause, where);

                count = db.update(POIS_TABLE_NAME, values, whereClause, whereArgs);

                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        notifyCheck(uri);
        return count;
    }

    private void preCalculateLatLon(ContentValues values) {
        // pre calcutes sin and cos values of lat/lon
        // see wikipage
        // https://github.com/sozialhelden/wheelmap-android/wiki/Sqlite,-Distance-calculations
        if (values.containsKey(POIs.LATITUDE)) {
            double lat = values.getAsDouble(POIs.LATITUDE);
            double sin_lat_rad = Math.sin(Math.toRadians(lat));
            double cos_lat_rad = Math.cos(Math.toRadians(lat));
            values.put(POIs.COS_LAT_RAD, cos_lat_rad);
            values.put(POIs.SIN_LAT_RAD, sin_lat_rad);
        }

        if (values.containsKey(POIs.LONGITUDE)) {
            double lon = values.getAsDouble(POIs.LONGITUDE);
            double sin_lon_rad = Math.sin(Math.toRadians(lon));
            double cos_lon_rad = Math.cos(Math.toRadians(lon));
            values.put(POIs.COS_LON_RAD, cos_lon_rad);
            values.put(POIs.SIN_LON_RAD, sin_lon_rad);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.v(TAG, "POIsProvider.insert: uri=" + uri);
        int match = sUriMatcher.match(uri);

        switch (match) {
            case POIS_ALL:
                throw new IllegalArgumentException(
                        "You need to insert either in POIS_RETRIEVED or in POIS_COPY");
            case POIS_RETRIEVED:
                values.put(POIs.TAG, POIs.TAG_RETRIEVED);
                break;
            case POIS_COPY:
                values.put(POIs.TAG, POIs.TAG_COPY);
                break;
            case POIS_TMP:
                values.put(POIs.TAG, POIs.TAG_TMP);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Uri resultUri = insertValuesInt(uri, values);
        notifyCheck(uri);
        return resultUri;

    }

    private Uri insertValuesInt(Uri uri, ContentValues values) {
        preCalculateLatLon(values);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(POIS_TABLE_NAME, POIs.NAME, values);
        if (rowId > 0) {
            Uri placeUri = ContentUris.withAppendedId(uri, rowId);
            return placeUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        Log.v(TAG, "POIsProvider.query: uri=" + uri);

        int match = sUriMatcher.match(uri);
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        Cursor c = null;
        if (!getBooleanQueryParameter(uri, POIs.PARAMETER_SORTED, false)) {
            qb.setTables(POIS_TABLE_NAME);
            qb.setProjectionMap(sPOIsProjectionMap);

            switch (match) {
                case POIS_ALL:
                case POIS_RETRIEVED:
                case POIS_RETRIEVED_ID:
                case POIS_COPY:
                case POIS_COPY_ID:
                case POIS_TMP:
                    String whereClause = calcWhereClause(uri);
                    if (whereClause != null) {
                        qb.appendWhere(whereClause);
                    }
                    c = qb.query(db, projection, selection, selectionArgs, null,
                            null, sortOrder);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);

            }

        } else {
            switch (match) {
                case POIS_RETRIEVED:
                    String whereClause = concatenateWhere(selection,
                            calcWhereClause(uri));
                    Location l = extractSortByLocation(uri);
                    String query = buildDistanceQuery(l.latitude, l.longitude,
                            whereClause);
                    Log.v(TAG, "query: sql = " + query);
                    c = db.rawQuery(query, null);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }

        }

        Log.d(TAG, "setNotificationUri uri = " + uri);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] valuesArray) {
        Log.v(TAG, "POISProvider.bulkInsert: uri=" + uri);

        try{
            if(valuesArray[0].size() <= 3){
                   return bulkInsertPhoto(uri,valuesArray);
            }
        }catch(Exception ex){}

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        DatabaseUtils.InsertHelper inserter = new DatabaseUtils.InsertHelper(
                db, POIS_TABLE_NAME);

        final int wmIdColumn = inserter.getColumnIndex(POIs.WM_ID);
        final int nameColumn = inserter.getColumnIndex(POIs.NAME);
        final int latColumn = inserter.getColumnIndex(POIs.LATITUDE);
        final int lonColumn = inserter.getColumnIndex(POIs.LONGITUDE);
        final int streetColumn = inserter.getColumnIndex(POIs.STREET);
        final int houseNumColumn = inserter.getColumnIndex(POIs.HOUSE_NUM);
        final int postcodeColumn = inserter.getColumnIndex(POIs.POSTCODE);
        final int cityColumn = inserter.getColumnIndex(POIs.CITY);
        final int phoneColumn = inserter.getColumnIndex(POIs.PHONE);
        final int websiteColumn = inserter.getColumnIndex(POIs.WEBSITE);
        final int wheelchairColumn = inserter.getColumnIndex(POIs.WHEELCHAIR);
        final int wheelchairToiletColumn = inserter.getColumnIndex(POIs.WHEELCHAIR_TOILET);
        final int descriptionColumn = inserter.getColumnIndex(POIs.DESCRIPTION);
        final int iconColumn = inserter.getColumnIndex(POIs.ICON);
        final int categoryIdColumn = inserter.getColumnIndex(POIs.CATEGORY_ID);
        final int categoryIdentifierColumn = inserter.getColumnIndex(POIs.CATEGORY_IDENTIFIER);
        final int nodeTypeIdColumn = inserter.getColumnIndex(POIs.NODETYPE_ID);
        final int nodeTypeIdentifierColumn = inserter.getColumnIndex(POIs.NODETYPE_IDENTIFIER);
        final int sinLatColumn = inserter.getColumnIndex(POIs.SIN_LAT_RAD);
        final int cosLatColumn = inserter.getColumnIndex(POIs.COS_LAT_RAD);
        final int sinLonColumn = inserter.getColumnIndex(POIs.SIN_LON_RAD);
        final int cosLonColumn = inserter.getColumnIndex(POIs.COS_LON_RAD);
        final int tagColumn = inserter.getColumnIndex(POIs.TAG);

        switch (match) {
            case POIS_RETRIEVED: {
                int count = 0;
                db.beginTransaction();
                int i;
                try {
                    for (i = 0; i < valuesArray.length; i++) {
                        ContentValues values = valuesArray[i];
                        preCalculateLatLon(values);
                        inserter.prepareForInsert();

                        long wmId = values.getAsLong(POIs.WM_ID);
                        inserter.bind(wmIdColumn, wmId);
                        String name = values.getAsString(POIs.NAME);
                        inserter.bind(nameColumn, name);
                        double lat = values.getAsDouble(POIs.LATITUDE);
                        inserter.bind(latColumn, lat);
                        double lon = values.getAsDouble(POIs.LONGITUDE);
                        inserter.bind(lonColumn, lon);
                        String street = values.getAsString(POIs.STREET);
                        inserter.bind(streetColumn, street);
                        String houseNum = values.getAsString(POIs.HOUSE_NUM);
                        inserter.bind(houseNumColumn, houseNum);
                        String postCode = values.getAsString(POIs.POSTCODE);
                        inserter.bind(postcodeColumn, postCode);
                        String city = values.getAsString(POIs.CITY);
                        inserter.bind(cityColumn, city);
                        String phone = values.getAsString(POIs.PHONE);
                        inserter.bind(phoneColumn, phone);
                        String website = values.getAsString(POIs.WEBSITE);
                        inserter.bind(websiteColumn, website);
                        int wheelchair = values.getAsInteger(POIs.WHEELCHAIR);
                        inserter.bind(wheelchairColumn, wheelchair);
                        int wheelchairToilet = values.getAsInteger(POIs.WHEELCHAIR_TOILET);
                        inserter.bind(wheelchairToiletColumn, wheelchairToilet);
                        String description = values.getAsString(POIs.DESCRIPTION);
                        inserter.bind(descriptionColumn, description);
                        String icon = values.getAsString(POIs.ICON);
                        inserter.bind(iconColumn, icon);

                        Integer categoryId = values.getAsInteger(POIs.CATEGORY_ID);
                        if (categoryId != null) {
                            inserter.bind(categoryIdColumn, categoryId);
                        }
                        String categoryIdentifier = values.getAsString(POIs.CATEGORY_IDENTIFIER);
                        inserter.bind(categoryIdentifierColumn, categoryIdentifier);

                        Integer nodetypeId = values.getAsInteger(POIs.NODETYPE_ID);
                        if (nodetypeId != null) {
                            inserter.bind(nodeTypeIdColumn, nodetypeId);
                        }
                        String nodetypeIdentifier = values.getAsString(POIs.NODETYPE_IDENTIFIER);
                        inserter.bind(nodeTypeIdentifierColumn, nodetypeIdentifier);

                        double sinLat = values.getAsDouble(POIs.SIN_LAT_RAD);
                        inserter.bind(sinLatColumn, sinLat);
                        double cosLat = values.getAsDouble(POIs.COS_LAT_RAD);
                        inserter.bind(cosLatColumn, cosLat);
                        double sinLon = values.getAsDouble(POIs.SIN_LON_RAD);
                        inserter.bind(sinLonColumn, sinLon);
                        double cosLon = values.getAsDouble(POIs.COS_LON_RAD);
                        inserter.bind(cosLonColumn, cosLon);
                        int tag = values.getAsInteger(POIs.TAG);
                        inserter.bind(tagColumn, tag);

                        long rowId = inserter.execute();

                        if (rowId > 0) {
                            // we ignore this here - notification makes no sense as
                            // the record was just inserted
                        }
                        count++;
                    }
                    db.setTransactionSuccessful();
                }catch(Exception ex){
                   Log.d(ex.getMessage());
                }
                finally {
                    db.endTransaction();
                    inserter.close();
                }

                notifyCheck(uri);
                return count;

            }
            default: {
                throw new IllegalArgumentException("Unknown URI - only "
                        + POIs.CONTENT_URI_RETRIEVED + " supported. " + uri);
            }

        }
    }

    public int bulkInsertPhoto(Uri uri, ContentValues[] valuesArray) {
        return 0;
    }


    private static class Location {

        double latitude;

        double longitude;
    }

    private Location extractSortByLocation(Uri uri) {

        String latitude = uri.getQueryParameter(POIs.PARAMETER_LATITUDE);
        String longitude = uri.getQueryParameter(POIs.PARAMETER_LONGITUDE);
        if (latitude == null || longitude == null) {
            return null;
        }

        Location l = new Location();
        try {
            l.latitude = Double.parseDouble(latitude);
            l.longitude = Double.parseDouble(longitude);
        } catch (NumberFormatException e) {
            l = null;
        }

        Log.v(TAG, "extractSortByLocation: latitude = " + latitude
                + " longitude = " + longitude);

        return l;
    }

    private String calcWhereClause(Uri uri) {
        Log.v(TAG, "calcWhereClause: uri = " + uri);

        String idWhere = null;
        int match = sUriMatcher.match(uri);
        if (match == POIS_RETRIEVED_ID || match == POIS_COPY_ID) {
            long id = ContentUris.parseId(uri);
            if (id != -1) {
                idWhere = POIs._ID + "=" + id;
            }
        }

        List<String> tagList = uri.getPathSegments();
        String tagWhere = null;
        if (tagList.size() > 0) {
            String tag = tagList.get(0);
            if (tag.equals(POIs.PATH_RETRIEVED)) {
                tagWhere = POIs.TAG + "=" + POIs.TAG_RETRIEVED;
            } else if (tag.equals(POIs.PATH_COPY)) {
                tagWhere = POIs.TAG + "=" + POIs.TAG_COPY;
            } else if (tag.equals(POIs.PATH_TMP)) {
                tagWhere = POIs.TAG + "=" + POIs.TAG_TMP;
            }
        }

        String result = concatenateWhere(tagWhere, idWhere);
        Log.v(TAG, "whereClause result = " + result);

        return result;
    }

    private void notifyCheck(Uri uri) {
        if (getBooleanQueryParameter(uri, POIs.PARAMETER_NONOTIFY, false)) {
            return;
        }

        Log.v(TAG, "notifyCheck: sending notification to uri = " + uri);
        getContext().getContentResolver().notifyChange(uri, null);
    }

    private String buildDistanceQuery(double latitude, double longitude,
            String whereParams) {
        double sin_lat_rad = Math.sin(latitude * Math.PI / 180);
        double sin_lon_rad = Math.sin(longitude * Math.PI / 180);
        double cos_lat_rad = Math.cos(latitude * Math.PI / 180);
        double cos_lon_rad = Math.cos(longitude * Math.PI / 180);
        StringBuilder a = new StringBuilder("SELECT *,");

        // @formatter:off
        a.append("(").append(sin_lat_rad).append("*\"sin_lat_rad\"+").append(cos_lat_rad)
                .append("*\"cos_lat_rad\"*");
        a.append("(").append(cos_lon_rad).append("*\"cos_lon_rad\"+").append(sin_lon_rad)
                .append("*\"sin_lon_rad\"))");
        a.append(" AS ").append("\"distance_acos\"");
        a.append(" FROM ").append(POIS_TABLE_NAME);
        if (whereParams != null) {
            if (whereParams.trim().length() > 0) {
                a.append(" WHERE ");
                a.append(whereParams);
            }
        }
        a.append(" ORDER BY \"distance_acos\" DESC");

        return a.toString();
    }

    private boolean getBooleanQueryParameter(Uri uri, String key,
            boolean defaultValue) {
        String flag = uri.getQueryParameter(key);
        if (flag == null) {
            return defaultValue;
        }
        flag = flag.toLowerCase();
        return (!"false".equals(flag) && !"0".equals(flag));
    }

    private String concatenateWhere(String a, String b) {
        boolean aIsEmpty = TextUtils.isEmpty(a);
        boolean bIsEmpty = TextUtils.isEmpty(b);

        if (aIsEmpty && bIsEmpty) {
            return null;
        } else if (aIsEmpty) {
            return b;
        } else if (bIsEmpty) {
            return a;
        } else {
            return "(" + a + ") AND (" + b + ")";
        }
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI("*", POIs.PATH_ALL, POIS_ALL);
        sUriMatcher.addURI("*", POIs.PATH_RETRIEVED,
                POIS_RETRIEVED);
        sUriMatcher.addURI("*", POIs.PATH_RETRIEVED + "/#",
                POIS_RETRIEVED_ID);
        sUriMatcher.addURI("*", POIs.PATH_COPY, POIS_COPY);
        sUriMatcher.addURI("*", POIs.PATH_COPY + "/#",
                POIS_COPY_ID);
        sUriMatcher.addURI("*", POIs.PATH_TMP, POIS_TMP);

        // POIs
        sPOIsProjectionMap = new HashMap<String, String>();
        sPOIsProjectionMap.put(POIs._ID, POIs._ID);
        sPOIsProjectionMap.put(POIs.WM_ID, POIs.WM_ID);
        sPOIsProjectionMap.put(POIs.NAME, POIs.NAME);
        sPOIsProjectionMap.put(POIs.LONGITUDE, POIs.LATITUDE);
        sPOIsProjectionMap.put(POIs.LATITUDE, POIs.LONGITUDE);
        sPOIsProjectionMap.put(POIs.STREET, POIs.STREET);
        sPOIsProjectionMap.put(POIs.HOUSE_NUM, POIs.HOUSE_NUM);
        sPOIsProjectionMap.put(POIs.POSTCODE, POIs.POSTCODE);
        sPOIsProjectionMap.put(POIs.CITY, POIs.CITY);
        sPOIsProjectionMap.put(POIs.PHONE, POIs.PHONE);
        sPOIsProjectionMap.put(POIs.ICON, POIs.ICON);
        sPOIsProjectionMap.put(POIs.WEBSITE, POIs.WEBSITE);
        sPOIsProjectionMap.put(POIs.WHEELCHAIR, POIs.WHEELCHAIR);
        sPOIsProjectionMap.put(POIs.WHEELCHAIR_TOILET, POIs.WHEELCHAIR_TOILET);
        sPOIsProjectionMap.put(POIs.DESCRIPTION, POIs.DESCRIPTION);
        sPOIsProjectionMap.put(POIs.CATEGORY_ID, POIs.CATEGORY_ID);
        sPOIsProjectionMap.put(POIs.CATEGORY_IDENTIFIER, POIs.CATEGORY_IDENTIFIER);
        sPOIsProjectionMap.put(POIs.NODETYPE_ID, POIs.NODETYPE_ID);
        sPOIsProjectionMap.put(POIs.NODETYPE_IDENTIFIER, POIs.NODETYPE_IDENTIFIER);
        sPOIsProjectionMap.put(POIs.TAG, POIs.TAG);
        sPOIsProjectionMap.put(POIs.STATE, POIs.STATE);
        sPOIsProjectionMap.put(POIs.DIRTY, POIs.DIRTY);
        sPOIsProjectionMap.put(POIs.STORE_TIMESTAMP, POIs.STORE_TIMESTAMP);

    }
}
