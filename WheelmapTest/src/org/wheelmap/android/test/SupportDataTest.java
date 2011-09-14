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
		
		Thread.sleep( 20000 );
		
		cursor = cr.query( NodeTypesContent.CONTENT_URI, NodeTypesContent.PROJECTION, null, null, null);
		Log.d( TAG, "nodetypes cursor count = " + cursor.getCount());
	
	}
	
}
