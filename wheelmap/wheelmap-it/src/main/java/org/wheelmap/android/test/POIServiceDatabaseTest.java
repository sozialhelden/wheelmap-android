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

import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.PrepareDatabaseHelper;
import org.wheelmap.android.model.UserQueryHelper;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.service.SyncServiceHelper;

import wheelmap.org.WheelchairState;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.test.AndroidTestCase;
import android.util.Log;

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;

public class POIServiceDatabaseTest extends AndroidTestCase {
	private final static String TAG = POIServiceDatabaseTest.class
			.getSimpleName();
	private final static int WAIT_IN_SECONDS_TO_FINISH = 60;

	private LocationInfo location;

	public void createLocation() {
		// Berlin, Andreasstra�e 10
		location = new LocationInfo(getContext());
		location.lastLat = 52.512523f;
		location.lastLong = 13.431240f;

	}

	public void testARetrieveTestDataset() throws Exception {
		Log.d(TAG, "testPOIServiceDatabaseOne starting");

		final AtomicBoolean testDone = new AtomicBoolean();

		final ContentResolver cr = getContext().getContentResolver();
		cr.delete(POIs.CONTENT_URI_ALL, null, null);

		ResultReceiver receiver = new ResultReceiver(null) {
			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				Log.d(TAG, "onReceiveResult in list resultCode = " + resultCode);
				switch (resultCode) {
				case SyncService.STATUS_RUNNING: {
					Log.d(TAG, "retrieval running");

					break;
				}
				case SyncService.STATUS_FINISHED: {
					Log.d(TAG, "retrieval finished");
					Cursor cursor = cr.query(POIs.CONTENT_URI_RETRIEVED,
							POIs.PROJECTION, null, null, null);
					Log.d(TAG, "cursor count = " + cursor.getCount());
					Assert.assertFalse(cursor.getCount() == 0);
					Util.dumpCursorToLog(TAG, cursor);
					cursor.close();
					testDone.set(true);
					break;
				}
				case SyncService.STATUS_ERROR: {
					Log.d(TAG, "retrieval error");
					final SyncServiceException e = resultData
							.getParcelable(Extra.EXCEPTION);

					Log.e(TAG, "error: ", e);
					throw e;
				}
				default: {
					// noop
				}
				}
			}
		};

		createLocation();
		float distance = 0.2f;

		Log.d(TAG, "starting service for nodes request");
		SyncServiceHelper.retrieveNodesByDistance(getContext(), location,
				distance, receiver);

		Log.d(TAG, "waiting for finishing service requests");
		Awaitility
				.await()
				.atMost(new Duration(WAIT_IN_SECONDS_TO_FINISH,
						TimeUnit.SECONDS)).and().untilTrue(testDone);

		Log.d(TAG, "testSupportData done");
	}

	public void testATestSortedURI() {
		ContentResolver cr = getContext().getContentResolver();
		createLocation();

		Uri uri = POIs.createUriSorted(location);
		Cursor c = cr.query(uri, POIs.PROJECTION, null, null, null);
		Assert.assertFalse(c.getCount() == 0);
		c.close();

		String query = UserQueryHelper.getUserQuery();
		Log.d(TAG, "Query for excluded is *" + query + "*");
		c = cr.query(uri, POIs.PROJECTION, query, null, null);
		Assert.assertFalse(c.getCount() == 0);
		c.close();

	}

	private String wmIdToTest = "537132758";
	private String newName = "changed name";

	public void testBPrepareDatabaseHelperWithDatasetA() {
		final ContentResolver cr = getContext().getContentResolver();

		long id = PrepareDatabaseHelper.getRowIdForWMId(getContext()
				.getContentResolver(), wmIdToTest, POIs.TAG_RETRIEVED);
		Assert.assertFalse(id == Extra.ID_UNKNOWN);

		long idOfCopy = PrepareDatabaseHelper.createCopyIfNotExists(cr, id,
				false);
		Assert.assertFalse(idOfCopy == Extra.ID_UNKNOWN);

		ContentValues values = new ContentValues();
		values.put(POIs.NAME, newName);
		values.put(POIs.DIRTY, POIs.DIRTY_ALL);
		PrepareDatabaseHelper.editCopy(cr, idOfCopy, values);

		Cursor c = PrepareDatabaseHelper.queryDirty(getContext()
				.getContentResolver());
		Assert.assertEquals(1, c.getCount());
		c.moveToFirst();
		int dirty = c.getInt(c.getColumnIndexOrThrow(POIs.DIRTY));
		Assert.assertEquals(POIs.DIRTY_ALL, dirty);
		c.close();

		PrepareDatabaseHelper.markDirtyAsClean(getContext()
				.getContentResolver());
		c = PrepareDatabaseHelper.queryDirty(cr);
		Assert.assertEquals(0, c.getCount());
		c.close();

		// TEST on changed values
		c = PrepareDatabaseHelper.queryState(cr, POIs.STATE_CHANGED);
		Assert.assertEquals(1, c.getCount());
		int state = c.getInt(c.getColumnIndexOrThrow(POIs.STATE));
		Assert.assertEquals(POIs.STATE_CHANGED, state);
		c.close();

		values.clear();
		values.put(POIs.WHEELCHAIR, WheelchairState.YES.getId());
		values.put(POIs.DIRTY, POIs.DIRTY_STATE);
		PrepareDatabaseHelper.editCopy(cr, idOfCopy, values);

		PrepareDatabaseHelper.markDirtyAsClean(getContext()
				.getContentResolver());

	}

	public void testCPrepareDatabaseHelperWithDatasetB() {
		final ContentResolver cr = getContext().getContentResolver();

		/*
		 * new dataset
		 */
		String newWmIdToTest = "424236321";

		long id = PrepareDatabaseHelper.getRowIdForWMId(getContext()
				.getContentResolver(), newWmIdToTest, POIs.TAG_RETRIEVED);
		Assert.assertFalse(id == Extra.ID_UNKNOWN);

		long idOfCopy = PrepareDatabaseHelper.createCopyIfNotExists(cr, id,
				false);
		Assert.assertFalse(idOfCopy == Extra.ID_UNKNOWN);
		Cursor c = PrepareDatabaseHelper.queryState(getContext()
				.getContentResolver(), POIs.STATE_UNCHANGED);
		Assert.assertEquals(1, c.getCount());
		int state = c.getInt(c.getColumnIndexOrThrow(POIs.STATE));
		Assert.assertEquals(POIs.STATE_UNCHANGED, state);
		c.close();

		PrepareDatabaseHelper.cleanupOldCopies(getContext()
				.getContentResolver());

		c = PrepareDatabaseHelper.queryState(cr, POIs.STATE_UNCHANGED);
		Assert.assertEquals(0, c.getCount());
		c.close();

	}

	public void testDPrepareDatabaseHelperWithDatasetA() {
		final ContentResolver cr = getContext().getContentResolver();

		Cursor c = PrepareDatabaseHelper.queryState(getContext()
				.getContentResolver(), POIs.STATE_CHANGED);
		Assert.assertEquals(1, c.getCount());
		c.close();

		PrepareDatabaseHelper.replayChangedCopies(getContext()
				.getContentResolver());

		long id = PrepareDatabaseHelper.getRowIdForWMId(getContext()
				.getContentResolver(), wmIdToTest, POIs.TAG_RETRIEVED);
		Assert.assertFalse(id == Extra.ID_UNKNOWN);

		Uri uri = ContentUris.withAppendedId(POIs.CONTENT_URI_RETRIEVED, id);
		c = cr.query(uri, POIs.PROJECTION, null, null, null);
		c.moveToFirst();
		Assert.assertEquals(newName, POIHelper.getName(c));
	}

	public void testEPrepareDatabaseHelperMisc() {
		final ContentResolver cr = getContext().getContentResolver();
		PrepareDatabaseHelper.deleteRetrievedData(cr);

		Cursor c = PrepareDatabaseHelper.queryState(cr, POIs.STATE_CHANGED);
		Assert.assertEquals(1, c.getCount());
		c.close();
		c = PrepareDatabaseHelper.queryState(cr, POIs.STATE_UNCHANGED);
		Assert.assertEquals(0, c.getCount());
		c.close();
		c = PrepareDatabaseHelper.queryDirty(cr);
		Assert.assertEquals(0, c.getCount());
		c.close();
	}
}
