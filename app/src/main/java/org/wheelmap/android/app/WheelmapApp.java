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

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.text.TextUtils;
import com.facebook.stetho.Stetho;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;
import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;
import org.wheelmap.android.analytics.AnalyticsTrackingManager;
import org.wheelmap.android.manager.MyLocationManager;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.mapping.node.Node;
import org.wheelmap.android.mapping.node.Photo;
import org.wheelmap.android.mapping.node.Photos;
import org.wheelmap.android.model.Support;
import org.wheelmap.android.model.UserQueryHelper;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.online.BuildConfig;
import org.wheelmap.android.online.R;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import de.akquinet.android.androlog.Constants;
import de.akquinet.android.androlog.Log;
import hotchemi.android.rate.AppRate;
import roboguice.RoboGuice;

public class WheelmapApp extends Application {

    private final static String TAG = WheelmapApp.class.getSimpleName();

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

    @Override
    public void onCreate() {

        RoboGuice.setModulesResourceId(R.array.roboguice_modules);
        super.onCreate();
        INSTANCE = this;

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }

        Log.init(getApplicationContext(), getString(R.string.andrologproperties));
        if (BuildConfig.DEBUG) {
            Log.setDefaultLogLevel(Constants.DEBUG);
        }
        Log.d(TAG, "onCreate: creating App");


        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .resetViewBeforeLoading(true)
                .build();

        File cacheDir = StorageUtils.getOwnCacheDirectory(getApplicationContext(), getString(R.string.app_name));
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(options)
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .memoryCache(new WeakMemoryCache())
                .build();

        ImageLoader.getInstance().init(config);


        if (!BuildConfig.DEBUG && !isBugsenseInitCalled) {
            isBugsenseInitCalled = true;
        }

        Support.init();
        Wheelmap.POIs.init(getApplicationContext());
        AppCapability.init(getApplicationContext());
        MyLocationManager.init(getApplicationContext());

        mSupportManager = new SupportManager(getApplicationContext());
        UserQueryHelper.init(getApplicationContext());

        initAppRateDialog();

        AnalyticsTrackingManager.init(this);
    }

    private void initAppRateDialog() {
        AppRate.with(this)
            .setInstallDays(4)
            .setLaunchTimes(3)
            .setRemindInterval(2)
            .setShowLaterButton(true)
            .setDebug(BuildConfig.DEBUG && BuildConfig.BUILD_TYPE.equals("debug"))
            .monitor();
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

    private SharedPreferences getDefaultSharedPreferences(){
        return getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
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
        String appId = BuildConfig.HOCKEY_APP_ID;
        if (TextUtils.isEmpty(appId)) {
            return;
        }
        CrashManager.register(context, appId);
    }

    public static void checkForUpdates(android.app.Activity context) {
        String appId = BuildConfig.HOCKEY_APP_ID;
        if (TextUtils.isEmpty(appId) || BuildConfig.DEBUG) {
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
