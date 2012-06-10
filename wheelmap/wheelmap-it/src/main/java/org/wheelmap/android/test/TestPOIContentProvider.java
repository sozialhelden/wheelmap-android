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


import org.wheelmap.android.model.POIsProvider;
import org.wheelmap.android.model.Wheelmap;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.test.ProviderTestCase2;

public class TestPOIContentProvider extends  ProviderTestCase2<POIsProvider> {

	private Location mLocation;

	public TestPOIContentProvider() {
		super(POIsProvider.class, Wheelmap.AUTHORITY);
	}


	protected void setUp() throws Exception {
		super.setUp();
		// Berlin, Andreasstra�e 10
		mLocation = new Location("");
		mLocation.setLongitude(13.431240);
		mLocation.setLatitude(52.512523);
	}

	public String[] createWhereValues() {
		String[] lonlat = new String[] {
				String.valueOf(mLocation.getLongitude()),
				String.valueOf(mLocation.getLatitude()) };
		return lonlat;
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	private void insertDummyPOI() {
		
		final ContentResolver resolver = getMockContentResolver();

		// create new POI and start editing
		ContentValues cv = new ContentValues();

		cv.put(Wheelmap.POIs.NAME, "default");
		cv.put(Wheelmap.POIs.COORD_LAT,  Math.ceil(mLocation.getLatitude() * 1E6));
		cv.put(Wheelmap.POIs.COORD_LON,  Math.ceil(mLocation.getLongitude() * 1E6));
		cv.put(Wheelmap.POIs.CATEGORY_ID, 1);
		cv.put(Wheelmap.POIs.NODETYPE_ID, 1);
		resolver.insert(Wheelmap.POIs.CONTENT_URI, cv);
	}

	public void testLocalHandler() throws Exception {
		Uri uri = Wheelmap.POIs.CONTENT_URI_POI_SORTED;


		final ContentResolver resolver = getMockContentResolver();
		Cursor cursor = resolver.query(uri, Wheelmap.POIs.PROJECTION, null,
				createWhereValues(), "");
			assertEquals(0, cursor.getCount());
		insertDummyPOI();

		uri = Wheelmap.POIs.CONTENT_URI_POI_SORTED;
		
		cursor = resolver.query(uri, Wheelmap.POIs.PROJECTION, null,
				createWhereValues(), "");
		assertEquals(1, cursor.getCount());
		
		//cursor = resolver.query(uri, Wheelmap.POIs.PROJECTION, null,
		//		createWhereValues(), "");
		//assertEquals(2, cursor.getCount());
	}     

}
