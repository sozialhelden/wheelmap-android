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
package org.wheelmap.android.overlays;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.overlay.ItemizedOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.WheelchairState;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import de.akquinet.android.androlog.Log;

public class POIsCursorMapsforgeOverlay extends ItemizedOverlay<OverlayItem> {

    private final static String TAG = POIsCursorMapsforgeOverlay.class
            .getSimpleName();

    private final static String THREAD_NAME = "MapsforgeOverlay";

    private Context mContext;

    private Cursor mCursor;

    private Handler mHandler;

    private OnTapListener mListener;

    public POIsCursorMapsforgeOverlay(Context context, OnTapListener listener) {
        super(null);
        mContext = context;
        mHandler = new Handler();
        mListener = listener;
    }

    public synchronized void setCursor(Cursor cursor) {
        if (cursor == mCursor) {
            return;
        }

        mCursor = cursor;

        if (mCursor == null) {
            return;
        }

        populate();
    }

    @Override
    public synchronized int size() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    @Override
    protected synchronized OverlayItem createItem(int i) {
        if (mCursor == null || mCursor.isClosed()) {
            return null;
        }

        int count = mCursor.getCount();
        if (count == 0 || i >= count) {
            return null;
        }

        mCursor.moveToPosition(i);
        String name = POIHelper.getName(mCursor);
        SupportManager manager = WheelmapApp.getSupportManager();
        WheelchairState state = POIHelper.getWheelchair(mCursor);
        double lat = POIHelper.getLatitude(mCursor);
        double lng = POIHelper.getLongitude(mCursor);
        int nodeTypeId = POIHelper.getNodeTypeId(mCursor);
        Drawable marker = null;
        if (nodeTypeId != 0) {
            marker = manager.lookupNodeType(nodeTypeId).stateDrawables
                    .get(state);
        }

        OverlayItem item = new OverlayItem();
        item.setTitle(name);
        item.setSnippet(name);
        item.setPoint(new GeoPoint(lat, lng));
        item.setMarker(marker);
        return item;
    }

    @Override
    protected String getThreadName() {
        return THREAD_NAME;
    }

    @Override
    protected synchronized boolean onTap(int index) {
        if (mCursor == null) {
            return false;
        }

        int count = mCursor.getCount();
        if (count == 0 || index >= count) {
            return false;
        }

        mCursor.moveToPosition(index);
        long poiId = POIHelper.getId(mCursor);
        Log.d(TAG, "onTap index = " + index + " id = " + poiId);

        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(mCursor, values);
        if (mListener != null) {
            mListener.onTap(createItem(index), values);
        }

        return true;
    }

    @Override
    protected synchronized boolean onLongPress(int index) {
        if (mCursor == null) {
            return false;
        }

        int count = mCursor.getCount();
        if (count == 0 || index >= count) {
            return false;
        }

        mCursor.moveToPosition(index);
        long poiId = POIHelper.getId(mCursor);
        String name = POIHelper.getName(mCursor);
        int nodeTypeId = POIHelper.getNodeTypeId(mCursor);
        String nodeTypeName = WheelmapApp.getSupportManager().lookupNodeType(
                nodeTypeId).localizedName;
        String address = POIHelper.getAddress(mCursor);

        StringBuilder builder = new StringBuilder();
        if (!TextUtils.isEmpty(name)) {
            builder.append(name);
        } else {
            builder.append(nodeTypeName);
        }

        if (!TextUtils.isEmpty(address)) {
            builder.append(", ");
            builder.append(address);
        }

        final String outputText = builder.toString();
        Log.d(TAG, Long.toString(poiId) + " " + outputText);
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(mContext, outputText, Toast.LENGTH_SHORT).show();
            }

        });

        return true;
    }
}
