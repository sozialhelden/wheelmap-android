package org.wheelmap.android.test;

import org.wheelmap.android.model.MapFileInfo.MapFileInfos;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;
import android.util.Log;

public class MapFileInfoProviderTest extends AndroidTestCase {
	private final static String TAG = "mapfileinfoprovider";

	private ContentResolver mResolver;
	
	public void testDatabase() {
		mResolver = getContext().getContentResolver();
		
		mResolver.delete(MapFileInfos.CONTENT_URI_DIRS, null, null);
		mResolver.delete(MapFileInfos.CONTENT_URI_FILES, null, null );
		
		ContentValues cvOne = new ContentValues();
		cvOne.put( MapFileInfos.NAME, "testA" );
		cvOne.put( MapFileInfos.PARENT_NAME, "/maps" );
		cvOne.put( MapFileInfos.REMOTE_NAME, "testA" );
		cvOne.put( MapFileInfos.REMOTE_PARENT_NAME, "/maps" );
		
		mResolver.insert( MapFileInfos.CONTENT_URI_DIRS, cvOne );
		
		ContentValues cvTwo = new ContentValues();
		cvTwo.put( MapFileInfos.NAME, "testB" );
		cvTwo.put( MapFileInfos.PARENT_NAME, "/maps" );
		cvTwo.put( MapFileInfos.REMOTE_NAME, "testB" );
		cvTwo.put( MapFileInfos.REMOTE_PARENT_NAME, "/maps" );
		
		mResolver.insert( MapFileInfos.CONTENT_URI_DIRS, cvTwo );
		
		ContentValues cvThree = new ContentValues();
		cvThree.put( MapFileInfos.NAME, "testC" );
		cvThree.put( MapFileInfos.PARENT_NAME, "/maps" );
		cvThree.put( MapFileInfos.REMOTE_NAME, "testB" );
		cvThree.put( MapFileInfos.REMOTE_PARENT_NAME, "/maps" );
		cvThree.put( MapFileInfos.VERSION, "0.2.4" );
		cvThree.put( MapFileInfos.LOCAL_AVAILABLE, 1 );
		
		mResolver.insert( MapFileInfos.CONTENT_URI_FILES, cvThree );
		
		
		Cursor cursorOne = mResolver.query( MapFileInfos.CONTENT_URI_DIRS, MapFileInfos.dirPROJECTION, null, null, null);
		Log.d( TAG, "Dirs in database " + cursorOne.getCount());
		assertEquals( 2, cursorOne.getCount());
		
		Cursor cursorTwo = mResolver.query( MapFileInfos.CONTENT_URI_FILES, MapFileInfos.filePROJECTION, null, null, null );
		Log.d( TAG, "Files in database " + cursorTwo.getCount());
		assertEquals( 1, cursorTwo.getCount());
		
		Cursor cursorThree = mResolver.query( MapFileInfos.CONTENT_URI_DIRS, MapFileInfos.dirPROJECTION, MapFileInfos.NAME + " = ? ", new String[] { "testA" }, null);
		Log.d( TAG, "files in database " + cursorThree.getCount());
		assertEquals( 1, cursorThree.getCount());
		
		Cursor cursorFour = mResolver.query( MapFileInfos.CONTENT_URI_FILES, MapFileInfos.dirPROJECTION, MapFileInfos.NAME + " = ? ", new String[] { "testA" }, null);
		Log.d( TAG, "files in database " + cursorFour.getCount());
		assertEquals( 0, cursorFour.getCount());
	}
	
}
