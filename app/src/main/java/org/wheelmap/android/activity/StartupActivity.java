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
package org.wheelmap.android.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.WindowManager;
<<<<<<< HEAD

import org.wheelmap.android.activity.base.BaseActivity;
=======
import org.wheelmap.android.analytics.AnalyticsTrackingManager;
>>>>>>> 1d57954aef8d4309f867c6ce784e3fa52dedb76f
import org.wheelmap.android.app.AppCapability;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Extra.What;
import org.wheelmap.android.modules.AppProperties;
import org.wheelmap.android.modules.IAppProperties;
import org.wheelmap.android.online.BuildConfig;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.RestService;
import org.wheelmap.android.service.RestServiceException;
import org.wheelmap.android.tango.TangoMeasureActivity;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.UtilsMisc;

import java.util.List;

import de.akquinet.android.androlog.Log;
import roboguice.inject.ContentViewListener;

public class StartupActivity extends BaseActivity implements
        DetachableResultReceiver.Receiver {

    public static boolean LOAD_AGAIN_DEBUG = false;
    public static String FIRST_START = "FIRST_START";

    private final static String TAG = StartupActivity.class.getSimpleName();

    ContentViewListener ignored;

    public IAppProperties appProperties;

    private State mState;

    private SupportManager mSupportManager;

    private boolean mIsInForeground;

    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appProperties = AppProperties.getInstance(getApplication());

        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_splashscreen);

        mState = new State();
        mState.mReceiver.setReceiver(this);

        checkForHockeyUpdates();

        Log.d(TAG, "Server: " + BuildConfig.API_BASE_URL);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsInForeground = true;
        AnalyticsTrackingManager.trackScreen(AnalyticsTrackingManager.TrackableScreensName.SPLASHSCREEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsInForeground = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSupportManager != null) {
            mSupportManager.releaseReceiver();
        }
    }

    private void checkForHockeyUpdates() {
        String hockeyURI = appProperties.get(IAppProperties.KEY_HOCKEY_URI);
        Log.d(TAG, "hockeyURI = *" + hockeyURI + "*");
        if (true || TextUtils.isEmpty(hockeyURI)) {
            onHockeyDone();
            return;
        }

    }

    public void onHockeyDone() {
        if (AppCapability.isNotWorking()) {
            showDialogNotWorking();
            return;
        }

        if (startupPersistentStuff()) {
            return;
        }

        if (needStartApp()) {
            startupAppDelayed();
        } else {
            startupApp();
            finish();
        }
    }

    private boolean startupPersistentStuff() {
        mSupportManager = WheelmapApp.getSupportManager();
        if (LOAD_AGAIN_DEBUG || mSupportManager.needsReloading()) {
            startTime = System.currentTimeMillis();
            mSupportManager.reload(mState.mReceiver);
            return true;
        }

        return false;
    }

    private boolean needStartApp() {
        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningTaskInfo> tasksInfo = am.getRunningTasks(1024);

        if (!tasksInfo.isEmpty()) {
            final String ourAppPackageName = getPackageName();
            RunningTaskInfo taskInfo;
            final int size = tasksInfo.size();
            for (int i = 0; i < size; i++) {
                taskInfo = tasksInfo.get(i);
                if (ourAppPackageName.equals(taskInfo.baseActivity
                        .getPackageName())) {
                    // continue application start only if there is the only
                    // Activity in the task
                    // (BTW in this case this is the StartupActivity)
                    return taskInfo.numActivities == 1;
                }
            }
        }

        return true;
    }

    private void startupAppDelayed() {
        long time_dif = System.currentTimeMillis()-startTime;
        time_dif = Math.abs(time_dif);
        if(time_dif < 1000){
            Handler h = new Handler();
            h.postDelayed(new Runnable() {

                @Override
                public void run() {
                    startupApp();
                }

            }, 1000-time_dif);
        } else{
            startupApp();
        }
    }

    private void startupApp() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        Intent intent;

        if (UtilsMisc.isTablet(getApplicationContext())) {
            intent = new Intent(getApplicationContext(),
                    MainMultiPaneActivity.class);
        } else {
            intent = new Intent(getApplicationContext(),
                    DashboardActivity.class);
        }

        intent = TangoMeasureActivity.newIntent(this);

        intent.putExtra(Extra.REQUEST, true);
        startActivity(intent);
        SharedPreferences defaultPreferences = WheelmapApp.getDefaultPrefs();
        if(defaultPreferences.getBoolean(FIRST_START, true)){
            startIntroductionActivity();
            defaultPreferences.edit().putBoolean(FIRST_START, false).commit();
        }
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void startIntroductionActivity(){
        Intent intent = new Intent(this, IntroductionActivity.class);
        startActivity(intent);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

        if (resultCode == RestService.STATUS_FINISHED) {
            int what = resultData.getInt(Extra.WHAT);
            switch (what) {
                case What.RETRIEVE_LOCALES:
                    mSupportManager.reloadStageTwo();
                    break;
                case What.RETRIEVE_CATEGORIES:
                    mSupportManager.reloadStageThree();
                    break;
                case What.RETRIEVE_MARKER_ICONS:
                    mSupportManager.reloadMarkerIcon();
                    break;
                case What.RETRIEVE_NODETYPES:
                    mSupportManager.reloadStageFour();
                    if(UtilsMisc.isTablet(getApplicationContext())){
                        startupAppDelayed();
                    }
                    break;
                case What.RETRIEVE_TOTAL_NODE_COUNT:
                    mSupportManager.reloadTotalNodeCount();
                    startupAppDelayed();
                    break;
            }
        } else if (resultCode == RestService.STATUS_ERROR) {
            final RestServiceException e = resultData
                    .getParcelable(Extra.EXCEPTION);
            Log.w(TAG, e);
            showErrorDialog(e);
        }
    }

    private static class State {

        public DetachableResultReceiver mReceiver;

        State() {
            mReceiver = new DetachableResultReceiver(new Handler());
        }
    }

    private void showDialogNotWorking() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(R.string.error_title_occurred);
        builder.setMessage(getResources().getString(
                R.string.error_not_enough_memory));
        builder.setPositiveButton(R.string.btn_quit,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void showErrorDialog(RestServiceException e) {
        if (!mIsInForeground) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (e.getErrorCode() == RestServiceException.ERROR_NETWORK_FAILURE) {
            builder.setTitle(R.string.error_network_title);
        } else {
            builder.setTitle(R.string.error_title_occurred);
        }
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setMessage(e.getRessourceString());
        builder.setPositiveButton(R.string.btn_quit,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }
}
