package org.wheelmap.android.manager;

import java.util.Date;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;
import org.wheelmap.android.model.MapFileInfo;
import org.wheelmap.android.model.MapFileInfo.MapFileInfos;
import org.wheelmap.android.service.MapFileService;
import org.wheelmap.android.service.MapFileService.BaseListener;
import org.wheelmap.android.service.MapFileService.FTPFileWithParent;
import org.wheelmap.android.service.MapFileService.FileWithParent;
import org.wheelmap.android.service.MapFileService.ReadDirectoryListener;
import org.wheelmap.android.service.MapFileService.RetrieveDirectoryListener;
import org.wheelmap.android.service.MapFileService.RetrieveFileListener;
import org.wheelmap.android.service.MapFileService.Task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ResultReceiver;
import android.util.Log;

public class MapFileManager {
	private final static String TAG = "mapfilemanager";

	private static MapFileManager INSTANCE;
	private ContentResolver mResolver;
	private MapFileService mMapFileService;
	private boolean mInterrupted;

	private MapFileManager(Context appCtx) {
		mResolver = appCtx.getContentResolver();
		mMapFileService = new MapFileService();

	}

	public static MapFileManager get(Context appCtx) {
		if (INSTANCE == null)
			INSTANCE = new MapFileManager(appCtx);

		INSTANCE.init();
		return INSTANCE;
	}

	private void init() {
		if (mMapFileService == null) {
			mMapFileService = new MapFileService();
			mMapFileService.start();
		}
		mInterrupted = false;
	}

	public void release() {
		if ( mMapFileService.resultReceiverClients() == 0) {
			mInterrupted = true;
			mMapFileService.stop();
			mMapFileService = null;
		}
	}

	public void registerResultReceiver(ResultReceiver receiver) {
		if (mMapFileService != null)
			mMapFileService.registerResultReceiver(receiver);
	}

	public void unregisterResultReceiver(ResultReceiver receiver) {
		if (mMapFileService != null)
			mMapFileService.unregisterResultReceiver(receiver);
	}

	public void update() {
		insertUpdateTag();
		updateDatabaseWithLocal();
		updateDatabaseWithRemote();
		purgeWithoutUpdateTag();
	}
	
	public void updateLocal() {
		updateDatabaseWithLocal();
	}

	private void updateDatabaseWithRemote() {

		RetrieveDirectoryListener listener = new RetrieveDirectoryListener() {
			RetrieveDirectoryListener listener;

			@Override
			public void setListener(
					org.wheelmap.android.service.MapFileService.BaseListener listener) {
				this.listener = (RetrieveDirectoryListener) listener;
			}

			@Override
			public void onRunning() {
				if (listener != null) {
					listener.onRunning();
				}
			}

			@Override
			public void onFinished() {
				if (listener != null) {
					listener.onFinished();
				}
			}

			@Override
			public void onDirectoryContent(List<FTPFileWithParent> files) {
				for (FTPFileWithParent fileWithParent : files) {
					Uri contentUri;
					String[] projection;
					if (fileWithParent.file.getType() == FTPFile.DIRECTORY_TYPE) {
						contentUri = MapFileInfos.CONTENT_URI_DIRS;
						projection = MapFileInfos.dirPROJECTION;
					} else {
						if (fileWithParent.file.getName().endsWith(".md5")) {
							continue;
						}

						contentUri = MapFileInfos.CONTENT_URI_FILES;
						projection = MapFileInfos.filePROJECTION;
					}

					String whereClause = "( " + MapFileInfos.NAME
							+ " = ? ) AND ( " + MapFileInfos.PARENT_NAME
							+ " = ? )";
					String[] whereValues = new String[] {
							fileWithParent.file.getName(),
							fileWithParent.parentDir };

					ContentValues values = new ContentValues();
					values.put(MapFileInfos.SCREEN_NAME, MapFileInfo
							.extractScreenName(fileWithParent.file.getName()));
					values.put(MapFileInfos.REMOTE_NAME,
							fileWithParent.file.getName());
					values.put(MapFileInfos.REMOTE_PARENT_NAME,
							fileWithParent.parentDir);
					values.put(MapFileInfos.REMOTE_TIMESTAMP, MapFileInfo
							.formatDate(fileWithParent.file.getTimestamp()
									.getTime()));
					values.put(MapFileInfos.REMOTE_SIZE,
							fileWithParent.file.getSize());
					values.put(MapFileInfos.VERSION, MapFileInfo
							.extractVersion(fileWithParent.file.getName()));
					values.put(MapFileInfos.UPDATE_TAG,
							MapFileInfo.ENTRY_UPDATED);

					insertContentValues(contentUri, projection, whereClause,
							whereValues, values);
				}
			}
		};

		if (!mInterrupted)
			mMapFileService.getRemoteMapsDirectory("", listener);

	}

	private void updateDatabaseWithLocal() {
		
		ReadDirectoryListener listener = new ReadDirectoryListener() {
			private ReadDirectoryListener listener;
			
			@Override
			public void setListener(BaseListener listener) {
				this.listener = (ReadDirectoryListener) listener;
			}
			
			@Override
			public void onRunning() {
				if (listener != null)
					listener.onRunning();				
			}
			
			@Override
			public void onFinished() {
				if (listener != null)
					listener.onFinished();				
			}
			
			@Override
			public void onDirectoryContent(List<FileWithParent> files) {
				for( FileWithParent fileWithParent: files ) {
					String whereClause = "( " + MapFileInfos.REMOTE_NAME
							+ " = ? )";
					String[] whereValues = new String[] { fileWithParent.file.getName() };

					Uri contentUri;
					String[] projection;
					long remoteFileSize = -1;
					String lastModifiedRemoteTimestamp = null;
					int localAvailable = 0;
					if (fileWithParent.file.isDirectory()) {
						contentUri = MapFileInfos.CONTENT_URI_DIRS;
						projection = MapFileInfos.dirPROJECTION;
					} else {
						contentUri = MapFileInfos.CONTENT_URI_FILES;
						projection = MapFileInfos.filePROJECTION;

						Cursor cursor = mResolver.query(contentUri,
								MapFileInfos.filePROJECTION, whereClause,
								whereValues, null);
						if (cursor.getCount() == 1) {
							cursor.moveToFirst();
							remoteFileSize = MapFileInfo.getRemoteSize(cursor);
							lastModifiedRemoteTimestamp = MapFileInfo
									.getRemoteTimestamp(cursor);
							localAvailable = MapFileInfo
									.getLocalAvailable(cursor);
						}
					}

					ContentValues values = new ContentValues();
					values.put(MapFileInfos.SCREEN_NAME,
							MapFileInfo.extractScreenName(fileWithParent.file.getName()));
					values.put(MapFileInfos.NAME, fileWithParent.file.getName());
					values.put(MapFileInfos.PARENT_NAME, fileWithParent.parentDir);
					values.put(MapFileInfos.UPDATE_TAG,
							MapFileInfo.ENTRY_UPDATED);

					if (fileWithParent.file.isFile()) {
						Date lastModified = new Date();
						lastModified.setTime(fileWithParent.file.lastModified());
						String lastModifiedLocalTimestamp = MapFileInfo
								.formatDate(lastModified);
						values.put(MapFileInfos.LOCAL_TIMESTAMP,
								lastModifiedLocalTimestamp);
						Log.d(TAG, "file = " + fileWithParent.file.getName()
								+ " localAvailable = " + localAvailable);
						if (localAvailable == MapFileInfo.FILE_NOT_LOCAL
								|| lastModifiedRemoteTimestamp == null) {
							values.put(MapFileInfos.LOCAL_AVAILABLE,
									MapFileInfo.FILE_COMPLETE);
						} else if (lastModifiedLocalTimestamp
								.compareTo(lastModifiedRemoteTimestamp) > 0) {
							if (fileWithParent.file.length() == remoteFileSize)
								values.put(MapFileInfos.LOCAL_AVAILABLE,
										MapFileInfo.FILE_COMPLETE);
							else
								values.put(MapFileInfos.LOCAL_AVAILABLE,
										MapFileInfo.FILE_INCOMPLETE);
						}
					}

					insertContentValues(contentUri, projection, whereClause,
							whereValues, values);
					
				}
				
			}
		};		
		
		if (!mInterrupted)
			mMapFileService.getLocalMapsDirectory("", listener);
	}

	public void retrieveFile(final String dir, final String fileName,
			final RetrieveFileListener listener ) {
		
		RetrieveFileListener localListener = new RetrieveFileListener() {
			private int progress;
			RetrieveFileListener listener;

			@Override
			public void setListener(BaseListener listener) {
				this.listener = (RetrieveFileListener) listener;
			}

			@Override
			public void onRunning() {
				if (listener != null)
					listener.onRunning();				
			}

			@Override
			public void onFinished() {
				if (mInterrupted)
					return;

				updateDatabaseWithLocal();
				if (listener != null)
					listener.onFinished();				
			}

			@Override
			public void onProgress(int percentageProgress) {
				progress = percentageProgress;
				if (listener != null)
					listener.onProgress(percentageProgress);				
			}

			@Override
			public int getProgress() {
				return progress;
			}
			
		};
		
		localListener.setListener(listener);
		mMapFileService.getRemoteFile(dir, fileName, dir, fileName, false,
				localListener);
		
	}

	public void deleteFile(final String dir, final String fileName) {
		BaseListener listener = new BaseListener() {
			private BaseListener listener;

			@Override
			public void setListener(BaseListener listener) {
				this.listener = (BaseListener) listener;
			}

			@Override
			public void onRunning() {
				if (listener != null) {
					listener.onRunning();
				}
			}

			@Override
			public void onFinished() {
				ContentValues values = new ContentValues();
				values.put(MapFileInfos.LOCAL_AVAILABLE,
						MapFileInfo.FILE_NOT_LOCAL);
				String whereClause = "( " + MapFileInfos.REMOTE_NAME
						+ " = ? ) ";
				String[] whereValues = new String[] { fileName };

				insertContentValues(MapFileInfos.CONTENT_URI_FILES,
						MapFileInfos.filePROJECTION, whereClause, whereValues,
						values);

				if (listener != null) {
					listener.onFinished();
				}

			}
		};

		mMapFileService.deleteLocalFile(dir, fileName, listener);
	}

	private void insertUpdateTag() {
		mMapFileService.post(new Runnable() {

			@Override
			public void run() {
				ContentValues values = new ContentValues();
				values.put(MapFileInfos.UPDATE_TAG,
						MapFileInfo.ENTRY_NOT_UPDATED);
				mResolver.update(MapFileInfos.CONTENT_URI_DIRSNFILES, values,
						null, null);
			}

		});
	}

	private void purgeWithoutUpdateTag() {
		mMapFileService.post(new Runnable() {

			public void run() {
				String whereClause = "( " + MapFileInfos.UPDATE_TAG + " = ? )";
				String[] whereValues = { String
						.valueOf(MapFileInfo.ENTRY_NOT_UPDATED) };

				mResolver.delete(MapFileInfos.CONTENT_URI_DIRSNFILES,
						whereClause, whereValues);
			}
		});
	}

	private void insertContentValues(Uri contentUri, String[] projection,
			String whereClause, String[] whereValues, ContentValues values) {
		Cursor c = mResolver.query(contentUri, projection, whereClause,
				whereValues, null);
		int cursorCount = c.getCount();
		if (cursorCount == 0)
			mResolver.insert(contentUri, values);
		else if (cursorCount == 1)
			mResolver.update(contentUri, values, whereClause, whereValues);
		else {
			// do nothing, as more than one file would be updated
		}
	}

	public String getRootDirectory() {
		Cursor cursor = mResolver.query(MapFileInfos.CONTENT_URI_DIRS,
				MapFileInfos.dirPROJECTION, null, null, null);
		cursor.moveToFirst();

		String rootDirName = null;
		while (!cursor.isAfterLast()) {
			String crrParentName = MapFileInfo.getRemoteParentName( cursor );
			if (rootDirName == null)
				rootDirName = crrParentName;
			else if (crrParentName.length() < rootDirName.length())
				rootDirName = crrParentName;
			cursor.moveToNext();
		}

		if (rootDirName == null)
			rootDirName = "";

		return rootDirName;
	}

	public Task findTask(String name, String parentName, int taskType) {
		return mMapFileService.findTask(name, parentName, taskType);
	}

}
