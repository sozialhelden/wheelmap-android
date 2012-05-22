/*
Copyright (C) 2011 Michal Harakal and Michael Kroez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS-IS" BASIS
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
        
*/

package org.wheelmap.android.model;

import wheelmap.org.WheelchairState;
import android.content.ContentValues;
import android.database.Cursor;

public class POIHelper {
	
	public static String getWMId( Cursor c ) {
		return c.getString( c.getColumnIndexOrThrow( Wheelmap.POIsColumns.WM_ID));
	}
		
	public static String getName(Cursor c) {
		String name = c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.NAME));
		if ( name == null)
			name = "";
		name = name.replace( "&#38;", "&" );
		
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
	
	public static int getLatitudeAsInt(Cursor c) {
		return(c.getInt(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.COORD_LAT)));
	}
	
	public static int getLongitudeAsInt(Cursor c) {
		return(c.getInt(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.COORD_LON)));
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
		if ((street != null || nr != null) && (postcode != null || city != null))
			address.append(", ");
		
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
	
	public static int getUpdateTag( Cursor c ) {
		return c.getInt( c.getColumnIndexOrThrow( Wheelmap.POIsColumns.UPDATE_TAG));
	}
	
	public static String getHouseNumber( Cursor c ) {
		return c.getString(c.getColumnIndexOrThrow(Wheelmap.POIsColumns.HOUSE_NUM));
	}
	
	public static int getCategoryId( Cursor c ) {
		return c.getInt( c.getColumnIndexOrThrow( Wheelmap.POIsColumns.CATEGORY_ID));
	}
	
	public static String getCategoryIdentifier( Cursor c ) {
		return c.getString( c.getColumnIndexOrThrow( Wheelmap.POIsColumns.CATEGORY_IDENTIFIER));
	}
	
	public static int getNodeTypeId( Cursor c ) {
		return c.getInt( c.getColumnIndexOrThrow( Wheelmap.POIsColumns.NODETYPE_ID));
	}
	
	public static String getNodeTypeIdentifier( Cursor c ) {
		return c.getString( c.getColumnIndexOrThrow( Wheelmap.POIsColumns.NODETYPE_IDENTIFIER));
	}
	
	public static void copyItemToValues( Cursor c, ContentValues values ) {
		values.put(Wheelmap.POIs.WM_ID, POIHelper.getWMId(c));
		values.put(Wheelmap.POIs.NAME, POIHelper.getName(c));
		values.put(Wheelmap.POIs.CATEGORY_ID, POIHelper.getCategoryId(c));
		values.put(Wheelmap.POIs.CATEGORY_IDENTIFIER, POIHelper.getCategoryIdentifier(c));
		values.put(Wheelmap.POIs.NODETYPE_ID, POIHelper.getNodeTypeId(c));
		values.put(Wheelmap.POIs.NODETYPE_IDENTIFIER, POIHelper.getNodeTypeIdentifier(c));
		values.put(Wheelmap.POIs.COORD_LAT, POIHelper.getLatitude(c));
		values.put(Wheelmap.POIs.COORD_LON, POIHelper.getLongitude(c));
		values.put(Wheelmap.POIs.WHEELCHAIR, POIHelper.getWheelchair(c)
				.getId());
		values.put(Wheelmap.POIs.WHEELCHAIR_DESC, POIHelper.getComment(c));
		values.put(Wheelmap.POIs.STREET, POIHelper.getStreet(c));
		values.put(Wheelmap.POIs.HOUSE_NUM, POIHelper.getHouseNumber(c));
		values.put(Wheelmap.POIs.POSTCODE, POIHelper.getPostcode(c));
		values.put(Wheelmap.POIs.CITY, POIHelper.getCity(c));
		values.put(Wheelmap.POIs.WEBSITE, POIHelper.getWebsite(c));
		values.put(Wheelmap.POIs.PHONE, POIHelper.getPhone(c));
	}
}
