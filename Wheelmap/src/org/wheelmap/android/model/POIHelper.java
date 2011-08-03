package org.wheelmap.android.model;

import wheelmap.org.WheelchairState;
import android.database.Cursor;

public class POIHelper {
		
	public String getName(Cursor c) {
		return(c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.NAME)));
	}
	
	public String getAddress(Cursor c) {
		StringBuilder address = new StringBuilder();
		// street
		String street = c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.STREET));
		if (street != null){
			address.append(street);
			address.append(' ');
		}
		// house number
		String nr = c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.HOUSE_NUM));
		if (street != null){
			address.append(nr);
			address.append(',');
		}
		// post code 
		String postcode = c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.POSTCODE));
		if (street != null){
			address.append(postcode);
			address.append(' ');
		}
		// city 
		String city = c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.CITY));
		if (street != null){
			address.append(city);
			
		}
		return address.toString();
	}
	
	public WheelchairState getWheelchair(Cursor c) { 
		return WheelchairState.valueOf(c.getInt(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.WHEELCHAIR)));
	}
}
