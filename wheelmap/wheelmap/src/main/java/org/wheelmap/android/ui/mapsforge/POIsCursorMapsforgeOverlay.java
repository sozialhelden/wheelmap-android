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
package org.wheelmap.android.ui.mapsforge;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.overlay.ItemizedOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.ui.POIDetailActivity;

import wheelmap.org.WheelchairState;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class POIsCursorMapsforgeOverlay extends ItemizedOverlay<OverlayItem> {
	private final static String TAG = "mapsforge";

	private final static String THREAD_NAME = "MapsforgeOverlay";

	private Context mContext;
	private Cursor mCursor;
	private Handler mHandler;
	private boolean mCursorInvalidated;

	public POIsCursorMapsforgeOverlay(Context context) {
		super(null);
		mContext = context;
		mHandler = new Handler();
	}

	@Override
	public synchronized void finalize() {
		if (mCursor != null)
			mCursor.close();
	}
	
	public synchronized void setCursor(Cursor cursor ) {
		if ( mCursor != null ) {
			mCursor.unregisterContentObserver( mContentObserver );
			mCursor.unregisterDataSetObserver( mCursorObserver );
			mCursor.close();
		}
			
		mCursor = cursor;
		mCursorInvalidated = false;
	
		if (mCursor == null)
			return;
		
		
		mCursor.registerContentObserver( mContentObserver );
		mCursor.registerDataSetObserver( mCursorObserver );
		populate();
	}

	@Override
	public synchronized int size() {
		if (mCursor == null)
			return 0;
		return mCursor.getCount();
	}

	@Override
	protected synchronized OverlayItem createItem(int i) {
		if (mCursor == null || mCursor.isClosed() || mCursorInvalidated)
			return null;

		int count = mCursor.getCount();
		if (count == 0 || i >= count) {
			return null;
		}

		mCursor.moveToPosition(i);
		String name = POIHelper.getName(mCursor);
		SupportManager manager = WheelmapApp.getSupportManager();
		WheelchairState state = POIHelper.getWheelchair(mCursor);
		int lat = POIHelper.getLatitudeAsInt(mCursor);
		int lng = POIHelper.getLongitudeAsInt(mCursor);
		int nodeTypeId = POIHelper.getNodeTypeId(mCursor);
		Drawable marker = null;
		if (nodeTypeId != 0)
			marker = manager.lookupNodeType(nodeTypeId).stateDrawables
					.get(state);

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
	
	private ContentObserver mContentObserver = new ContentObserver(new Handler()) {

		@Override
		public boolean deliverSelfNotifications() {
			return false;
		}

		@Override
		public void onChange(boolean selfChange) {
			reload();
		}
		
	};
	
	private DataSetObserver mCursorObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			super.onChanged();
			Log.d( TAG, "cursor changed" );
		}

		@Override
		public void onInvalidated() {
			super.onInvalidated();
			Log.d( TAG, "cursor invalidated" );
			mCursorInvalidated = true;
		}
	};

	private synchronized void reload() {
		Log.d(TAG, "reload - requery and populate");
		mCursor.requery();
		mCursorInvalidated = false;
		populate();
	}

	public synchronized void deactivateCursor() {
		Log.d(TAG, "deactivate");
		mCursor.deactivate();
	}

	@Override
	protected synchronized boolean onTap(int index) {
		if (mCursor == null)
			return false;

		int count = mCursor.getCount();
		if (count == 0 || index >= count)
			return false;

		mCursor.moveToPosition(index);
		long poiId = POIHelper.getId(mCursor);
		Log.d(TAG, "onTap index = " + index + " id = " + poiId);

		Intent i = new Intent(mContext, POIDetailActivity.class);
		i.putExtra(Wheelmap.POIs.EXTRAS_POI_ID, poiId);
		mContext.startActivity(i);
		return true;
	}

	@Override
	protected synchronized boolean onLongPress(int index) {
		if (mCursor == null)
			return false;

		int count = mCursor.getCount();
		if (count == 0 || index >= count)
			return false;

		mCursor.moveToPosition(index);
		long poiId = POIHelper.getId( mCursor );
		String name = POIHelper.getName( mCursor );
		int nodeTypeId = POIHelper.getNodeTypeId( mCursor );
		String nodeTypeName = WheelmapApp.getSupportManager().lookupNodeType( nodeTypeId).localizedName;
		String address = POIHelper.getAddress(mCursor );
				
		StringBuilder builder = new StringBuilder();
		if ( name.length() > 0 )
			builder.append( name );
		else
			builder.append( nodeTypeName );
		
		if ( address.length() > 0 ) {
			builder.append( ", " );
			builder.append( address );
		}
		
		final String outputText = builder.toString();
		Log.d( TAG, Long.toString(poiId) + " " + outputText );
		mHandler.post( new Runnable() {

				@Override
				public void run() {
					Toast.makeText( mContext, outputText, Toast.LENGTH_SHORT ).show();				
				}
				
		});
			
		return true;
	}
}
