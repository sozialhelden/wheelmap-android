/*
 * #%L
 * Wheelmap - App
 * %%
 * Copyright (C) 2011 - 2012 Michal Harakal - Michael Kroez - Sozialhelden e.V.
 * %%
 * Wheelmap App based on the Wheelmap Service by Sozialhelden e.V.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wheelmap.android.model;

public interface Extra {

    public final static long ID_UNKNOWN = -1l;
    public final static String WM_ID_UNKNOWN = null;
    public final static int UNKNOWN = -1;

    public final static String POI_ID = "POI_ID";
    public final static String WM_ID = "WM_ID";

    public final static String CATEGORY = "CATEGORY";
    public final static String NODETYPE = "NODETYPE";
    public final static String LATITUDE = "LATITUDE";
    public final static String LONGITUDE = "LONGITUDE";
    public final static String ZOOM_LEVEL = "ZOOM_LEVEL";
    public final static String WHEELCHAIR_STATE = "WHEELCHAIR_STATE";

    public final static String SELECTED_TAB = "SELECTED_TAB";
    public final static String CENTER_MAP = "CENTER_MAP";
    public final static String FIRST_VISIBLE_POSITION = "FIRST_VISIBLE_POSITION";
    public final static String CREATE_WORKER_FRAGMENT = "CREATE_WORKER_FRAGMENT";
    public final static String DISABLE_SEARCH = "DISABLE_SEARCH";
    public final static String SHOW_POI = "SHOW_POI";

    public final static String ZOOM_MAP = "CENTER_ZOOM";
    public final static String REQUEST = "REQUEST";
    public final static String EXPLICIT_DIRECT_RETRIEVAL = "DIRECT_RETRIEVAL";
    public final static String SHOW_MAP = "SHOW_MAP";
    public final static String MAP_HEIGHT_FULL = "MAP_HEIGHT_FULL";

    public final static String SHOW_DISTANCE = "SHOW_DISTANCE";
    public final static String SHOW_MAP_HINT = "SHOW_MAP_HINT";
    public final static String ENABLE_BOUNDING_BOX = "ENABLE_BOUNDING_BOX";

    public final static String TEMPORARY_STORED = "TEMPORARY_STORED";

    public final static String EXCEPTION = "EXCEPTION";
    public final static String STATUS_RECEIVER = "STATUS_RECEIVER";
    public final static String BOUNDING_BOX = "BOUNDING_BOX";
    public final static String LOCATION = "LOCATION";
    public final static String DISTANCE_LIMIT = "DISTANCE_LIMIT";
    public final static String LOCALE = "LOCALE";
    public final static String EMAIL = "EMAIL";
    public final static String PASSWORD = "PASSWORD";
    public final static String WHAT = "WHAT";
    public final static String ALERT_TITLE = "ALERT_TITLE";
    public final static String ALERT_MESSAGE = "ALERT_MESSAGE";
    public final static String ID = "ID";
    public final static String MOVABLE_VISIBLE = "MOVABLE_VISIBLE";

    public final static String IS_RESTARTED = "IS_RESTARTED";

    public interface What {

        public final static int RETRIEVE_NODES = 0x1;
        public final static int RETRIEVE_NODE = 0x2;
        public final static int RETRIEVE_LOCALES = 0x3;
        public final static int RETRIEVE_CATEGORIES = 0x4;
        public final static int RETRIEVE_NODETYPES = 0x5;
        public final static int UPDATE_SERVER = 0x6;
        public final static int RETRIEVE_APIKEY = 0x7;
        public final static int SEARCH_NODES_IN_BOX = 0x8;
        public final static int SEARCH_NODES = 0x9;
        public final static int LOCATION_MANAGER_UPDATE = 0x11;

    }
}
