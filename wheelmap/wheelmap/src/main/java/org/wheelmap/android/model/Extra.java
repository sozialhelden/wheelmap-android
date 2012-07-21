package org.wheelmap.android.model;

public interface Extra {
	public final static long ID_UNKNOWN = -1l;
	public final static String WM_ID_UNKNOWN = null;
	public final static int UNKNOWN = -1;

	public final static String POI_ID = "org.wheelmap.android.POI_ID";
	public final static String WM_ID = "org.wheelmap.android.WM_ID";

	public static final String CATEGORY = "org.wheelmap.android.CATEGORY";
	public final static String NODETYPE = "org.wheelmap.android.NODETYPE";
	public final static String LATITUDE = "org.wheelmap.android.LATITUDE";
	public final static String LONGITUDE = "org.wheelmap.android.LONGITUDE";
	public final static String WHEELCHAIR_STATE = "org.wheelmap.android.WHEELCHAIR_STATE";

	public final static String SELECTED_TAB = "org.wheelmap.android.SELECTED_TAB";
	public final static String IS_RECREATED = "org.wheelmap.android.IS_RECREATED";
	public final static String CENTER_MAP = "org.wheelmap.android.CENTER_MAP";
	public final static String FIRST_VISIBLE_POSITION = "org.wheelmap.android.FIRST_VISIBLE_POSITION";
	public final static String CREATE_WORKER_FRAGMENT = "org.wheelmap.android.CREATE_WORKER_FRAGMENT";

	public static final String ZOOM_MAP = "org.wheelmap.android.CENTER_ZOOM";
	public static final String EXPLICIT_RETRIEVAL = "org.wheelmap.android.RETRIEVAL";
	public static final String EXPLICIT_DIRECT_RETRIEVAL = "org.wheelmap.android.DIRECT_RETRIEVAL";

	public final static String SHOW_DISTANCE = "org.wheelmap.android.SHOW_DISTANCE";
	public final static String SHOW_MAP_HINT = "org.wheelmap.android.SHOW_MAP_HINT";

	public static final String EDITABLE_TEMPORARY_STORE = "org.wheelmap.android.EDITABLE_TEMPORARY_STORE";

	public final static String EXCEPTION = "org.wheelmap.android.EXCEPTION";
	public static final String STATUS_RECEIVER = "org.wheelmap.android.STATUS_RECEIVER";
	public static final String BOUNDING_BOX = "org.wheelmap.android.BOUNDING_BOX";
	public static final String LOCATION = "org.wheelmap.android.LOCATION";
	public static final String DISTANCE_LIMIT = "org.wheelmap.android.DISTANCE_LIMIT";
	public static final String LOCALE = "org.wheelmap.android.LOCALE";
	public static final String EMAIL = "org.wheelmap.android.EMAIL";
	public static final String PASSWORD = "org.wheelmap.android.PASSWORD";
	public static final String WHAT = "org.wheelmap.android.WHAT";

	public interface What {

		public static final int RETRIEVE_NODES = 0x1;
		public static final int RETRIEVE_NODE = 0x2;
		public static final int RETRIEVE_LOCALES = 0x3;
		public static final int RETRIEVE_CATEGORIES = 0x4;
		public static final int RETRIEVE_NODETYPES = 0x5;
		public static final int UPDATE_SERVER = 0x6;
		public static final int RETRIEVE_APIKEY = 0x7;
		public static final int SEARCH_NODES_IN_BOX = 0x8;
		public static final int SEARCH_NODES = 0x9;
		public static final int LOCATION_MANAGER_UPDATE = 0x11;

	}
}
