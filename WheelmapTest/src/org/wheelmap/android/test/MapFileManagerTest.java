package org.wheelmap.android.test;

import org.wheelmap.android.manager.MapFileManager;
import org.wheelmap.android.model.MapFileInfo.MapFileInfos;
import org.wheelmap.android.service.MapFileService.RetrieveFileListener;
import org.wheelmap.android.utils.DetachableResultReceiver;

import android.content.ContentResolver;
import android.os.Bundle;
import android.os.Handler;
import android.test.AndroidTestCase;
import android.util.Log;

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
		//MapFileManager dm = MapFileManager.get(getContext());
		//dm.registerResultReceiver( mState.mReceiver);

		//dm.update();
		Thread.sleep(20000);
		
		RetrieveFileListener listenerTwo = new RetrieveFileListener() {
			
			@Override
			public void setListener(
					org.wheelmap.android.service.MapFileService.BaseListener listener) {
			}
			
			@Override
			public void onRunning() {
				Log.d( TAG, "sendRunning" );				
			}
			
			@Override
			public void onFinished() {
				Log.d( TAG, "sendFinished" );				
			}
			
			@Override
			public void onProgress(int percentageProgress) {
				Log.d( TAG, "percentageProgress = " + percentageProgress  );
			}
			
			@Override
			public int getProgress() {
				return 0;
			}
		};
		
		//dm.retrieveFile( "europe", "luxembourg-0.2.4.map", listenerTwo );
		Thread.sleep( 70000 );
		
		//dm.deleteFile( "europe", "luxembourg-0.2.4.map" );
		Thread.sleep( 2000 );

		
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {

	}

	private static class State {
		public DetachableResultReceiver mReceiver;
		public boolean mSyncing = false;

		private State() {
			mReceiver = new DetachableResultReceiver(new Handler());
		}
	}

}
