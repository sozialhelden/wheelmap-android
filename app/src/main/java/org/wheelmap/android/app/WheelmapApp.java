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
package org.wheelmap.android.app;


import com.bugsense.trace.BugSenseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;


import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import org.holoeverywhere.HoloEverywhere;
import org.holoeverywhere.addon.AddonMyRoboguice;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Application;
import org.holoeverywhere.preference.SharedPreferences;
import org.wheelmap.android.manager.MyLocationManager;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.mapping.node.Node;
import org.wheelmap.android.mapping.node.Photo;
import org.wheelmap.android.mapping.node.Photos;
import org.wheelmap.android.model.Support;
import org.wheelmap.android.model.UserQueryHelper;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.modules.MainModule;
import org.wheelmap.android.online.R;
import org.wheelmap.android.push.GCMIntentReceiver;

import android.content.Context;
import android.text.TextUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import de.akquinet.android.androlog.Log;
import roboguice.RoboGuice;

public class WheelmapApp extends Application {

    private final static String TAG = WheelmapApp.class.getSimpleName();

    private static final long ACTIVE_FREQUENCY = 2 * 60 * 1000;

    private static final int ACTIVE_MAXIMUM_AGE = 10 * 60 * 1000;

    private static WheelmapApp INSTANCE;

    private SupportManager mSupportManager;

    private boolean isBugsenseInitCalled;

    public boolean isChangedText() {
        return changedText;
    }

    public void setChangedText(boolean changedText) {
        this.changedText = changedText;
    }

    private boolean changedText;

    private List listImages;

    BigInteger countPOIs;

    public static Context get() {
        return INSTANCE.getApplicationContext();
    }

    public static WheelmapApp getApp() {
        return INSTANCE;
    }

    public static SupportManager getSupportManager() {
        if (INSTANCE == null || INSTANCE.mSupportManager == null) {
            throw new NullPointerException(

                    "instance is null or mSupportManager is null - need to terminated");
        }

        return INSTANCE.mSupportManager;
    }

    static {
        HoloEverywhere.DEBUG = true;
    }

    @Override
    public void onCreate() {

        RoboGuice.setModulesResourceId(R.array.roboguice_modules);
        AddonMyRoboguice.addModule(MainModule.class);
        super.onCreate();
        INSTANCE = this;

       // setUpUrbanAirShip();

        Log.init(getApplicationContext(), getString(R.string.andrologproperties));
        Log.d(TAG, "onCreate: creating App");

        // LazyLoading images.
        // https://github.com/nostra13/Android-Universal-Image-Loader
        int memoryCacheSize = 1 * 1024 * 1024;
        int discCacheSize = 64 * 1024 * 1024;
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(false).cacheOnDisc(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext()).memoryCacheSize(memoryCacheSize)
                .discCacheSize(discCacheSize)
                .defaultDisplayImageOptions(options).build();
        ImageLoader.getInstance().init(config);


        if (!getResources().getBoolean(R.bool.developbuild) && !isBugsenseInitCalled) {
            BugSenseHandler.initAndStartSession(getApplicationContext(), getString(R.string.bugsense_key));
            isBugsenseInitCalled = true;
        }

        Support.init(getApplicationContext());
        Wheelmap.POIs.init(getApplicationContext());
        AppCapability.init(getApplicationContext());
        MyLocationManager.init(getApplicationContext());

        mSupportManager = new SupportManager(getApplicationContext());
        UserQueryHelper.init(getApplicationContext());
    }

    private void setUpUrbanAirShip(){
        if(true){
           return;
        }

        // Todo modify Manifest

        // Configure your application
        //
        // This can be done in code as illustrated here,
        // or you can add these settings to a properties file
        // called airshipconfig.properties
        // and place it in your "assets" folder
        AirshipConfigOptions options = AirshipConfigOptions.loadDefaultOptions(this);
        options.developmentAppKey = "app_key";
        options.developmentAppSecret = "app_secret";
        options.productionAppKey="app_key2";
        options.productionAppSecret="app_secred2";
        options.gcmSender="sender_id"; // PROJEKT ID in api console
        options.transport="gcm";
        options.inProduction = false;

        // Take off initializes the services
        UAirship.takeOff(this, options);
        PushManager.init();
        PushManager.enablePush();
        PushManager.shared().setIntentReceiver(GCMIntentReceiver.class);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "onTerminate");
        MyLocationManager.destroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d("lowmemory", "wheelmap app - onLowMemory");
    }

    public boolean isBugsenseInitCalled() {
        return isBugsenseInitCalled;
    }


    public static SharedPreferences getCategoryChoosedPrefs(){
        SharedPreferences prefs = INSTANCE.getSharedPreferences("wheelmap_category",0);
        return prefs;
    }

    public static SharedPreferences getDefaultPrefs(){
        return INSTANCE.getDefaultSharedPreferences();
    }

    public BigInteger getCountPOIs(){
        return this.countPOIs;
    }
    public void setCountPOIs(BigInteger countPOIs){
        this.countPOIs = countPOIs;
    }

    List<Photo> photos;

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(Photos photos) {
        this.photos = photos.getPhotos();
        listImages = new ArrayList();

        for(Photo p : this.photos){

            // always loads only the "original" photo
            String newurl = p.getImages().get(0).getUrl();
            String[] sList = newurl.split("\\?");
            String url = sList[0];

            this.listImages.add(url);

            Log.d("load photo with url");

        }


    }

    public boolean isNoItemToSelect() {
        return noItemToSelect;
    }

    public void setNoItemToSelect(boolean noItemToSelect) {
        this.noItemToSelect = noItemToSelect;
    }

    boolean noItemToSelect = false;

    public List getListImages() {
        return listImages;
    }

    public void setListImages(List listImages) {
        this.listImages = listImages;
    }

    String uriString;

    public double getGeoLon() {
        return geoLon;
    }

    public void setGeoLon(double geoLon) {
        this.geoLon = geoLon;
    }

    double geoLon;

    public double getGeoLat() {
        return geoLat;
    }

    public void setGeoLat(double geoLat) {
        this.geoLat = geoLat;
    }

    double geoLat;
    String addressString;


    public String getAddressString() {
        return addressString;
    }

    public void setAddressString(String adressString) {
        this.addressString = adressString;
    }



    public String getUriString(){ return uriString; }
    public void setUriString(String uriString){this.uriString = uriString; }


    Node node;

    public Node getNode(){ return node; }
    public void setNode(Node node){this.node = node; }

    boolean saved = false;

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }



    public static void checkForCrashes(android.app.Activity context) {
        String appId = context.getString(R.string.hockeyapp_id);
        if (TextUtils.isEmpty(appId)) {
            return;
        }
        CrashManager.register(context, appId);
    }

    public static void checkForUpdates(android.app.Activity context) {
        String appId = context.getString(R.string.hockeyapp_id);
        if (TextUtils.isEmpty(appId) || "org.wheelmap.android.online".equals(context.getPackageName())) {
            return;
        }
        UpdateManager.register(context, appId);
    }

    boolean isSearching;

    public boolean isSearching() {
        return isSearching;
    }

    public void setSearching(boolean isSearching) {
        this.isSearching = isSearching;
    }

    boolean searchSuccessfully = true;

    public boolean isSearchSuccessfully() {
        return searchSuccessfully;
    }

    public void setSearchSuccessfully(boolean searchSuccessfully) {
        this.searchSuccessfully = searchSuccessfully;
    }


}
