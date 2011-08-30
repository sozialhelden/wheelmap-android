package org.wheelmap.android.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.wheelmap.android.service.MapFileService.RetrieveFileListener;

import android.util.Log;

public class MapsforgeFTP {
	private final static String TAG = "mapsforgeftp";

	private final static String HOST = "ftp.mapsforge.org";
	private final static String USERNAME = "anonymous";
	private final static String PASSWORD = "android-app@wheelmap.org";
	
	public final static int RESULT_OK = 0;
	public final static int RESULT_INTERRUPTED = 1;

	private FTPClient mFTPClient;
	private AtomicBoolean mInterrupted = new AtomicBoolean();

	public MapsforgeFTP() {
		mFTPClient = new FTPClient();
		FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
		mFTPClient.configure(conf);

		mInterrupted.set( false );
	}

	public void interrupt() {
		mInterrupted.set( true );
	}

	public void connect() throws SocketException, IOException {
		mFTPClient.connect(HOST);
		mFTPClient.login(USERNAME, PASSWORD);
		int reply = mFTPClient.getReplyCode();

		if (!FTPReply.isPositiveCompletion(reply)) {
			mFTPClient.disconnect();
		}

		mFTPClient.enterLocalPassiveMode();
		mFTPClient.setFileTransferMode(FTP.COMPRESSED_TRANSFER_MODE);
		mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
	}

	public void diconnect() throws IOException {
		mFTPClient.logout();
		mFTPClient.disconnect();
	}

	public FTPFile[] getDir(String dir) throws IOException {
		FTPFile[] files = mFTPClient.listFiles(dir);
		return files;
	}

	public int getFile(String localDir, String localFilename,
			String remoteDir, String remoteFilename, boolean force,
			RetrieveFileListener listener) throws IOException {
		mInterrupted.set( false );
		
		File dir = new File(localDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File localFile = new File(localDir, localFilename);
		Log.d(TAG, "getFile: localFile = " + localFile.getAbsolutePath());

		FTPFile remoteFile = null;
		FTPFile[] remoteFileInfo = mFTPClient.listFiles(remoteDir
				+ File.separator + remoteFilename);
		remoteFile = remoteFileInfo[0];

		Calendar localFileMod = new GregorianCalendar();
		localFileMod.setTimeInMillis(localFile.lastModified());
		long restartAtOffset;

		if (!localFile.exists() || force) {
			localFile.createNewFile();
			restartAtOffset = -1;
		} else {
			if (localFileMod.after(remoteFile.getTimestamp())
					&& localFile.length() < remoteFile.getSize()) {
				restartAtOffset = localFile.length();
			} else if (!localFileMod.after(remoteFile.getTimestamp())) {
				restartAtOffset = -1;
			} else {
				restartAtOffset = localFile.length();
			}
		}

		FileOutputStream out;
		if (restartAtOffset != -1) {
			mFTPClient.setRestartOffset(restartAtOffset);
			out = new FileOutputStream(localFile, true);
		} else {
			out = new FileOutputStream(localFile);

		}

		Log.d(TAG, "MapsforgeFTP: mFTPClient: retrieveFileStream");
		InputStream in;
		in = mFTPClient.retrieveFileStream(remoteDir + File.separator
				+ remoteFilename);

		int bufSize = 512 * 1024;
		byte[] buffer = new byte[bufSize];
		int byteCount = 0;
		long byteCountProgress = restartAtOffset;

		int progressPercent;
		while (((byteCount = in.read(buffer)) != -1) && !mInterrupted.get()) {
			byteCountProgress += byteCount;
			out.write(buffer, 0, byteCount);
			progressPercent = (int) (byteCountProgress * 100 / (remoteFile
					.getSize()));
			if (listener != null)
				listener.onProgress(progressPercent);
		}

		in.close();
		mFTPClient.completePendingCommand();
		out.close();
		
		if ( mInterrupted.get())
			return RESULT_INTERRUPTED;
		else
			return RESULT_OK;
	}

	public String getRemoteMD5Sum(String remoteDir, String remoteMD5File ) throws IOException {
		Log.d(TAG, "getRemoteMD5Sum: remoteDir = " + remoteDir
				+ " remoteMD5File = " + remoteMD5File);
		InputStream in;
		in = mFTPClient.retrieveFileStream(remoteDir + File.separator
				+ remoteMD5File);
		BufferedReader buf = new BufferedReader(new InputStreamReader(in));
		String line = buf.readLine();
		buf.close();
		mFTPClient.completePendingCommand();

		Log.d(TAG, "getRemoteMD5Sum: line = " + line);
		String content[] = line.split("  ");

		Log.d(TAG, "getRemoteMD5Sum: remoteDir = " + remoteDir + " file = "
				+ content[1] + " md5sum = " + content[0]);
		if (content[0] == null || content[1] == null)
			return null;
		
		return content[0];

	}

	public boolean isConnected() {
		return mFTPClient.isConnected() && mFTPClient.isAvailable();
	}
}