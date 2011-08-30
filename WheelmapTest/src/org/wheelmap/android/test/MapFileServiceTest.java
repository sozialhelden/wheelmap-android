package org.wheelmap.android.test;

import java.util.List;

import junit.framework.Assert;

import org.wheelmap.android.service.MapFileService;
import org.wheelmap.android.service.MapFileService.FTPFileWithParent;
import org.wheelmap.android.service.MapFileService.FileWithParent;
import org.wheelmap.android.service.MapFileService.ReadDirectoryListener;
import org.wheelmap.android.service.MapFileService.RetrieveDirectoryListener;
import org.wheelmap.android.service.MapFileService.RetrieveFileListener;
import org.wheelmap.android.utils.DetachableResultReceiver;

import android.os.Bundle;
import android.os.Handler;
import android.test.AndroidTestCase;
import android.util.Log;

public class MapFileServiceTest extends AndroidTestCase implements DetachableResultReceiver.Receiver {
	private final static String TAG = "mapfileservice";
	
	private State mState;
	private MapFileService mDs;
	
	public void testDownloadService() throws InterruptedException {
		mState = new State();
		mState.mReceiver.setReceiver( this );
		mDs = new MapFileService();
		mDs.registerResultReceiver( mState.mReceiver );
		
		RetrieveDirectoryListener listenerOne = new RetrieveDirectoryListener() {
			
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
			public void onDirectoryContent(List<FTPFileWithParent> files) {
				for( FTPFileWithParent file: files ) {
					Log.d( TAG, "Parent = " + file.parentDir + " Name = " + file.file.getName());
				}
			}
		};
		
		mDs.getRemoteMapsDirectory( "", listenerOne );
		
		Thread.sleep( 20000 );
		
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
				// TODO Auto-generated method stub
				return 0;
			}
		};
		
		
		mDs.getRemoteFile( "europe/germany", "bremen-0.2.4.map", "europe/germany", "bremen-0.2.4.map", true, listenerTwo );
		
		ReadDirectoryListener listenerThree = new ReadDirectoryListener() {
			
			@Override
			public void setListener(
					org.wheelmap.android.service.MapFileService.BaseListener listener) {				
			}
			
			@Override
			public void onRunning() {
				
			}
			
			@Override
			public void onFinished() {
				
			}
			
			@Override
			public void onDirectoryContent(List<FileWithParent> files) {
				for( FileWithParent file: files ) {
					Log.d( TAG, "parentDir = " + file.parentDir + " fileName = " + file.file.getName() );
				}
			}
		};
		

		mDs.getLocalMapsDirectory( "", listenerThree );
		Thread.sleep( 30000 );

	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Log.d( TAG, "onReceiveResult" );
		if ( resultCode == MapFileService.STATUS_FINISHED ) {
			Log.d(TAG,  "Operation finished");
		}
		else if ( resultCode == MapFileService.STATUS_ERROR ) {
			Log.d( TAG, "onReceiveResult: resultData = " + resultData.getString( MapFileService.STATUS_ERROR_MSG));
			Assert.fail( resultData.getString( MapFileService.STATUS_ERROR_MSG ));
		}
	}
	
	private static class State {
		public DetachableResultReceiver mReceiver;
		public boolean mSyncing = false;

		private State() {
			mReceiver = new DetachableResultReceiver(new Handler());
		}
	}
}
