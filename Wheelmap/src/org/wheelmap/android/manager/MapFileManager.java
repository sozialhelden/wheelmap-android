package org.wheelmap.android.manager;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;
import org.wheelmap.android.model.MapFileInfo;
import org.wheelmap.android.model.MapFileInfoProvider;
import org.wheelmap.android.model.MapFileInfo.MapFileInfos;
import org.wheelmap.android.service.BaseListener;
import org.wheelmap.android.service.DownloadListener;
import org.wheelmap.android.service.FileListener;
import org.wheelmap.android.service.MapFileService;
import org.wheelmap.android.service.MapFileService.Task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ResultReceiver;

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

		INSTANCE.mInterrupted = false;
		INSTANCE.mMapFileService.start();
		return INSTANCE;
	}

	public void setResultReceiver(ResultReceiver receiver) {
		if (mMapFileService != null)
			mMapFileService.setResultReceiver(receiver);
	}

	public void stop() {
		mInterrupted = true;
		// mMapFileService.stop();
	}

	public void updateDatabaseWithRemote() {
		updateDatabaseWithRemoteRecursive("");
	}

	private void updateDatabaseWithRemoteRecursive(String dir) {

		DownloadListener listener = new DownloadListener() {
			private DownloadListener listener;

			@Override
			public void setListener(BaseListener listener) {
				this.listener = (DownloadListener) listener;
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
			public void onDirectoryContent(String dir, List<FTPFile> files) {
				for (FTPFile file : files) {
					Uri contentUri;
					String[] projection;
					if (file.getType() == FTPFile.DIRECTORY_TYPE) {
						contentUri = MapFileInfos.CONTENT_URI_DIRS;
						projection = MapFileInfos.dirPROJECTION;
					} else {
						if (file.getName().endsWith(".md5")) {
							continue;
						}

						contentUri = MapFileInfos.CONTENT_URI_FILES;
						projection = MapFileInfos.filePROJECTION;
					}
					String whereClause = "( " + MapFileInfos.REMOTE_NAME
							+ " = ? ) AND ( " + MapFileInfos.REMOTE_PARENT_NAME
							+ " = ? )";
					String[] whereValues = new String[] { file.getName(), dir };

					ContentValues values = new ContentValues();
					values.put(MapFileInfos.SCREEN_NAME,
							MapFileInfo.extractScreenName(file.getName()));
					values.put(MapFileInfos.REMOTE_NAME, file.getName());
					values.put(MapFileInfos.REMOTE_PARENT_NAME, dir);
					values.put(MapFileInfos.REMOTE_TIMESTAMP, MapFileInfo
							.formatDate(file.getTimestamp().getTime()));
					values.put(MapFileInfos.REMOTE_SIZE, file.getSize());
					values.put(MapFileInfos.REMOTE_MD5_SUM, "");
					values.put(MapFileInfos.VERSION,
							MapFileInfo.extractVersion(file.getName()));

					insertContentValues(contentUri, projection, whereClause,
							whereValues, values);

					if (file.getType() == FTPFile.DIRECTORY_TYPE
							&& !mInterrupted) {
						mMapFileService.getRemoteMapsDirectory(dir
								+ File.separator + file.getName(), this);
					}
				}
			}

			@Override
			public void onProgress(int percentageProgress) {
			}

			@Override
			public void onMD5Sum(String parentDir, String file, String md5sum) {

			}

			@Override
			public int getProgress() {

				return 0;
			}
		};
		mMapFileService.getRemoteMapsDirectory(dir, listener);
	}

	public void updateDatabaseWithLocal() {
		updateDatabaseWithLocalRecursive("");
	}

	private void updateDatabaseWithLocalRecursive(String dir) {

		FileListener listener = new FileListener() {
			private FileListener listener;

			@Override
			public void setListener(BaseListener listener) {
				this.listener = (FileListener) listener;
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
			public void onDirectoryContent(String parentDir, List<File> files) {
				for (File file : files) {
					if (mInterrupted)
						return;

					String whereClause = "( " + MapFileInfos.REMOTE_NAME
							+ " = ? )";
					String[] whereValues = new String[] { file.getName() };

					Uri contentUri;
					String[] projection;
					long remoteFileSize = -1;
					String lastModifiedRemoteTimestamp = null;
					int localAvailable = 0;
					if (file.isDirectory()) {
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
					values.put(MapFileInfos.NAME, file.getName());
					values.put(MapFileInfos.PARENT_NAME, parentDir);

					if (file.isFile()) {
						Date lastModified = new Date();
						lastModified.setTime(file.lastModified());
						String lastModifiedLocalTimestamp = MapFileInfo
								.formatDate(lastModified);
						values.put(MapFileInfos.LOCAL_TIMESTAMP,
								lastModifiedLocalTimestamp);

						if (lastModifiedLocalTimestamp
								.compareTo(lastModifiedRemoteTimestamp) > 0
								|| localAvailable == MapFileInfo.FILE_NOT_LOCAL) {
							if (file.length() == remoteFileSize)
								values.put(MapFileInfos.LOCAL_AVAILABLE,
										MapFileInfo.FILE_COMPLETE);
							else
								values.put(MapFileInfos.LOCAL_AVAILABLE,
										MapFileInfo.FILE_INCOMPLETE);
						}
					}

					insertContentValues(contentUri, projection, whereClause,
							whereValues, values);

					if (file.isDirectory() && !mInterrupted) {
						mMapFileService.getLocalMapsDirectory(parentDir
								+ File.separator + file.getName(), this);
					}
				}
			}

			@Override
			public void onMD5Sum(String parentDir, String file, String md5sum) {

			}
		};

		mMapFileService.getLocalMapsDirectory("", listener);
	}

	public void retrieveFile(final String dir, final String fileName,
			final DownloadListener listener) {

		DownloadListener localListener = new DownloadListener() {
			private DownloadListener listener;
			private int progress;

			@Override
			public void setListener(BaseListener listener) {
				this.listener = (DownloadListener) listener;
			}

			@Override
			public void onRunning() {
				if (listener != null)
					listener.onRunning();
			}

			@Override
			public void onFinished() {
				updateDatabaseWithLocal();
				if (listener != null)
					listener.onFinished();
			}

			@Override
			public void onDirectoryContent(String parentDir, List<FTPFile> files) {

			}

			@Override
			public void onProgress(int percentageProgress) {
				progress = percentageProgress;
				if (listener != null)
					listener.onProgress(percentageProgress);
			}

			@Override
			public void onMD5Sum(String parentDir, String file, String md5sum) {

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
		FileListener listener = new FileListener() {
			private FileListener listener;

			@Override
			public void setListener(BaseListener listener) {
				this.listener = (FileListener) listener;
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

			@Override
			public void onDirectoryContent(String parentDir, List<File> files) {

			}

			@Override
			public void onMD5Sum(String parentDir, String file, String md5sum) {

			}
		};

		mMapFileService.deleteLocalFile(dir, fileName, listener);
	}

	public void getRemoteMD5Sum(String dir, String fileName) {
		DownloadListener listener = new DownloadListener() {
			private DownloadListener listener;

			@Override
			public void setListener(BaseListener listener) {
				this.listener = (DownloadListener) listener;
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
			public void onDirectoryContent(String parentDir, List<FTPFile> files) {
			}

			@Override
			public void onProgress(int percentageProgress) {
			}

			@Override
			public void onMD5Sum(String parentDir, String file, String md5sum) {
				ContentValues values = new ContentValues();
				values.put(MapFileInfos.REMOTE_MD5_SUM, md5sum);
				String whereClause = "( " + MapFileInfos.REMOTE_NAME
						+ " = ? ) AND ( " + MapFileInfos.REMOTE_PARENT_NAME
						+ " = ? )";
				String[] whereValues = new String[] { file, parentDir };

				insertContentValues(MapFileInfos.CONTENT_URI_FILES,
						MapFileInfos.filePROJECTION, whereClause, whereValues,
						values);
			}

			@Override
			public int getProgress() {
				return 0;
			}

		};

		mMapFileService.getMD5Sum(dir, fileName, listener);
	}

	public void insertContentValues(Uri contentUri, String[] projection,
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
			String crrParentName = MapFileInfo.getRemoteParentName(cursor);
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
