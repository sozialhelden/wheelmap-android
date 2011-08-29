package org.wheelmap.android.ui.mapsforge;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.ItemizedOverlay;
import org.mapsforge.android.maps.OverlayItem;
import org.wheelmap.android.R;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.ui.POIDetailActivity;

import wheelmap.org.WheelchairState;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class POIsCursorMapsforgeOverlay extends ItemizedOverlay<OverlayItem> {
	private final static String TAG = "mapsforge";

	private final static String THREAD_NAME = "CursorMapsforgeOverlay";

	private Context mContext;
	private Cursor mCursor;
	private Drawable dUnknown;
	private Drawable dYes;
	private Drawable dNo;
	private Drawable dLimited;

	public POIsCursorMapsforgeOverlay(Context context, Cursor cursor) {
		super(null);

		mContext = context;
		mCursor = cursor;

		dUnknown = mContext.getResources().getDrawable(
				R.drawable.marker_unknown);
		dYes = mContext.getResources().getDrawable(R.drawable.marker_yes);
		dNo = mContext.getResources().getDrawable(R.drawable.marker_no);
		dLimited = mContext.getResources().getDrawable(
				R.drawable.marker_limited);

		mCursor.registerContentObserver(new ChangeObserver());
		populate();
	}

	@Override
	public int size() {
		synchronized (mCursor) {
			return mCursor.getCount();
		}
	}

	@Override
	protected OverlayItem createItem(int i) {
		synchronized (mCursor) {
			int count = mCursor.getCount();
			if (count == 0)
				return null;

			mCursor.moveToPosition(i);
			int stateColumn = mCursor.getColumnIndex(Wheelmap.POIs.WHEELCHAIR);
			int latColumn = mCursor.getColumnIndex(Wheelmap.POIs.COORD_LAT);
			int lonColumn = mCursor.getColumnIndex(Wheelmap.POIs.COORD_LON);

			Double lat = mCursor.getDouble(latColumn);
			Double lng = mCursor.getDouble(lonColumn);
			WheelchairState state = WheelchairState.valueOf(mCursor
					.getInt(stateColumn));

			Log.d( TAG, "Wheelchair state = " + state.toString());
			Drawable marker;
			switch (state) {
			case UNKNOWN:
				marker = dUnknown;
				break;
			case YES:
				marker = dYes;
				break;
			case LIMITED:
				marker = dLimited;
				break;
			case NO:
				marker = dNo;
				break;
			default:
				marker = dUnknown;
			}

			marker.setBounds(0, 0, marker.getIntrinsicWidth(),
					marker.getIntrinsicHeight());
			OverlayItem item = new OverlayItem();
			item.setPoint( new GeoPoint(lat.intValue(), lng.intValue()));
			item.setMarker( marker );
			return item;
		}
	}

	@Override
	protected String getThreadName() {
		return THREAD_NAME;
	}

	private class ChangeObserver extends ContentObserver {
		public ChangeObserver() {
			super(new Handler());
		}

		@Override
		public boolean deliverSelfNotifications() {
			return false;
		}

		@Override
		public void onChange(boolean selfChange) {
			synchronized (mCursor) {
				mCursor.requery();
			}
			populate();
		}
	}
	
	@Override
	public boolean onTap(int index) {
		mCursor.moveToPosition(index);
		int idColumn = mCursor.getColumnIndex(Wheelmap.POIs._ID);
		int poiId = mCursor.getInt(idColumn);
		
		Log.d(TAG, "onTap: index = " + index + " id = " + poiId);

		Uri poiUri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI,
				String.valueOf( poiId ));

		// Then query for this specific record:
		Cursor cur = mContext.getContentResolver().query(poiUri, null, null,
				null, null);
		if (cur.moveToFirst()) {
			Log.d(TAG, Integer.toBinaryString(poiId) + " "
					+ POIHelper.getName(cur) + ' ' + POIHelper.getAddress(cur));

			Toast.makeText(mContext,
					POIHelper.getName(cur) + ' ' + POIHelper.getAddress(cur),
					Toast.LENGTH_SHORT).show();
		}
		cur.close();
		return true;
	}

	@Override
	protected boolean onLongPress(int index) {
		mCursor.moveToPosition(index);
		int poiId = POIHelper.getId( mCursor );
		
		Intent i = new Intent(mContext, POIDetailActivity.class);
		i.putExtra(Wheelmap.POIs.EXTRAS_POI_ID, poiId);
		mContext.startActivity( i );
		return true;
	}

}
