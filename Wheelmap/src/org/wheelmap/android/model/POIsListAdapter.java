package org.wheelmap.android.model;

import java.util.ArrayList;

import org.wheelmap.android.R;
import org.wheelmap.android.ui.POIsListItemView;
import org.wheelmap.android.utils.GeocoordinatesMath;

import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;
import wheelmap.org.WheelchairState;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class POIsListAdapter extends BaseAdapter {
	private final static String TAG = "poislist";

	Context mContext;
	Cursor mCursor;
	ArrayList<POI> pois;

	private class POI {
		public POI() {

		}

		public String name;
		public String category;
		public WheelchairState state;
		public double distance;
	}

	public POIsListAdapter(Context context, Cursor c) {
		super();

		mContext = context;
		mCursor = c;
		pois = createPois();

		mCursor.registerContentObserver(new ChangeObserver());

	}

	private ArrayList<POI> createPois() {
		// copy data from ContentProvider into Array and sort it

		LocationManager lm = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);
		Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (loc == null)
			loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (loc == null) {
			loc = new Location(LocationManager.GPS_PROVIDER);
			loc.setLatitude(52.5);
			loc.setLongitude(13.4);
		}

		Wgs84GeoCoordinates location = new Wgs84GeoCoordinates(
				loc.getLongitude(), loc.getLatitude());

		ArrayList<POI> pois = new ArrayList<POI>();
		if (mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			do {
				POI poi = new POI();
				poi.name = POIHelper.getName(mCursor);
				poi.category = POIHelper.getAddress(mCursor);
				poi.state = POIHelper.getWheelchair(mCursor);
				// calcutate distance
				poi.distance = GeocoordinatesMath.calculateDistance(
						location,
						new Wgs84GeoCoordinates(
								POIHelper.getLongitude(mCursor), POIHelper
										.getLatitude(mCursor)));

				pois.add(poi);
			} while (mCursor.moveToNext());
		}
		return pois;
	}

	@Override
	public int getCount() {
		return pois.size();
	}

	@Override
	public Object getItem(int position) {
		return pois.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null)
			convertView = new POIsListItemView(mContext);
		if (position < pois.size()) {
			POI poi = pois.get(position);
			POIsListItemView pliv = (POIsListItemView) convertView;

			pliv.setCategory(poi.category);
			pliv.setName(poi.name);

			// TODO nice formater for distance (123m, 1.5km ....)
			pliv.setDistance(String.format("%.3f", poi.distance));

			switch (poi.state) {
			case UNKNOWN:
				pliv.setIcon(R.drawable.marker_unknown);
				break;
			case YES:
				pliv.setIcon(R.drawable.marker_yes);
				break;
			case LIMITED:
				pliv.setIcon(R.drawable.marker_limited);
				break;
			case NO:
				pliv.setIcon(R.drawable.marker_no);
				break;
			default:
				pliv.setIcon(R.drawable.marker_unknown);
				break;
			}

			return convertView;
		} else
			return null;
	}

	private class ChangeObserver extends ContentObserver {
		public ChangeObserver() {
			super(new Handler());
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			Log.d(TAG, "POIsListAdapter: ChangeObserver: onChange");
			mCursor.requery();
			pois = createPois();
			notifyDataSetChanged();
		}
	}

}