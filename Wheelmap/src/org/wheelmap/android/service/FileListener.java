package org.wheelmap.android.service;

import java.io.File;
import java.util.List;

public interface FileListener extends BaseListener {
	public void onDirectoryContent( String parentDir, List<File> files );
	public void onMD5Sum( String parentDir, String file, String md5sum );
}
