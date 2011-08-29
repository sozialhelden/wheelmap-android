package org.wheelmap.android.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;
import org.wheelmap.android.net.MapsforgeFTP;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.MultiResultReceiver;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.util.Log;

public class MapFileService {
	private final static String TAG = "mapfileservice";

	public static final String EXTRA_STATUS_RECEIVER = "org.wheelmap.android.STATUS_RECEIVER";
	public static final String EXTRA_STATUS_DOWNLOAD_PROGRESS = "org.wheelmap.android.net.DOWNLOAD_PROGRESS";

	private static final String EXTRA_REMOTE_DIR = "org.wheelmap.android.net.MapFileService.REMOTE_PATH";
	private static final String EXTRA_REMOTE_FILE = "org.wheelmap.android.net.MapFileService.REMOTE_FILE";
	private static final String EXTRA_LOCAL_FILE = "org.wheelmap.android.net.MapFileService.LOCAL_FILE";
	private static final String EXTRA_LOCAL_DIR = "org.wheelmap.android.net.MapFileService.LOCAL_DIR";
	private static final String EXTRA_DOWNLOAD_FORCE = "org.wheelmap.android.net.MapFileService.FORCE_DOWNLOAD";
	private static final int WHAT_RETRIEVE_DIRECTORY = 0x1;
	private static final int WHAT_RETRIEVE_FILE = 0x2;
	private static final int WHAT_RETRIEVE_MD5SUM = 0x3;
	private static final int WHAT_READ_DIRECTORY = 0x4;
	private static final int WHAT_DELETE_FILE = 0x5;
	private static final int WHAT_CALC_MD5SUM = 0x6;

	public static final int STATUS_RUNNING = 0x1;
	public static final int STATUS_ERROR = 0x2;
	public static final int STATUS_FINISHED = 0x3;
	public static final String STATUS_ERROR_MSG = "org.wheelmap.android.net.MapFileService.STATUS_ERROR_MSG";

	private MultiResultReceiver mReceiver;
	private MyHandler mHandler;
	private HandlerThread mHandlerThread;

	private static final String REMOTE_BASE_PATH_DIR = File.separator + "maps";
	public static String LOCAL_BASE_PATH_DIR = File.separator + "sdcard"
			+ File.separator + "wheelmap" + File.separator + "maps";

	public static class Task {
		public static final int TYPE_UNKNOWN = 0x0;
		public static final int TYPE_RETRIEVE_DIR = 0x1;
		public static final int TYPE_RETRIEVE_FILE = 0x2;
		public static final int TYPE_RETRIEVE_MD5SUM = 0x3;
		public static final int TYPE_CALC_MD5SUM = 0x4;
		public static final int TYPE_DELETE_FILE = 0x5;
		public static final int TYPE_READ_DIR = 0x6;

		public int type;
		public String name;
		public String parentName;
		public BaseListener listener;

		Task(int type, String parentName, String name, BaseListener listener) {
			this.type = type;
			this.name = name;
			this.parentName = parentName;
			this.listener = listener;
		}
	}

	private final LinkedList<Task> queue;

	public MapFileService() {
		queue = new LinkedList<Task>();
		mReceiver = new MultiResultReceiver(new Handler());
		start();
	}

	public void registerResultReceiver(ResultReceiver receiver) {
		mReceiver.addReceiver(receiver, true);
	}

	public void unregisterResultReceiver(ResultReceiver receiver) {
		mReceiver.removeReceiver(receiver);
	}

	public void post(Runnable runnable) {
		mHandler.post(new MapFileRunnable(runnable));
	}

	public class MapFileRunnable implements Runnable {
		Runnable r;

		MapFileRunnable(Runnable r) {
			this.r = r;
		}

		@Override
		public void run() {
			if (mReceiver != null)
				mReceiver.send(STATUS_RUNNING, Bundle.EMPTY);

			r.run();

			if (mReceiver != null)
				mReceiver.send(STATUS_FINISHED, Bundle.EMPTY);
		}

	}

	public void addTaskAtEnd(Task task) {
		synchronized (queue) {
			queue.add(task);
		}
	}

	public void addTaskAtFront(Task task) {
		synchronized (queue) {
			queue.addFirst(task);
		}
	}

	public void removeTaskFromFront() {
		synchronized (queue) {
			queue.removeFirst();
		}
	}

	public void start() {
		if (mHandlerThread == null) {
			mHandlerThread = new HandlerThread("MapFileService");

		}

		if (!mHandlerThread.isAlive()) {
			mHandlerThread.start();
			mHandler = new MyHandler(mHandlerThread.getLooper());
			mHandler.connect();
		}
	}

	public void stop() {
		if (mHandlerThread != null) {
			mHandler.interrupt();
			mHandler.disconnect();
		}
	}

	public Task findTask(String name, String parentName, int taskType) {
		synchronized (queue) {
			Iterator<Task> iter = queue.iterator();
			while (iter.hasNext()) {
				Task crrTask = iter.next();
				if (((taskType == Task.TYPE_UNKNOWN) || (taskType == crrTask.type))
						&& name.equals(crrTask.name)
						&& parentName.equals(crrTask.parentName))
					return crrTask;
			}
		}
		return null;
	}

	public String adjustDir(String dir, String basePath) {
		if (dir == null || dir.equals(""))
			return basePath;
		else if (dir.startsWith(basePath))
			return dir;
		else
			return basePath + (dir.startsWith("/") ? "" : File.separator) + dir;
	}

	public void getRemoteMapsDirectory(String remoteDir,
			DownloadListener listener) {
		Message msg = mHandler.obtainMessage();
		msg.what = WHAT_RETRIEVE_DIRECTORY;
		msg.obj = listener;
		Bundle b = new Bundle();
		b.putString(EXTRA_REMOTE_DIR, remoteDir);
		msg.setData(b);
		Task task = new Task(Task.TYPE_RETRIEVE_DIR, remoteDir, "", listener);

		addTaskAtEnd(task);
		mHandler.sendMessage(msg);
	}

	public void getRemoteFile(String localDir, String localFile,
			String remoteDir, String remoteFile, boolean force,
			DownloadListener listener) {
		Message msg = mHandler.obtainMessage();
		msg.what = WHAT_RETRIEVE_FILE;
		msg.obj = listener;
		Bundle b = new Bundle();
		b.putString(EXTRA_REMOTE_DIR, remoteDir);
		b.putString(EXTRA_REMOTE_FILE, remoteFile);
		b.putString(EXTRA_LOCAL_DIR, localDir);
		b.putString(EXTRA_LOCAL_FILE, localFile);
		b.putBoolean(EXTRA_DOWNLOAD_FORCE, force);

		msg.setData(b);
		addTaskAtEnd(new Task(Task.TYPE_RETRIEVE_FILE, remoteDir, remoteFile,
				listener));
		mHandler.sendMessage(msg);
	}

	public void getMD5Sum(String remoteDir, String remoteFile,
			DownloadListener listener) {
		Message msg = mHandler.obtainMessage();
		msg.what = WHAT_RETRIEVE_MD5SUM;
		msg.obj = listener;
		Bundle b = new Bundle();
		b.putString(EXTRA_REMOTE_DIR, remoteDir);
		b.putString(EXTRA_REMOTE_FILE, remoteFile);
		msg.setData(b);
		addTaskAtEnd(new Task(Task.TYPE_RETRIEVE_FILE, remoteDir, remoteFile,
				listener));
		mHandler.sendMessage(msg);
	}

	public void getLocalMapsDirectory(String localDir, FileListener listener) {
		Message msg = mHandler.obtainMessage();
		msg.what = WHAT_READ_DIRECTORY;
		msg.obj = listener;
		Bundle b = new Bundle();
		b.putString(EXTRA_LOCAL_DIR, localDir);
		msg.setData(b);
		addTaskAtEnd(new Task(Task.TYPE_READ_DIR, localDir, "", listener));
		mHandler.sendMessage(msg);
	}

	public void deleteLocalFile(String localDir, String localFile,
			FileListener listener) {
		Message msg = mHandler.obtainMessage();
		msg.what = WHAT_DELETE_FILE;
		msg.obj = listener;
		Bundle b = new Bundle();
		b.putString(EXTRA_LOCAL_DIR, localDir);
		b.putString(EXTRA_LOCAL_FILE, localFile);
		msg.setData(b);
		addTaskAtFront(new Task(Task.TYPE_DELETE_FILE, localDir, localFile,
				listener));
		mHandler.sendMessageAtFrontOfQueue(msg);
	}

	public void calcMD5Sum(String localDir, String localFile,
			FileListener listener) {
		Message msg = mHandler.obtainMessage();
		msg.what = WHAT_CALC_MD5SUM;
		msg.obj = listener;
		Bundle b = new Bundle();
		b.putString(EXTRA_LOCAL_DIR, localDir);
		b.putString(EXTRA_LOCAL_FILE, localFile);
		msg.setData(b);
		addTaskAtEnd(new Task(Task.TYPE_CALC_MD5SUM, localDir, localFile,
				listener));
		mHandler.sendMessage(msg);
	}

	private class MyHandler extends Handler {
		MapsforgeFTP mFTPClient;

		public MyHandler(Looper looper) {
			super(looper);
			mFTPClient = new MapsforgeFTP();
		}

		public void connect() {
			try {
				mFTPClient.connect();
			} catch (Exception e) {
				Bundle bStatus = new Bundle();
				bStatus.putString(STATUS_ERROR_MSG, e.getMessage());
				if (mReceiver != null)
					mReceiver.send(STATUS_ERROR, bStatus);
			}
		}

		public void disconnect() {
			try {
				mFTPClient.diconnect();
			} catch (IOException e) {
				Bundle bStatus = new Bundle();
				bStatus.putString(STATUS_ERROR_MSG, e.getMessage());
				if (mReceiver != null)
					mReceiver.send(STATUS_ERROR, bStatus);
			}
		}

		public void interrupt() {
			mFTPClient.interrupt();
		}

		@Override
		public void handleMessage(Message msg) {
			if (mReceiver != null)
				mReceiver.send(STATUS_RUNNING, Bundle.EMPTY);
			if (!mFTPClient.isConnected()) {
				disconnect();
				connect();
			}
			BaseListener listener = (BaseListener) msg.obj;
			if (listener != null)
				listener.onRunning();
			Bundle b = msg.getData();

			try {
				switch (msg.what) {
				case WHAT_RETRIEVE_DIRECTORY: {
					String remoteDirPath = b.getString(EXTRA_REMOTE_DIR);
					FTPFile[] ftpFiles = null;
					ftpFiles = mFTPClient.getDir(adjustDir(remoteDirPath,
							REMOTE_BASE_PATH_DIR));
					if (ftpFiles != null) {
						List<FTPFile> ftpFilesList = Arrays.asList(ftpFiles);
						if (listener != null)
							((DownloadListener) listener).onDirectoryContent(
									remoteDirPath, ftpFilesList);
					}
					break;
				}
				case WHAT_RETRIEVE_FILE: {
					String remoteDir = b.getString(EXTRA_REMOTE_DIR);
					String remoteFile = b.getString(EXTRA_REMOTE_FILE);
					String localDir = b.getString(EXTRA_LOCAL_DIR);
					String localFile = b.getString(EXTRA_LOCAL_FILE);
					boolean forceDownload = b.getBoolean(EXTRA_DOWNLOAD_FORCE);

					mFTPClient.getFile(
							adjustDir(localDir, LOCAL_BASE_PATH_DIR),
							localFile,
							adjustDir(remoteDir, REMOTE_BASE_PATH_DIR),
							remoteFile, forceDownload,
							(DownloadListener) listener);
					break;
				}
				case WHAT_RETRIEVE_MD5SUM: {
					String remoteDir = b.getString(EXTRA_REMOTE_DIR);
					String remoteFile = b.getString(EXTRA_REMOTE_FILE);
					mFTPClient.getRemoteMD5Sum(
							adjustDir(remoteDir, REMOTE_BASE_PATH_DIR),
							remoteFile + ".md5", (DownloadListener) listener);
					break;
				}
				case WHAT_READ_DIRECTORY: {
					String localDir = b.getString(EXTRA_LOCAL_DIR);
					File localDirFile = new File(adjustDir(localDir,
							LOCAL_BASE_PATH_DIR));
					File[] files = localDirFile.listFiles();
					if (files != null) {
						List<File> filesList = Arrays.asList(files);
						if (listener != null)
							((FileListener) listener).onDirectoryContent(
									localDir, filesList);
					}
					break;
				}
				case WHAT_DELETE_FILE: {
					String localDir = b.getString(EXTRA_LOCAL_DIR);
					String localFile = b.getString(EXTRA_LOCAL_FILE);
					File file = new File(adjustDir(localDir,
							LOCAL_BASE_PATH_DIR) + File.separator + localFile);
					file.delete();
					break;
				}
				case WHAT_CALC_MD5SUM: {
					String localDir = b.getString(EXTRA_LOCAL_DIR);
					String localFile = b.getString(EXTRA_LOCAL_FILE);
					String md5Sum = null;
					md5Sum = calcMD5Sum(
							adjustDir(localDir, LOCAL_BASE_PATH_DIR), localFile);
					if (listener != null)
						((DownloadListener) listener).onMD5Sum(localDir,
								localFile, md5Sum);
					break;
				}
				default:
					// nothing here
				}
			} catch (Exception e) {
				Log.e(TAG, "Problem while downloading/file access", e);
				
				Bundle bStatus = new Bundle();
				bStatus.putString(STATUS_ERROR_MSG, e.getMessage());
				if (mReceiver != null)
					mReceiver.send(STATUS_ERROR, bStatus);
			}

			removeTaskFromFront();
			if (listener != null)
				listener.onFinished();
			if (mReceiver != null)
				mReceiver.send(STATUS_FINISHED, Bundle.EMPTY);
		}

		public String calcMD5Sum(String localDir, String localFile)
				throws NoSuchAlgorithmException, IOException {
			MessageDigest md = MessageDigest.getInstance("MD5");
			InputStream is = new FileInputStream(localDir + File.separator
					+ localFile);
			DigestInputStream dis;
			dis = new DigestInputStream(is, md);
			byte[] buffer = new byte[16384];
			while (dis.read(buffer) != -1) {
				;
			}

			dis.close();
			byte mdBytes[] = md.digest();

			BigInteger bigInt = new BigInteger(1, mdBytes);
			String hexCalcString = bigInt.toString(16);

			return hexCalcString;
		}
	}
}
