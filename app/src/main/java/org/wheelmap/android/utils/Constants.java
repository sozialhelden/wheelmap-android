package org.wheelmap.android.utils;

/**
 * Created by timfreiheit on 30.10.15.
 */
public class Constants {

    public static class Api {
        public static final String LOGIN = "/users/auth/osm";
        public static final String WM_REGISTER_LINK = "/en/oauth/register_osm";
        public static final String SCHEMA_WHEELMAP = "wheelmap";
    }

    public  static class TabContent {
        public static final int LOCATION_BASED_LIST = 0;
        public static final int MAP = 1;
        public static final int CATEGORY_LIST = 2;
    }

    public  static class Tracking {
        public  static class Screens {
            public static final String SPLASHSCREEN = "SplashScreen";
            public static final String HOMESCREEN = "HomeScreen";
            public static final String NEARBYSCREEN = "NearbyScreen";
            public static final String MAPSCREEN = "MapScreen";
            public static final String CATEGORYSCREEN = "CategoriesScreen";
            public static final String CONTRIBUTESCREEN = "ContributeScreen";
            public static final String OSMONBORDINGSCREEN = "OSMOnboardingScreen";
            public static final String OSMLOGOUTSCREEN = "OSMLogoutScreen";
            public static final String INFOSCREEN = "InfoScreen";
        }
    }
}
