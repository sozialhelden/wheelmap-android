package org.wheelmap.android.service;

import java.util.List;

import org.apache.commons.net.ftp.FTPFile;

public interface DownloadListener extends BaseListener {
	public void onDirectoryContent( String parentDir, List<FTPFile> files );
	public void onProgress( int percentageProgress );
	public int getProgress();
	public void onMD5Sum( String parentDir, String file, String md5sum );
}
