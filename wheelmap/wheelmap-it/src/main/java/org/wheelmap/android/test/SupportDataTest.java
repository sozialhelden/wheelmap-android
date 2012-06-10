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

import org.wheelmap.android.model.Support.CategoriesContent;
import org.wheelmap.android.model.Support.LocalesContent;
import org.wheelmap.android.model.Support.NodeTypesContent;
import org.wheelmap.android.service.SyncService;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.test.AndroidTestCase;
import android.util.Log;

public class SupportDataTest extends AndroidTestCase {

	private final static String TAG = "executor";
	
	public void testSupportData() throws InterruptedException {
		ContentResolver cr = getContext().getContentResolver();
		
		// API does not provide working locales request
//		final Intent intent = new Intent(Intent.ACTION_SYNC, null, mContext, SyncService.class);
//		intent.putExtra(SyncService.EXTRA_WHAT, SyncService.WHAT_RETRIEVE_LOCALES );
//		mContext.startService(intent);
//		
//		Thread.sleep( 4000 );
//		
//		Cursor cursor = cr.query( LocalesContent.CONTENT_URI, LocalesContent.PROJECTION, null, null, null);
//		Log.d( TAG, "locales cursor count = " + cursor.getCount());
		
		Intent intent = new Intent(Intent.ACTION_SYNC, null, mContext, SyncService.class);
		intent.putExtra(SyncService.EXTRA_WHAT, SyncService.WHAT_RETRIEVE_CATEGORIES );
		mContext.startService(intent);
		
		Thread.sleep( 3000 );
		
		Cursor cursor = cr.query( CategoriesContent.CONTENT_URI, CategoriesContent.PROJECTION, null, null, null);
		Log.d( TAG, "categories cursor count = " + cursor.getCount());
	
		intent = new Intent(Intent.ACTION_SYNC, null, mContext, SyncService.class);
		intent.putExtra(SyncService.EXTRA_WHAT, SyncService.WHAT_RETRIEVE_NODETYPES );
		mContext.startService(intent);
		
		Thread.sleep( 25000 );
		
		cursor = cr.query( NodeTypesContent.CONTENT_URI, NodeTypesContent.PROJECTION, null, null, null);
		Log.d( TAG, "nodetypes cursor count = " + cursor.getCount());
	
	}
	
}
