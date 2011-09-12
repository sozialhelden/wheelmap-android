package org.wheelmap.android.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;
import org.wheelmap.android.net.MapsforgeFTP;
import org.wheelmap.android.utils.MultiResultReceiver;

import android.os.Bundle;
import android.os.Environment;
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
	private static final int WHAT_READ_DIRECTORY = 0x3;
	private static final int WHAT_DELETE_FILE = 0x4;

	public static final int STATUS_RUNNING = 0x1;
	public static final int STATUS_ERROR = 0x2;
	public static final int STATUS_FINISHED = 0x3;
	public static final String STATUS_ERROR_MSG = "org.wheelmap.android.net.MapFileService.STATUS_ERROR_MSG";

	private MultiResultReceiver mReceiver;
	private MyHandler mHandler;
	private HandlerThread mHandlerThread;

	private static final String REMOTE_BASE_PATH_DIR = File.separator + "maps";
	public static String LOCAL_BASE_PATH_DIR;

	public static class Task {
		public static final int TYPE_UNKNOWN = 0x0;
		public static final int TYPE_RETRIEVE_DIR = 0x1;
		public static final int TYPE_RETRIEVE_FILE = 0x2;
		public static final int TYPE_DELETE_FILE = 0x3;
		public static final int TYPE_READ_DIR = 0x4;

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

	public static class FTPFileWithParent {
		public String parentDir;
		public FTPFile file;

		public FTPFileWithParent(String parentDir, FTPFile file) {
			this.parentDir = parentDir;
			this.file = file;
		}
	}

	public static class FileWithParent {
		public String parentDir;
		public File file;

		public FileWithParent(String parentDir, File file) {
			this.parentDir = parentDir;
			this.file = file;
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
			Looper looper = mHandlerThread.getLooper();
			mHandler = new MyHandler(looper);
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
			RetrieveDirectoryListener listener) {
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
			RetrieveFileListener listener) {
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

	public void getLocalMapsDirectory(String localDir,
			ReadDirectoryListener listener) {
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
			BaseListener listener) {
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
					List<FTPFileWithParent> files = new ArrayList<FTPFileWithParent>();
					retrieveDirectoryContent(remoteDirPath, files);
					if (listener != null)
						((RetrieveDirectoryListener) listener)
								.onDirectoryContent(files);

					break;
				}
				case WHAT_RETRIEVE_FILE: {
					String remoteDir = b.getString(EXTRA_REMOTE_DIR);
					String remoteFile = b.getString(EXTRA_REMOTE_FILE);
					String localDir = b.getString(EXTRA_LOCAL_DIR);
					String localFile = b.getString(EXTRA_LOCAL_FILE);
					boolean forceDownload = b.getBoolean(EXTRA_DOWNLOAD_FORCE);
					retrieveFile(remoteDir, remoteFile, localDir, localFile,
							forceDownload, listener);
					break;
				}
				case WHAT_READ_DIRECTORY: {
					String localDir = b.getString(EXTRA_LOCAL_DIR);
					List<FileWithParent> files = new ArrayList<FileWithParent>();
					readDirectoryContent(localDir, files);
					if (listener != null)
						((ReadDirectoryListener) listener)
								.onDirectoryContent(files);

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

		public void retrieveDirectoryContent(String parentDir,
				List<FTPFileWithParent> files) throws IOException {
			FTPFile[] ftpFiles = null;
			ftpFiles = mFTPClient.getDir(adjustDir(parentDir,
					REMOTE_BASE_PATH_DIR));
			for (FTPFile file : ftpFiles) {
				files.add(new FTPFileWithParent(parentDir, file));
				if (file.isDirectory()) {
					retrieveDirectoryContent(
							parentDir + File.separator + file.getName(), files);
				}
			}
		}

		private void retrieveFile(String remoteDir, String remoteFile,
				String localDir, String localFile, boolean forceDownload,
				BaseListener listener) throws IOException,
				NoSuchAlgorithmException {
			int result = mFTPClient.getFile(
					adjustDir(localDir, LOCAL_BASE_PATH_DIR), localFile,
					adjustDir(remoteDir, REMOTE_BASE_PATH_DIR), remoteFile,
					forceDownload, (RetrieveFileListener) listener);
			if (result == MapsforgeFTP.RESULT_INTERRUPTED)
				return;

			String md5sumRemote = mFTPClient.getRemoteMD5Sum(
					adjustDir(remoteDir, REMOTE_BASE_PATH_DIR), remoteFile
							+ ".md5");
			String md5sumLocal = calcMD5Sum(
					adjustDir(localDir, LOCAL_BASE_PATH_DIR), localFile);
			Log.d(TAG, "md5sumRemote = " + md5sumRemote);
			Log.d(TAG, "md5sumLocal = " + md5sumLocal);

			if (!md5sumRemote.equals(md5sumLocal)) {
				File file = new File(adjustDir(localDir, LOCAL_BASE_PATH_DIR)
						+ File.separator + localFile);
				file.delete();
				throw new IOException(
						"Transmitted file is not correct. MD5Sum error. Deleted.");
			}
		}

		private void readDirectoryContent(String localDir,
				List<FileWithParent> fileList) {

			File localDirFile = new File(adjustDir(localDir,
					LOCAL_BASE_PATH_DIR));
			if (!localDirFile.exists())
				return;

			File[] files = localDirFile.listFiles();
			for (File file : files) {
				fileList.add(new FileWithParent(localDir, file));
				if (file.isDirectory())
					readDirectoryContent(
							localDir + File.separator + file.getName(),
							fileList);
			}
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

			StringBuilder missingZeros = new StringBuilder();
			int missingChars = 32 - hexCalcString.length();
			int i;
			for (i = 0; i < missingChars; i++) {
				missingZeros.append("0");
			}

			hexCalcString = missingZeros.toString() + hexCalcString;
			return hexCalcString;
		}
	}

	public interface BaseListener {
		public void setListener(BaseListener listener);

		public void onRunning();

		public void onFinished();
	}

	public interface RetrieveDirectoryListener extends BaseListener {
		public void onDirectoryContent(List<FTPFileWithParent> files);
	}

	public interface ReadDirectoryListener extends BaseListener {
		public void onDirectoryContent(List<FileWithParent> files);
	}

	public interface RetrieveFileListener extends BaseListener {
		public void onProgress(int percentageProgress);

		public int getProgress();
	}

	static {
		LOCAL_BASE_PATH_DIR = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separator
				+ "wheelmap"
				+ File.separator + "maps";
	}

}
