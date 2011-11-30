/*
Copyright (C) 2011 Michal Harakal and Michael Kroez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS-IS" BASIS
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
        
*/

package org.wheelmap.android.model;

import android.net.Uri;
import android.provider.BaseColumns;

public class Wheelmap {

		public static final String TAG ="org.wheelmap.android";
		public static final String AUTHORITY = "org.wheelmap.android";
		
		// This class cannot be instantiated
		private Wheelmap() {} 
		
		public static final int UPDATE_NO = 0x0;
		public static final int UPDATE_WHEELCHAIR_STATE = 0x1;
		public static final int UPDATE_ALL_FIELDS = 0x2;
		public static final int UPDATE_PENDING_STATE_ONLY = 0x3;
		public static final int UPDATE_PENDING_FIELDS_ALL = 0x4;

		
		/**
		 * Columns from the Places table that other columns join into themselves.
		 */
		public static interface POIsColumns {

			/**
			 * The name of the place
			 * <P>Type: TEXT</P>
			 */
			public static final String WM_ID = "wm_id";
			
			public static final String NAME = "name";
						
			public static final String COORD_LAT = "lat";
			
			public static final String COORD_LON = "lon";
			
			public static final String STREET = "street";
			
			public static final String HOUSE_NUM = "house_num";
			
			public static final String POSTCODE = "postcode";
			
			public static final String CITY = "city";
			
			public static final String PHONE = "phone";
			
			public static final String WEBSITE = "website";
			
			public static final String WHEELCHAIR = "wheelchair";
			
			public static final String WHEELCHAIR_DESC = "wheelchair_desc";
			
			public static final String CATEGORY_ID = "category_id";
			
			public static final String CATEGORY_IDENTIFIER = "category_identifier";
			
			public static final String NODETYPE_ID = "nodetype_id";
			
			public static final String NODETYPE_IDENTIFIER = "nodetype_identifier";
			
			// auxiliry pre calculated sin and cos values of lat/lon (in radians)
			public static final String SIN_LAT_RAD = "sin_lat_rad";
			public static final String COS_LAT_RAD = "cos_lat_rad";
			public static final String SIN_LON_RAD = "sin_lon_rad";
			public static final String COS_LON_RAD = "cos_lon_rad";
			
			public static final String UPDATE_TAG = "update_tag";
			public static final String UPDATE_TIMESTAMP = "update_timestamp";
			
		}

		public static final class POIs implements BaseColumns, POIsColumns {
			// This class cannot be instantiated
			private POIs() {}

			/**
			 * The content:// style URL for this table
			 */
			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/pois");
			public static final Uri CONTENT_URI_POI_ID = Uri.parse( "content://" + AUTHORITY + "/poi_id" );
			public static final Uri CONTENT_URI_POI_SORTED = Uri.parse("content://" + AUTHORITY + "/poissorted" );

			/**
			 * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
			 */
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.wheelmap.pois";
			public static final String CONTENT_TYPE_SORTED = "vnd.android.cursor.dir/vnd.wheelmap.poissorted";
			/**
			 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
			 */
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.wheelmap.poi_id";
   
	        /**
			 * The default sort order for this table - categories
			 */
			public static final String DEFAULT_SORT_ORDER = NAME + " DESC";
			
			/**
			 * The default sort order for this table - categories
			 */
			public static final String EXTRAS_POI_ID = NAME + "_ID";
			

			/**
			 * The columns we are interested in from the database
			 */
			public static final String[] PROJECTION = new String[] {
				_ID,
				WM_ID,
				NAME,
				COORD_LAT,
				COORD_LON,
				STREET,
				HOUSE_NUM,
				POSTCODE,
				CITY,
				PHONE,
				WEBSITE,
				WHEELCHAIR,
				WHEELCHAIR_DESC,
				CATEGORY_ID,
				CATEGORY_IDENTIFIER,
				NODETYPE_ID,
				NODETYPE_IDENTIFIER,
				UPDATE_TAG,
				UPDATE_TIMESTAMP
			};
		}
}
