/*
 * #%L
 * Wheelmap-it - Integration tests
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

package org.wheelmap.android.test;

import junit.framework.Assert;

import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.utils.GeocoordinatesMath;

import wheelmap.org.BoundingBox;
import wheelmap.org.WheelchairState;
import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.test.AndroidTestCase;
import android.util.Log;

public class ModelTest extends AndroidTestCase {

	private final static String TAG = "executor";

	public void testNodes() throws RemoteException, OperationApplicationException {

		BoundingBox bb = GeocoordinatesMath.calculateBoundingBox( 
				new Wgs84GeoCoordinates(13.3988888, 52.50055), 2 );

		ContentResolver cr = getContext().getContentResolver();

//		RESTExecutor re = new RESTExecutor(cr);
//		re.retrieveSinglePage( bb, WheelchairState.UNKNOWN );
//
		Cursor c = cr.query(Wheelmap.POIs.CONTENT_URI, null, null, null, null);
//		Assert.assertEquals(RESTExecutor.DEFAULT_TEST_PAGE_SIZE, c.getCount());

		int nameIndex = c.getColumnIndex(Wheelmap.POIs.NAME);
		int wmIdIndex = c.getColumnIndex(Wheelmap.POIs.WM_ID);
		int latIndex = c.getColumnIndex(Wheelmap.POIs.COORD_LAT);
		int lonIndex = c.getColumnIndex(Wheelmap.POIs.COORD_LON);
		int streetIndex = c.getColumnIndex(Wheelmap.POIs.STREET);
		int housenumIndex = c.getColumnIndex(Wheelmap.POIs.HOUSE_NUM);
		int postcodeIndex = c.getColumnIndex(Wheelmap.POIs.POSTCODE);
		int cityIndex = c.getColumnIndex(Wheelmap.POIs.CITY);
		int phoneIndex = c.getColumnIndex(Wheelmap.POIs.PHONE);
		int websiteIndex = c.getColumnIndex(Wheelmap.POIs.WEBSITE);
		int wheelchairIndex = c.getColumnIndex(Wheelmap.POIs.WHEELCHAIR);
		int wheelchairDescIndex = c
				.getColumnIndex(Wheelmap.POIs.WHEELCHAIR_DESC);

		String name;
		Integer wmId;
		String latitude;
		String longitude;
		String street;
		String houseNum;
		String postCode;
		String city;
		String phone;
		String website;
		String wheelchair;
		String wheelchairDesc;

		c.moveToFirst();
		while (!c.isAfterLast()) {
			name = c.getString(nameIndex);
			wmId = c.getInt(wmIdIndex);
			latitude = c.getString(latIndex);
			longitude = c.getString(lonIndex);
			street = c.getString(streetIndex);
			houseNum = c.getString(housenumIndex);
			postCode = c.getString(postcodeIndex);
			city = c.getString(cityIndex);
			phone = c.getString(phoneIndex);
			website = c.getString(websiteIndex);
			wheelchair = c.getString(wheelchairIndex);
			wheelchairDesc = c.getString(wheelchairDescIndex);
			Log.d(TAG, "wmId = " + wmId + " Name = " + name + " lat = "
					+ latitude + " lon = " + longitude + " street = " + street
					+ " housenum = " + houseNum + " postcode = " + postCode
					+ " city = " + city + " phone = " + phone + " website = "
					+ website + " wheelchar = " + wheelchair
					+ " wheelchairDesc = " + wheelchairDesc);
			c.moveToNext();
		}
	}

	public void testNodesTwo() throws RemoteException, OperationApplicationException {
		BoundingBox bb = GeocoordinatesMath.calculateBoundingBox( 
				new Wgs84GeoCoordinates(13.3988888, 52.50055), 2 );

		ContentResolver cr = getContext().getContentResolver();

//		RESTExecutor re = new RESTExecutor(cr);
//		re.retrieveAllPages(bb, WheelchairState.UNKNOWN);

		Cursor c = cr.query(Wheelmap.POIs.CONTENT_URI, null, null, null, null);
		Log.d(TAG, "Query count = " + c.getCount());
		// Assert.assertEquals(5, c.getCount());
	}
}
