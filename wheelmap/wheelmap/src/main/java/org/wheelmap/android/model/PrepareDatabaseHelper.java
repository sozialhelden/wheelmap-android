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

import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.mapping.node.SingleNode;
import org.wheelmap.android.model.Wheelmap.POIs;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import de.akquinet.android.androlog.Log;

public class PrepareDatabaseHelper {

    private static final String TAG = PrepareDatabaseHelper.class
            .getSimpleName();

    private static final long TIME_TO_DELETE_FOR_PENDING = 10 * 60 * 1000;

    private PrepareDatabaseHelper() {

    }

    private static void prepareCopyDefaultValues(ContentValues values,
            boolean retain) {
        values.remove(POIs._ID);
        values.remove(POIs.DISTANCE_ACOS);
        values.put(POIs.TAG, POIs.TAG_COPY);
        if (!retain) {
            values.put(POIs.STATE, POIs.STATE_UNCHANGED);
        } else {
            values.put(POIs.STATE, POIs.STATE_CHANGED);
        }
        values.put(POIs.DIRTY, POIs.CLEAN);
        long now = System.currentTimeMillis();

        Log.d(TAG, "prepareCopyDefaultValues: copy with timestamp " + now);
        values.put(POIs.STORE_TIMESTAMP, now);
    }

    public static long createCopyIfNotExists(ContentResolver resolver, long id,
            boolean retain) {
        Log.v(TAG, "createCopyIfNotExists id = " + id);
        Uri uri = Uri.withAppendedPath(POIs.CONTENT_URI_RETRIEVED,
                Long.toString(id));
        Cursor c = resolver.query(uri, POIs.PROJECTION, null, null, null);

        if (c == null || c.getCount() != 1) {
            return Extra.ID_UNKNOWN;
        }

        c.moveToFirst();
        ContentValues values = new ContentValues();
        POIHelper.copyItemToValues(c, values);
        c.close();
        return createCopyFromContentValues(resolver, values, retain);
    }

    public static long createCopyFromContentValues(ContentResolver resolver,
            ContentValues values, boolean retain) {
        ContentValues copyValues = new ContentValues(values);

        String wmId = copyValues.getAsString(POIs.WM_ID);
        long copyId = getRowIdForWMId(resolver, wmId, POIs.TAG_COPY);
        if (copyId != Extra.ID_UNKNOWN) {
            return copyId;
        }

        prepareCopyDefaultValues(copyValues, retain);
        Uri uri = resolver.insert(POIs.CONTENT_URI_COPY, copyValues);
        return ContentUris.parseId(uri);
    }

    public static long getRowIdForWMId(ContentResolver resolver, String wmId,
            int tag) {
        Log.v(TAG, "getRowIdForWMId wmId = " + wmId);
        String whereClause = "(" + POIs.WM_ID + " = ? ) AND (" + POIs.TAG
                + " = ?)";
        String[] whereValues = new String[]{wmId, Integer.toString(tag)};

        Cursor c = resolver.query(POIs.CONTENT_URI_ALL, POIs.PROJECTION,
                whereClause, whereValues, null);

        if (c == null) {
            return Extra.ID_UNKNOWN;
        }
        if (c.getCount() == 0) {
            c.close();
            return Extra.ID_UNKNOWN;
        }

        c.moveToFirst();
        long id = POIHelper.getId(c);
        c.close();
        return id;
    }

    public static void editCopy(ContentResolver resolver, long id,
            ContentValues values) {
        Log.v(TAG, "editCopy id = " + id);
        Uri uri = ContentUris.withAppendedId(POIs.CONTENT_URI_COPY, id);
        values.put(POIs.STATE, POIs.STATE_CHANGED);
        int count = resolver.update(uri, values, null, null);
        Log.d(TAG, "editCopy: edited count = " + count);
    }

    public static Cursor queryDirty(ContentResolver resolver) {
        Log.v(TAG, "queryDirty");
        String whereClause = "(" + POIs.DIRTY + " = ? ) OR ( " + POIs.DIRTY
                + " = ? )";
        String[] whereValues = new String[]{
                Integer.toString(POIs.DIRTY_STATE),
                Integer.toString(POIs.DIRTY_ALL)};

        Cursor c = resolver.query(POIs.CONTENT_URI_COPY, POIs.PROJECTION,
                whereClause, whereValues, null);
        return c;
    }

    public static void markDirtyAsClean(ContentResolver resolver, long id) {
        Log.v(TAG, "markDirtyAsClean");
        String whereClause = "(" + POIs.DIRTY + " = ? ) OR ( " + POIs.DIRTY
                + " = ?)";
        String whereValues[] = new String[]{Integer.toString(POIs.DIRTY_ALL),
                Integer.toString(POIs.DIRTY_STATE)};

        ContentValues values = new ContentValues();
        values.put(POIs.DIRTY, POIs.CLEAN);
        values.put(POIs.STORE_TIMESTAMP, System.currentTimeMillis());
        Uri uri = POIs.createNoNotify(ContentUris.withAppendedId(
                POIs.CONTENT_URI_COPY, id));
        resolver.update(uri, values, whereClause, whereValues);
    }

    public static void replayChangedCopies(ContentResolver resolver) {
        Log.v(TAG, "replayChangedCopies");
        String whereClause = POIs.STATE + " = ?";
        String[] whereValues = new String[]{Integer
                .toString(POIs.STATE_CHANGED)};

        Cursor c = resolver.query(POIs.CONTENT_URI_COPY, POIs.PROJECTION,
                whereClause, whereValues, null);
        if (c == null) {
            return;
        }

        String whereClauseTarget = POIs.WM_ID + " = ?";
        String[] whereValuesTarget = new String[]{""};

        c.moveToFirst();
        Uri uri = POIs.createNoNotify(POIs.CONTENT_URI_RETRIEVED);
        ContentValues values = new ContentValues();
        while (!c.isAfterLast()) {
            values.clear();
            whereValuesTarget[0] = POIHelper.getWMId(c);
            POIHelper.copyItemToValues(c, values);
            resolver.update(uri, values, whereClauseTarget, whereValuesTarget);
            c.moveToNext();
        }

        c.close();

        resolver.notifyChange(POIs.CONTENT_URI_RETRIEVED, null);
    }

    public static void cleanupOldCopies(ContentResolver resolver, boolean force) {
        Log.v(TAG, "cleanupOldCopies");
        long now = System.currentTimeMillis();
        String whereClause = "( " + POIs.TAG + " = ? AND  "
                + POIs.STORE_TIMESTAMP + "< ?) OR ( " + POIs.TAG + " = ? )";

        long deleteTime;
        if (!force) {
            deleteTime = now - TIME_TO_DELETE_FOR_PENDING;
        } else {
            deleteTime = now;
        }

        String[] whereValues = new String[]{Integer.toString(POIs.TAG_COPY),
                Long.toString(deleteTime), Integer.toString(POIs.TAG_TMP)};

        Uri uri = POIs.createNoNotify(POIs.CONTENT_URI_ALL);
        int count = resolver.delete(uri, whereClause, whereValues);
        Log.v(TAG, "cleanupOldCopies: cleaned " + count + " copies");
    }

    public static long insertOrUpdateContentValues(ContentResolver resolver,
            Uri contentUri, String[] projection, String whereClause,
            String[] whereValues, ContentValues values) {
        Log.v(TAG, "insertOrUpdateContentValues");
        Cursor c = resolver.query(contentUri, projection, whereClause,
                whereValues, null);
        if (c == null) {
            return Extra.ID_UNKNOWN;
        }

        int cursorCount = c.getCount();
        long id;
        if (cursorCount == 0) {
            id = ContentUris.parseId(resolver.insert(contentUri, values));
        } else if (cursorCount == 1) {
            c.moveToFirst();
            id = POIHelper.getId(c);
            resolver.update(contentUri, values, whereClause, whereValues);
        } else {
            id = Extra.ID_UNKNOWN;
        }
        c.close();

        return id;
    }

    public static void deleteRetrievedData(ContentResolver resolver) {
        Log.v(TAG, "deleteRetrievedData");
        String whereClause = "( " + POIs.TAG + " = ?)";
        String[] whereValues = new String[]{Integer
                .toString(POIs.TAG_RETRIEVED)};
        Uri uri = POIs.createNoNotify(POIs.CONTENT_URI_RETRIEVED);
        resolver.delete(uri, whereClause, whereValues);
    }

    public static void insert(ContentResolver resolver, SingleNode node) {
        Log.v(TAG, "insert singleNode");
        ContentValues values = new ContentValues();
        DataOperationsNodes don = new DataOperationsNodes(null);
        don.copyToValues(node.getNode(), values);
        String whereClause = "( " + POIs.WM_ID + " = ? )";
        String whereValues[] = {node.getNode().getId().toString()};

        long id = insertOrUpdateContentValues(resolver,
                Wheelmap.POIs.CONTENT_URI_RETRIEVED, Wheelmap.POIs.PROJECTION,
                whereClause, whereValues, values);

        createCopyIfNotExists(resolver, id, false);
    }

    public static long insertNew(ContentResolver resolver, String name,
            double latitude, double longitude) {
        Log.v(TAG, "insertNew");
        ContentValues values = new ContentValues();
        values.put(POIs.NAME, name);

        values.put(POIs.LATITUDE, latitude);
        values.put(POIs.LONGITUDE, longitude);

        values.put(POIs.CATEGORY_ID, SupportManager.UNKNOWN_TYPE);
        values.put(POIs.NODETYPE_ID, SupportManager.UNKNOWN_TYPE);

        values.put(POIs.TAG, POIs.TAG_COPY);
        values.put(POIs.STATE, POIs.STATE_UNCHANGED);
        values.put(POIs.DIRTY, POIs.CLEAN);

        Uri uri = resolver.insert(POIs.CONTENT_URI_COPY, values);
        long id = ContentUris.parseId(uri);
        Log.i(TAG, "New POI in database: id = " + id);
        return id;
    }

    public static Cursor queryState(ContentResolver resolver, int state) {
        Log.v(TAG, "queryState state = " + state);
        String whereClause = POIs.STATE + " = ?";
        String[] whereValues = new String[]{Integer.toString(state)};

        Cursor c = resolver.query(POIs.CONTENT_URI_COPY, POIs.PROJECTION,
                whereClause, whereValues, null);

        c.moveToFirst();
        return c;
    }

    public static long storeTemporary(ContentResolver resolver,
            ContentValues values) {
        Uri uri = POIs.createNoNotify(POIs.CONTENT_URI_TMP);
        resolver.delete(uri, null, null);
        return ContentUris.parseId(resolver.insert(uri, values));

    }
}
