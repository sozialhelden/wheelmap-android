package org.wheelmap.android.test;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.net.ftp.FTPFile;
import org.wheelmap.android.service.BaseListener;
import org.wheelmap.android.service.DownloadListener;
import org.wheelmap.android.service.FileListener;
import org.wheelmap.android.service.MapFileService;
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
		mDs.setResultReceiver( mState.mReceiver );
		
		DownloadListener listenerOne = new DownloadListener() {

			@Override
			public void onRunning() {
				Log.d( TAG, "sendRunning" );
			}

			@Override
			public void onFinished() {
				Log.d( TAG, "sendFinished" );
			}

			@Override
			public void onDirectoryContent(String dir, List<FTPFile> files) {
				Log.d( TAG, "dir = " + dir + " Number of files = " + files.size());
				for( FTPFile file: files ) {
					Log.d( TAG, "File: " + file.getName() );
					if ( file.getType() == FTPFile.DIRECTORY_TYPE)
						mDs.getRemoteMapsDirectory( dir + File.separator + file.getName(), this);
				}
			}

			@Override
			public void onProgress(int percentageProgress) {
				
			}

			@Override
			public void onMD5Sum(String parentDir, String file, String md5sum) {
				
			}

			@Override
			public void setListener(BaseListener listener) {
				
			}

			@Override
			public int getProgress() {
				return 0;
			}
		};
		
		mDs.getRemoteMapsDirectory( "", listenerOne );
		
		Thread.sleep( 20000 );
		
		DownloadListener listenerTwo = new DownloadListener() {

			@Override
			public void onRunning() {
				Log.d( TAG, "sendRunning" );				
			}

			@Override
			public void onFinished() {
				Log.d( TAG, "sendFinished" );
			}

			@Override
			public void onDirectoryContent(String dir, List<FTPFile> files) {
				
			}

			@Override
			public void onProgress(int percentageProgress) {
				Log.d( TAG, "percentageProgress = " + percentageProgress  );
			}

			@Override
			public void onMD5Sum(String parentDir, String file, String md5sum) {
				Log.d( TAG, "md5Sum: parentDir = " + parentDir + " file = " + file + " MD5 = " + md5sum );
			}

			@Override
			public void setListener(BaseListener listener) {
				
			}

			@Override
			public int getProgress() {
				return 0;
			}

			
		};
		
		mDs.getRemoteFile( "europe/germany", "bremen-0.2.4.map", "europe/germany", "bremen-0.2.4.map", true, listenerTwo );
		mDs.getMD5Sum( "europe/germany", "bremen-0.2.4.map.md5", listenerTwo );
		
		
		FileListener listenerThree = new FileListener() {

			@Override
			public void onRunning() {
				
			}

			@Override
			public void onFinished() {
				
			}

			@Override
			public void onDirectoryContent(String parentDir, List<File> files) {
				for( File file: files ) {
					Log.d( TAG, "parentDir = " + parentDir + " fileName = " + file.getName() );
					if ( file.isDirectory()) {
						mDs.getLocalMapsDirectory( parentDir + File.separator + file.getName(), this );
					}
				}
				
				
			}

			@Override
			public void onMD5Sum(String parentDir, String file, String md5sum) {
				
			}

			@Override
			public void setListener(BaseListener listener) {
				
			}	
		};
		
		mDs.getLocalMapsDirectory( "", listenerThree );
		Thread.sleep( 20000 );

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
