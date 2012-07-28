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

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Assert;

import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Extra.What;
import org.wheelmap.android.model.Support.CategoriesContent;
import org.wheelmap.android.model.Support.LocalesContent;
import org.wheelmap.android.model.Support.NodeTypesContent;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.test.AndroidTestCase;
import android.util.Log;

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;

public class SupportDataTest extends AndroidTestCase {

	private final static String TAG = SupportDataTest.class.getSimpleName();

	private final static int WAIT_IN_SECONDS_TO_FINISH = 60;

	public void testAAA() {
		Log.d(TAG, "testAAA - clearing database");
		final ContentResolver cr = getContext().getContentResolver();
		cr.delete(LocalesContent.CONTENT_URI, null, null);
		cr.delete(CategoriesContent.CONTENT_URI, null, null);
		cr.delete(NodeTypesContent.CONTENT_URI, null, null);

	}

	public Uri getUriForWhat(int what) {
		if (what == What.RETRIEVE_LOCALES)
			return LocalesContent.CONTENT_URI;
		else if (what == What.RETRIEVE_CATEGORIES)
			return CategoriesContent.CONTENT_URI;
		else if (what == What.RETRIEVE_NODETYPES)
			return NodeTypesContent.CONTENT_URI;
		else
			throw new IllegalArgumentException("No such what: " + what
					+ " - no such uri.");
	}

	public String[] getProjectionForWhat(int what) {
		if (what == What.RETRIEVE_LOCALES)
			return LocalesContent.PROJECTION;
		else if (what == What.RETRIEVE_CATEGORIES)
			return CategoriesContent.PROJECTION;
		else if (what == What.RETRIEVE_NODETYPES)
			return NodeTypesContent.PROJECTION;
		else
			throw new IllegalArgumentException("No such what: " + what
					+ " - no such projection.");
	}

	public void testSupportData() throws Exception {
		Log.d(TAG, "testSupportData starting");

		final AtomicBoolean testDone = new AtomicBoolean();

		final ContentResolver cr = getContext().getContentResolver();

		ResultReceiver receiver = new ResultReceiver(null) {
			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				int what = resultData.getInt(Extra.WHAT);
				Log.d(TAG, "onReceiveResult in list resultCode = " + resultCode
						+ " for what = " + what);
				switch (resultCode) {
				case SyncService.STATUS_RUNNING: {
					Log.d(TAG, "retrieval running");
					break;
				}
				case SyncService.STATUS_FINISHED: {
					Log.d(TAG, "retrieval finished");
					Cursor cursor = cr.query(getUriForWhat(what),
							getProjectionForWhat(what), null, null, null);
					Log.d(TAG, "cursor count = " + cursor.getCount());
					Assert.assertFalse(cursor.getCount() == 0);
					// Util.dumpCursorToLog(TAG, cursor);
					cursor.close();
					testDone.set(what == What.RETRIEVE_NODETYPES);
					break;
				}
				case SyncService.STATUS_ERROR: {
					Log.d(TAG, "retrieval error");
					final SyncServiceException e = resultData
							.getParcelable(Extra.EXCEPTION);

					Log.e(TAG, "error: ", e);
					testDone.set(what == What.RETRIEVE_NODETYPES);

					break;
				}
				default: {
					// noop
				}
				}
			}
		};

		final Intent intent = new Intent(Intent.ACTION_SYNC, null,
				getContext(), SyncService.class);
		intent.putExtra(Extra.STATUS_RECEIVER, receiver);
		intent.putExtra(Extra.LOCALE, Locale.GERMAN.getLanguage());

		intent.putExtra(Extra.WHAT, What.RETRIEVE_LOCALES);
		Log.d(TAG, "starting service for locales request");
		getContext().startService(intent);

		intent.putExtra(Extra.WHAT, What.RETRIEVE_CATEGORIES);
		Log.d(TAG, "starting service for categories request");
		getContext().startService(intent);

		intent.putExtra(Extra.WHAT, What.RETRIEVE_NODETYPES);
		Log.d(TAG, "starting service for nodetypes request");
		getContext().startService(intent);

		Log.d(TAG, "waiting for finishing service requests");
		Awaitility
				.await()
				.atMost(new Duration(WAIT_IN_SECONDS_TO_FINISH,
						TimeUnit.SECONDS)).and().untilTrue(testDone);

		Log.d(TAG, "testSupportData done");
	}
}
