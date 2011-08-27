package org.wheelmap.android.model;

import wheelmap.org.WheelchairState;
import android.database.Cursor;

public class POIHelper {
		
	public static String getName(Cursor c) {
		return(c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.NAME)));
	}
	
	public static double getLatitude(Cursor c) {
		return(c.getDouble(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.COORD_LAT)) / 1E6);
	}
	
	public static double getLongitude(Cursor c) {
		return(c.getDouble(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.COORD_LON)) / 1E6);
	}
	
	public static String getAddress(Cursor c) {
		StringBuilder address = new StringBuilder();
		// street
		String street = c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.STREET));
		if (street != null){
			address.append(street);
			address.append(' ');
		}
		// house number
		String nr = c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.HOUSE_NUM));
		if (nr != null){
			address.append(nr);
			address.append(',');
		}
		// post code 
		String postcode = c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.POSTCODE));
		if (postcode != null){
			address.append(postcode);
			address.append(' ');
		}
		// city 
		String city = c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.CITY));
		if (city != null){
			address.append(city);
			
		}
		return address.toString();
	}
	
	public static WheelchairState getWheelchair(Cursor c) { 
		return WheelchairState.valueOf(c.getInt(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.WHEELCHAIR)));
	}
}
