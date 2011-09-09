package org.wheelmap.android.model;

import wheelmap.org.WheelchairState;
import android.database.Cursor;

public class POIHelper {
		
	public static String getName(Cursor c) {
		String name = c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.NAME));
		if ( name == null)
			name = "";
		
		return name;
	}
	
	public static String getStreet(Cursor c) {
		String street = c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.STREET));
		if ( street == null)
			street = "";
		
		return street;
	}
	
	public static String getPostcode(Cursor c) {
		String postcode = c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.POSTCODE));
		if ( postcode == null)
			postcode = "";
		
		return postcode;
	}
	
	public static String getCity(Cursor c) {
		String city = c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.CITY));
		if ( city == null)
			city = "";
		
		return city;
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
		}
		// post code  & city
		String postcode = c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.POSTCODE));
		String city = c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.CITY));
		if ( postcode != null || city != null )
			address.append(',');
		
		if (postcode != null){
			address.append(postcode);
			address.append(' ');
		}
		if (city != null){
			address.append(city);
			
		}
		return address.toString();
	}
	
	public static WheelchairState getWheelchair(Cursor c) { 
		return WheelchairState.valueOf(c.getInt(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.WHEELCHAIR)));
	}
	
	//  comment into DB
	public static String getComment(Cursor c) { 
		String comment = c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.WHEELCHAIR_DESC));
		if ( comment == null)
			comment = "";
		
		return comment;
	}
	
	public static String getWebsite(Cursor c) { 
		String website = c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.WEBSITE));
		if ( website == null)
			website = "";
		
		return website;
	}
	
	public static String getPhone(Cursor c) { 
		String phone = c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.PHONE));
		if ( phone == null)
			phone = "";
		
		return phone;
	}


	public static long getId(Cursor c) {
		return c.getLong( c.getColumnIndexOrThrow( Wheelmap.POIs._ID));
	}
}
