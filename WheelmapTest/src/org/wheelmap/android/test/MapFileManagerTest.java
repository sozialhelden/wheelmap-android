package org.wheelmap.android.test;

import org.wheelmap.android.manager.MapFileManager;
import org.wheelmap.android.model.MapFileInfo.MapFileInfos;
import org.wheelmap.android.utils.DetachableResultReceiver;

import android.content.ContentResolver;
import android.os.Bundle;
import android.os.Handler;
import android.test.AndroidTestCase;

public class MapFileManagerTest extends AndroidTestCase implements
		DetachableResultReceiver.Receiver {

	private final static String TAG = "mapfilemanager";
	private State mState;

	private ContentResolver mResolver;

	@Override
	public void testAndroidTestCaseSetupProperly() {
		super.testAndroidTestCaseSetupProperly();

		mResolver = getContext().getContentResolver();

		mResolver.delete(MapFileInfos.CONTENT_URI_DIRS, null, null);
		mResolver.delete(MapFileInfos.CONTENT_URI_FILES, null, null);
	}

	public void testMapFileManagerOne() throws InterruptedException {

		mState = new State();
		mState.mReceiver.setReceiver(this);
		MapFileManager dm = MapFileManager.get(getContext());
		dm.registerResultReceiver( mState.mReceiver);

		dm.update();
		Thread.sleep(15000);
		
		dm.retrieveFile( "europe", "luxembourg-0.2.4.map", null );
		Thread.sleep( 50000 );
		
		dm.deleteFile( "europe", "luxembourg-0.2.4.map" );
		Thread.sleep( 2000 );

	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		// TODO Auto-generated method stub

	}

	private static class State {
		public DetachableResultReceiver mReceiver;
		public boolean mSyncing = false;

		private State() {
			mReceiver = new DetachableResultReceiver(new Handler());
		}
	}

}
