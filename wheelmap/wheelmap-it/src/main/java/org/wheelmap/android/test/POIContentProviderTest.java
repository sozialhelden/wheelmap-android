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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Assert;

import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.POIsProvider;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.model.Wheelmap.POIs;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.util.Log;

import com.jayway.awaitility.Awaitility;

public class POIContentProviderTest extends ProviderTestCase2<POIsProvider> {
	private final static String TAG = POIContentProviderTest.class
			.getSimpleName();

	private Location mLocation;

	public POIContentProviderTest() {
		super(POIsProvider.class, Wheelmap.AUTHORITY);
	}

	protected void setUp() throws Exception {
		super.setUp();
		// Berlin, AndreasstraÔøΩe 10
		mLocation = new Location("");
		mLocation.setLongitude(13.431240);
		mLocation.setLatitude(52.512523);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAAACleanDatabase() {
		final ContentResolver cr = getContext().getContentResolver();

		Uri uri = POIs.CONTENT_URI_ALL;
		Log.d(TAG, "deleting all at uri = " + uri);
		int count = cr.delete(uri, null, null);
		Log.d(TAG, "deleted records: count = " + count);
	}

	private ContentValues createDummyContentValues(String name) {
		ContentValues cv = new ContentValues();

		cv.put(Wheelmap.POIs.NAME, name);
		cv.put(Wheelmap.POIs.LATITUDE, Math.ceil(mLocation.getLatitude() * 1E6));
		cv.put(Wheelmap.POIs.LONGITUDE,
				Math.ceil(mLocation.getLongitude() * 1E6));
		cv.put(Wheelmap.POIs.CATEGORY_ID, 1);
		cv.put(Wheelmap.POIs.NODETYPE_ID, 1);

		return cv;
	}

	public void testAInsertFirstItemIntoRetrieved() {
		final ContentResolver cr = getContext().getContentResolver();

		Uri uri = POIs.CONTENT_URI_RETRIEVED;
		ContentValues values = createDummyContentValues("testA");
		cr.insert(uri, values);
		values = createDummyContentValues("testB");
		cr.insert(uri, values);
		values = createDummyContentValues("testC");
		cr.insert(uri, values);

		Cursor c = cr.query(uri, POIs.PROJECTION, null, null, null);
		assertEquals(3, c.getCount());

		// Util.dumpCursorToLog(TAG, c);
	}

	public void testBInsertFirstItemIntoCopy() {
		final ContentResolver cr = getContext().getContentResolver();

		Uri uri = POIs.CONTENT_URI_COPY;
		ContentValues values = createDummyContentValues("testA");
		cr.insert(uri, values);
		values = createDummyContentValues("testB");
		cr.insert(uri, values);
		values = createDummyContentValues("testC");
		cr.insert(uri, values);

		Cursor c = cr.query(uri, POIs.PROJECTION, null, null, null);
		assertEquals(3, c.getCount());

		// Util.dumpCursorToLog(TAG, c);
	}

	public void testCQueryAll() {
		final ContentResolver cr = getContext().getContentResolver();

		Uri uri = POIs.CONTENT_URI_ALL;
		Cursor c = cr.query(uri, POIs.PROJECTION, null, null, null);
		assertEquals(6, c.getCount());

		Util.dumpCursorToLog(TAG, c);
	}

	private String newName = "hallo - holla";

	public void testDReplaceAllTestANames() {
		final ContentResolver cr = getContext().getContentResolver();
		String whereClause = POIs.NAME + "= ?";
		String[] whereValues = new String[] { "testA" };
		ContentValues values = new ContentValues();
		values.put(POIs.NAME, newName);

		Uri uri = POIs.CONTENT_URI_ALL;
		int updated = cr.update(uri, values, whereClause, whereValues);
		Assert.assertEquals(2, updated);
		Log.d(TAG, "updated rows = " + updated);
	}

	public void testEReplaceCopyTestBNames() {
		final ContentResolver cr = getContext().getContentResolver();
		String whereClause = POIs.NAME + "= ?";
		String[] whereValues = new String[] { "testB" };
		ContentValues values = new ContentValues();
		values.put(POIs.NAME, newName);

		Uri uri = POIs.CONTENT_URI_COPY;
		int updated = cr.update(uri, values, whereClause, whereValues);
		Assert.assertEquals(1, updated);
		Log.d(TAG, "updated rows = " + updated);
	}

	public void testFReplaceRetrievedTestCNames() {
		final ContentResolver cr = getContext().getContentResolver();
		String whereClause = POIs.NAME + "= ?";
		String[] whereValues = new String[] { "testC" };
		ContentValues values = new ContentValues();
		values.put(POIs.NAME, newName);

		Uri uri = POIs.CONTENT_URI_COPY;
		int updated = cr.update(uri, values, whereClause, whereValues);
		Assert.assertEquals(1, updated);
		Log.d(TAG, "updated rows = " + updated);
	}

	public void testGQueryCopy() throws Exception {
		final ContentResolver cr = getContext().getContentResolver();
		String whereClause = POIs.NAME + "= ?";
		String[] whereValues = new String[] { newName };

		Uri uri = POIs.CONTENT_URI_ALL;
		Cursor c = cr.query(uri, POIs.PROJECTION, whereClause, whereValues,
				null);
		Assert.assertEquals(4, c.getCount());
		c.close();

		uri = POIs.CONTENT_URI_RETRIEVED;
		c = cr.query(uri, POIs.PROJECTION, whereClause, whereValues, null);
		Assert.assertEquals(1, c.getCount());

		final AtomicBoolean done = new AtomicBoolean();
		c.registerContentObserver(new ContentObserver(null) {

			@Override
			public void onChange(boolean selfChange) {
				done.set(true);
				Log.d(TAG, "ContentObeserver:onChange called");
			}

		});

		ContentValues values = new ContentValues();
		values.put(POIs.NAME, "das geht voran");
		int updated = cr.update(uri, values, whereClause, whereValues);
		Assert.assertEquals(1, updated);
		done.set(false);

		String newDescription = "schön schön schön";

		values.put(POIs.DESCRIPTION, newDescription);
		c.moveToFirst();
		long id = POIHelper.getId(c);
		Uri uriWithId = ContentUris.withAppendedId(POIs.CONTENT_URI_RETRIEVED,
				id);
		updated = cr.update(uriWithId, values, null, null);
		Assert.assertEquals(1, updated);

		String newWhereClause = POIs.DESCRIPTION + "= ?";
		String[] newWhereValues = new String[] { newDescription };

		c = cr.query(POIs.CONTENT_URI_RETRIEVED, POIs.PROJECTION,
				newWhereClause, newWhereValues, null);
		Assert.assertEquals(1, c.getCount());

		Awaitility.await().atMost(60, TimeUnit.SECONDS).untilTrue(done);
	}

}
