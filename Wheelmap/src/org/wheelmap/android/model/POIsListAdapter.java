package org.wheelmap.android.model;

import java.util.ArrayList;

import org.wheelmap.android.R;
import org.wheelmap.android.ui.POIsListItemView;

import wheelmap.org.WheelchairState;
import android.content.Context;
import android.database.Cursor;
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
	}

	// copy data from ContentProvider into Array and sort it
	private ArrayList<POI> getPois() {
		POIHelper mPOIHelper = new POIHelper();
		ArrayList<POI> pois = new ArrayList<POI>();
		if (mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			do {		
				POI poi = new POI();
				poi.name = mPOIHelper.getName(mCursor);
				poi.category = mPOIHelper.getAddress(mCursor);
				poi.state = mPOIHelper.getWheelchair(mCursor);
						
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

			// TODO calculate distance from current location
			pliv.setDistance("123 m");

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