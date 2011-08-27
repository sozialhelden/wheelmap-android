package org.wheelmap.android.ui.mapsforge;

import org.mapsforge.android.maps.ArrayItemizedOverlay;
import org.mapsforge.android.maps.GeoPoint;
import org.wheelmap.android.R;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.Wheelmap;

import wheelmap.org.WheelchairState;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class POIsPaintedMapsforgeOverlay extends ArrayItemizedOverlay {

	private Context mContext;
	private Cursor mPois;
	private Drawable dUnknown;
	private Drawable dYes;
	private Drawable dNo;
	private Drawable dLimited;

	public POIsPaintedMapsforgeOverlay(Context context, Cursor cursor) {
		super(context.getResources().getDrawable(R.drawable.marker_unknown));

		mContext = context;
		mPois = cursor;

		dUnknown = mContext.getResources().getDrawable(
				R.drawable.marker_unknown);
		dYes = mContext.getResources().getDrawable(R.drawable.marker_yes);
		dNo = mContext.getResources().getDrawable(R.drawable.marker_no);
		dLimited = mContext.getResources().getDrawable(
				R.drawable.marker_limited);

		refreshLocations();
		mPois.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				refreshLocations();
			}
		});
	}

	private void refreshLocations() {
		Log.d( "poiadding", "refreshLocations" );

		int stateColumn = mPois.getColumnIndex(Wheelmap.POIs.WHEELCHAIR);
		int latColumn = mPois.getColumnIndex(Wheelmap.POIs.COORD_LAT);
		int lonColumn = mPois.getColumnIndex(Wheelmap.POIs.COORD_LON);
		int idColumn = mPois.getColumnIndex(Wheelmap.POIs._ID);

		clear();
		if (mPois.moveToFirst())
			do {
				Double lat = mPois.getDouble(latColumn);
				Double lng = mPois.getDouble(lonColumn);
				WheelchairState state = WheelchairState.valueOf(mPois
						.getInt(stateColumn));
				int poiId = mPois.getInt(idColumn);

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

				// 
				marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight()); 
				POIMapItem geoPoint = new POIMapItem(new GeoPoint(
						lat.intValue(), lng.intValue()), state, poiId, marker);
				Log.d( "poiadding", "Adding new geopoint to overlay" );
				addItem(geoPoint);
			} while (mPois.moveToNext());
	}

	@Override
	public boolean onTap(int index) {
		POIMapItem mapItem = (POIMapItem) createItem(index);
		Uri poiUri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI,
				Integer.toString(mapItem.getId()));

		// Then query for this specific record:
		Cursor cur = mContext.getContentResolver().query(poiUri, null, null,
				null, null);
		POIHelper poi_helper = new POIHelper();
		if (cur.moveToFirst()) {
			Log.d("POI id",
					Integer.toBinaryString(mapItem.getId())
							+ poi_helper.getName(cur) + ' '
							+ poi_helper.getAddress(cur));

			Toast.makeText(mContext,
					poi_helper.getName(cur) + ' ' + poi_helper.getAddress(cur),
					Toast.LENGTH_SHORT).show();
		}
		cur.close();
		return true;
	}
}
