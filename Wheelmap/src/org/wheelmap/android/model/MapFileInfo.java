package org.wheelmap.android.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class MapFileInfo {

		public static final String TAG ="org.wheelmap.android";
		public static final String AUTHORITY = "org.wheelmap.android.mapfile";
		private static final String ISO_8601_DATEFORMAT = "yyyy-MM-dd HH:mm:ss";

		private static final Pattern VERSION_PATTERN = Pattern.compile( ".*-(\\d+\\.\\d+\\.?\\d*)\\..*" );
		
		public static final int FILE_NOT_LOCAL = 0;
		public static final int FILE_COMPLETE = 1;
		public static final int FILE_INCOMPLETE = 2;
		
		public static final int ENTRY_NOT_UPDATED = 0;
		public static final int ENTRY_UPDATED = 1;
		
		// This class cannot be instantiated
		private MapFileInfo() {} 

		/**
		 * Columns for DownloadInfo
		 */
		public static interface MapFileInfoColumns {
			
			public static final String SCREEN_NAME = "screen_name";
			
			public static final String NAME = "name";
			
			public static final String PARENT_NAME = "parent_name";
			
			public static final String TYPE = "type";
			
			public static final String REMOTE_NAME = "remote_name";
			
			public static final String REMOTE_PARENT_NAME = "remote_parent_name";
			
			public static final String REMOTE_TIMESTAMP = "remote_timestamp";
			
			public static final String REMOTE_MD5_SUM = "remote_md5_sum";
			
			public static final String REMOTE_SIZE = "remote_size";
			
			public static final String VERSION = "version";
			
			public static final String LOCAL_TIMESTAMP = "local_timestamp";
			
			public static final String LOCAL_AVAILABLE = "local_available";
			
			public static final String UPDATE_TAG = "update_tag";
			
		}
		
		public static String getScreenName( Cursor c ) {
			return c.getString(c.getColumnIndexOrThrow( MapFileInfoColumns.SCREEN_NAME ));
		}
		
		public static String getName( Cursor c ) {
			return c.getString(c.getColumnIndexOrThrow( MapFileInfoColumns.NAME ));
		}
		
		public static String getParentName( Cursor c ) {
			return c.getString(c.getColumnIndexOrThrow( MapFileInfoColumns.PARENT_NAME ));
		}
		
		public static int getType( Cursor c ) {
			return c.getInt( c.getColumnIndexOrThrow( MapFileInfoColumns.TYPE ));
		}
		
		public static String getRemoteName( Cursor c ) {
			return c.getString(c.getColumnIndexOrThrow( MapFileInfoColumns.REMOTE_NAME ));
		}
		
		public static String getRemoteParentName( Cursor c ) {
			return c.getString(c.getColumnIndexOrThrow( MapFileInfoColumns.REMOTE_PARENT_NAME ));
		}
		
		public static String getRemoteTimestamp( Cursor c ) {
			return c.getString(c.getColumnIndexOrThrow( MapFileInfoColumns.REMOTE_TIMESTAMP ));
		}
		
		public static long getRemoteSize( Cursor c ) {
			return c.getLong(c.getColumnIndexOrThrow( MapFileInfoColumns.REMOTE_SIZE ));
		}
		
		public static String getRemoteMD5Sum( Cursor c ) {
			return c.getString(c.getColumnIndexOrThrow( MapFileInfoColumns.REMOTE_MD5_SUM ));
		}
		
		public static String getVersion( Cursor c ) {
			return c.getString(c.getColumnIndexOrThrow( MapFileInfoColumns.VERSION ));
		}
		
		public static String getLocalTimestamp( Cursor c ) {
			return c.getString(c.getColumnIndexOrThrow( MapFileInfoColumns.LOCAL_TIMESTAMP ));
		}
		
		public static int getLocalAvailable( Cursor c ) {
			return c.getInt(c.getColumnIndexOrThrow( MapFileInfoColumns.LOCAL_AVAILABLE ));
		}
		
		public static String formatDate(Date date) {
			SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_DATEFORMAT);
			return sdf.format(date);
		}

		public static Date parseDate(String iso8601_date) throws ParseException {
			SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_DATEFORMAT);
			return sdf.parse(iso8601_date);
		}

		public static String extractVersion( String fileName ) {
			Matcher m = VERSION_PATTERN.matcher( fileName );
			if ( m.matches()) {
				return m.group( 1 );
			} else
				return "";
		}
		
		public static String extractScreenName( String fileName ) {
			int begin = 0;
			int end = fileName.lastIndexOf( '-');
			if ( end > -1 )
				return fileName.substring( begin, end ).replace( "-", " ").replace( "_", " ");
			else
				return fileName;
		}
		

		public static final class MapFileInfos implements BaseColumns, MapFileInfoColumns {
			// This class cannot be instantiated
			private MapFileInfos() {}
			
			/**
			 * The content:// style URL for this table
			 */
			public static final Uri CONTENT_URI_DIRS = Uri.parse("content://" + AUTHORITY + "/dirs" );
			public static final Uri CONTENT_URI_FILES = Uri.parse("content://" + AUTHORITY + "/files" );
			public static final Uri CONTENT_URI_DIRSNFILES = Uri.parse( "content://" + AUTHORITY + "/dirsnfiles" );

	        /**
			 * The default sort order for this table - categories
			 */
			public static final String DEFAULT_SORT_ORDER = SCREEN_NAME + " ASC";
			
			public static final String DIR_TYPE = "vnd.android.cursor.item/vnd.wheelmap.mapsforge_dirs";
			public static final String FILE_TYPE = "vnd.android.cursor.item/vnd.wheelmap.mapsforge_files";
			public static final String DIRNFILE_TYPE = "vnd.android.cursor.item/vnd.wheelmap.mapsforge_dirsnfiles";

			
			/**
			 * The columns we are interested in from the database
			 */
			public static final String[] filePROJECTION = new String[] {
				_ID,
				SCREEN_NAME,
				NAME,
				PARENT_NAME,
				TYPE,
				REMOTE_NAME,
				REMOTE_PARENT_NAME,
				REMOTE_TIMESTAMP,
				REMOTE_SIZE,
				REMOTE_MD5_SUM,
				VERSION,
				LOCAL_TIMESTAMP,
				LOCAL_AVAILABLE,
				UPDATE_TAG
				
			};
			
			public static final String[] dirPROJECTION = new String[] {
				_ID,
				SCREEN_NAME,
				NAME,
				PARENT_NAME,
				TYPE,
				REMOTE_NAME,
				REMOTE_PARENT_NAME,
				REMOTE_TIMESTAMP,
				UPDATE_TAG
			};
			
		}
}
