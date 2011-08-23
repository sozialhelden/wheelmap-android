package org.wheelmap.android.model;

import java.util.ArrayList;

import org.wheelmap.android.R;
import org.wheelmap.android.ui.POIsListItemView;
import org.wheelmap.android.utils.GeocoordinatesMath;

import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;
import wheelmap.org.WheelchairState;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class POIsListAdapter extends BaseAdapter {
	Context mContext;	
	Cursor mCursor;

	private class POI {
		public POI() {
			
		}
		public String name;
	    public String category;
	    public WheelchairState state;
	    public double distance;
	}

	// copy data from ContentProvider into Array and sort it
	private ArrayList<POI> getPois() {
		POIHelper mPOIHelper = new POIHelper();
		
		Location loc = ((LocationManager)mContext.getSystemService(mContext.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (loc == null)
            loc = ((LocationManager)mContext.getSystemService(mContext.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (loc == null) { // fallback berlin 
            loc = new Location(LocationManager.GPS_PROVIDER);
            loc.setLatitude(52.5);
            loc.setLongitude(13.4);
        }
        Wgs84GeoCoordinates cur_loc = new Wgs84GeoCoordinates(loc.getLongitude(), loc.getLatitude()) ;
        
		
		ArrayList<POI> pois = new ArrayList<POI>();
		if (mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			do {		
				POI poi = new POI();
				poi.name = mPOIHelper.getName(mCursor);
				poi.category = mPOIHelper.getAddress(mCursor);
				poi.state = mPOIHelper.getWheelchair(mCursor);
				// calcutate distance
				poi.distance = GeocoordinatesMath.calculateDistance(cur_loc, 
						new Wgs84GeoCoordinates(
								mPOIHelper.getLongitude(mCursor),
								mPOIHelper.getLatitude(mCursor)));
						
				pois.add(poi);
			} while (mCursor.moveToNext());                   
		}
		return pois;
	}

	ArrayList<POI> pois;
	
	public POIsListAdapter(Context context, Cursor c) {
		super();		

		mContext = context;   
		mCursor = c;
		pois = getPois();
		
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
			POIsListItemView  pliv = (POIsListItemView ) convertView;


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
		} 
		else
			return null;
	}
}