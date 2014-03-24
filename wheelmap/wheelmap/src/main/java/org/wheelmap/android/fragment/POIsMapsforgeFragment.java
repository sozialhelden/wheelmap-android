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
package org.wheelmap.android.fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapController;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.MapView.OnMoveListener;
import org.mapsforge.android.maps.MapView.OnZoomListener;
import org.mapsforge.android.maps.Projection;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.wheelmap.android.activity.MapActivity;
import org.wheelmap.android.app.AppCapability;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.fragment.SearchDialogFragment.OnSearchDialogListener;
import org.wheelmap.android.manager.MyLocationManager;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.online.R;
import org.wheelmap.android.overlays.MyLocationOverlay;
import org.wheelmap.android.overlays.OnTapListener;
import org.wheelmap.android.overlays.POIsCursorMapsforgeOverlay;
import org.wheelmap.android.utils.ParceableBoundingBox;
import org.wheelmap.android.utils.UtilsMisc;
import org.wheelmap.android.view.CompassView;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import de.akquinet.android.androlog.Log;
import de.greenrobot.event.EventBus;

public class POIsMapsforgeFragment extends Fragment implements
        DisplayFragment, OnMoveListener, OnZoomListener, OnTapListener,
        OnSearchDialogListener, OnExecuteBundle {

    public final static String TAG = POIsMapsforgeFragment.class
            .getSimpleName();

    private static final float SPAN_ENLARGEMENT_FAKTOR = 1.3f;

    private static final byte ZOOMLEVEL_MIN = 16;

    private static final int MAP_ZOOM_DEFAULT = 18; // Zoon 1 is world view

    private WorkerFragment mWorkerFragment;

    private DisplayFragmentListener mListener;

    private Bundle mDeferredExecuteBundle;

    private MapView mMapView;

    private MapController mMapController;

    private POIsCursorMapsforgeOverlay mPoisItemizedOverlay;

    private MyLocationOverlay mCurrLocationOverlay;

    private GeoPoint mLastRequestedPosition;

    private GeoPoint mCurrentLocationGeoPoint;

    private boolean mHeightFull = true;

    private boolean isCentered;

    private int oldZoomLevel = 18;

    private CompassView mCompass;

    private Cursor mCursor;

    private SensorManager mSensorManager;

    private Sensor mSensor;

    private boolean mOrientationAvailable;

    private Location mLocation;

    private EventBus mBus;

    public POIsMapsforgeFragment() {
        // noop
    }

    public static POIsMapsforgeFragment newInstance(boolean createWorker,
            boolean disableSearch) {
        createWorker = false;
        POIsMapsforgeFragment f = new POIsMapsforgeFragment();
        Bundle b = new Bundle();
        b.putBoolean(Extra.CREATE_WORKER_FRAGMENT, createWorker);
        b.putBoolean(Extra.DISABLE_SEARCH, disableSearch);

        f.setArguments(b);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof DisplayFragmentListener) {
            mListener = (DisplayFragmentListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mBus = EventBus.getDefault();

        mSensorManager = (SensorManager) getActivity().getSystemService(
                Context.SENSOR_SERVICE);
        // noinspection deprecation
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mOrientationAvailable = mSensor != null;
        attachWorkerFragment();
        // retrieveInitialLocation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater
                .inflate(R.layout.fragment_mapsforge, container, false);

        mMapView = (MapView) v.findViewById(R.id.map);

        mMapView.setClickable(true);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setScaleBar(true);
        mMapController = mMapView.getController();

        // overlays
        mPoisItemizedOverlay = new POIsCursorMapsforgeOverlay(getActivity(),
                this);
        mCurrLocationOverlay = new MyLocationOverlay(getActivity());

        if (AppCapability.degradeLargeMapQuality()) {
            mPoisItemizedOverlay.enableLowDrawQuality(true);
            mCurrLocationOverlay.enableLowDrawQuality(true);
            mCurrLocationOverlay.enableUseOnlyOneBitmap(true);
        }
        mMapView.getOverlays().add(mPoisItemizedOverlay);
        mMapView.getOverlays().add(mCurrLocationOverlay);
        mMapController.setZoom(oldZoomLevel);
        mMapView.setMoveListener(this);
        mMapView.setZoomListener(this);

        v.findViewById(R.id.my_location_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMapController.setZoom(17);
                centerMap(mCurrentLocationGeoPoint, true);
                requestUpdate();
            }
        });
        mCompass = (CompassView) v.findViewById(R.id.compass);


        if(savedInstanceState != null){
            int la = savedInstanceState.getInt(Extra.LATITUDE);
            int lo = savedInstanceState.getInt(Extra.LONGITUDE);
            byte zoom = savedInstanceState.getByte(Extra.ZOOM_LEVEL);

            mCurrentLocationGeoPoint = new GeoPoint(la,lo);

            centerMap(mCurrentLocationGeoPoint, true);
            setZoomIntern(zoom);

        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!hasExecuteBundle()) {
            if (savedInstanceState != null) {
                executeBundle(savedInstanceState);
            } else {
                executeMapDeferred(null, 0, false, true);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.registerSticky(this);
        mBus.post(MyLocationManager.RegisterEvent.INSTANCE);
    }

    @Override
    public void onStop() {
        super.onStop();
        mBus.post(MyLocationManager.UnregisterEvent.INSTANCE);
        mBus.unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mOrientationAvailable) {
            mSensorManager.registerListener(mSensorEventListener, mSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        executeState(retrieveExecuteBundle());
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOrientationAvailable) {
            mSensorManager.unregisterListener(mSensorEventListener);
        }
        mMapView.onPause();
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
        ((MapActivity) getActivity()).destroyMapView(mMapView);
        mWorkerFragment.unregisterDisplayFragment(this);
        WheelmapApp.getSupportManager().cleanReferences();


    }

    private void retrieveInitialLocation() {
        MyLocationManager.LocationEvent event = (MyLocationManager.LocationEvent) mBus.getStickyEvent(MyLocationManager.LocationEvent.class);
        mLocation = event.location;
    }

    private void attachWorkerFragment() {
        Fragment fragment = null;
        if (getArguments() == null
                || getArguments()
                .getBoolean(Extra.CREATE_WORKER_FRAGMENT, true)) {
            mHeightFull = true;
            FragmentManager fm = getFragmentManager();
            fragment = (Fragment) fm.findFragmentByTag(POIsMapWorkerFragment.TAG);
            Log.d(TAG, "Looking for Worker Fragment:" + fragment);
            if (fragment == null) {
                fragment = new POIsMapWorkerFragment();
                fm.beginTransaction()
                        .add(fragment, POIsMapWorkerFragment.TAG)
                        .commit();

            }

        } else if (!getArguments().getBoolean(Extra.CREATE_WORKER_FRAGMENT,
                false)) {
            Log.d(TAG, "Connecting to Combined Worker Fragment");
            FragmentManager fm = getFragmentManager();
            fragment = (Fragment) fm.findFragmentByTag(CombinedWorkerFragment.TAG);
        }

        mWorkerFragment = (WorkerFragment) fragment;
        mWorkerFragment.registerDisplayFragment(this);
        Log.d(TAG, "result mWorkerFragment = " + mWorkerFragment);
    }

    @Override
    public void executeBundle(Bundle bundle) {
        Log.d(TAG, "executeBundle fragment is visible = " + isVisible());
        if (isVisible()) {
            bundle.putBoolean(Extra.EXPLICIT_DIRECT_RETRIEVAL, true);
            executeState(bundle);
        } else {
            storeExecuteBundle(bundle);
        }
    }

    private void executeState(Bundle bundle) {
        if (bundle == null) {
            return;
        }

        mHeightFull = bundle.getBoolean(Extra.MAP_HEIGHT_FULL, false);

        boolean doRequest = false;
        boolean doCenter = false;
        GeoPoint centerPoint = null;
        int zoom = 0;

        if (bundle.containsKey(Extra.REQUEST)) {
            doRequest = true;
        }

        if (bundle.containsKey(Extra.CENTER_MAP)) {
            double lat = bundle.getDouble(Extra.LATITUDE);
            double lon = bundle.getDouble(Extra.LONGITUDE);
            zoom = bundle.getInt(Extra.ZOOM_MAP, MAP_ZOOM_DEFAULT);
            centerPoint = new GeoPoint(lat, lon);
            doCenter = true;
        }

        if (doCenter || doRequest) {
            executeMapDeferred(centerPoint, zoom, doCenter, doRequest);
        }
    }

    private boolean hasExecuteBundle() {
        return mDeferredExecuteBundle != null;
    }

    private void storeExecuteBundle(Bundle bundle) {
        mDeferredExecuteBundle = bundle;
    }

    private Bundle retrieveExecuteBundle() {
        Bundle result = mDeferredExecuteBundle;
        mDeferredExecuteBundle = null;
        return result;
    }

    private void executeMapDeferred(final GeoPoint centerPoint, final int zoom,
            final boolean center, final boolean request) {
        mMapView.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        Log.d("onGlobalLayout: doing mapview center deffered");
                        if (center) {
                            executeMapPositioning(centerPoint, zoom);
                        }
                        if (request && mMapView.getWidth()>0) {
                            centerMap(mCurrentLocationGeoPoint, true);
                            requestUpdate();
                        }

                        mMapView.getViewTreeObserver()
                                .removeGlobalOnLayoutListener(this);
                    }
                });
    }

    private void executeMapPositioning(GeoPoint geoPoint, int zoom) {
        if (geoPoint != null) {
            centerMap(geoPoint, true);
        }
        if (zoom != 0) {
            setZoomIntern(zoom);
        }
        markItemIntern(geoPoint, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Extra.MAP_HEIGHT_FULL, mHeightFull);

        GeoPoint current_location = mMapView.getMapCenter();
        byte zoomlevel = mMapView.getZoomLevel();

        outState.putByte(Extra.ZOOM_LEVEL,zoomlevel);
        outState.putInt(Extra.LATITUDE,current_location.getLatitudeE6());
        outState.putInt(Extra.LONGITUDE, current_location.getLongitudeE6());


        //outState.putInt(Extra.LATITUDE,mCurrentLocationGeoPoint.getLatitudeE6());
        //outState.putInt(Extra.LONGITUDE, mCurrentLocationGeoPoint.getLongitudeE6());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.ab_map_fragment, menu);
        if (getArguments().containsKey(Extra.DISABLE_SEARCH)) {
           // menu.removeItem(R.id.menu_search);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_search:
                showSearch();
                return true;
            /*case R.id.menu_location:
                centerMap(mCurrentLocationGeoPoint, true);
                requestUpdate();
                break;       */
            default:
                // noop
        }

        return false;
    }

    @Override
    public void onRefreshStarted() {
        // do nothing
        Log.d("");
    }

    @Override
    public void onMove(float vertical, float horizontal) {
        Log.d(TAG, "onMove");
        GeoPoint centerLocation = mMapView.getMapCenter();
        int minimalLatitudeSpan = mMapView.getLatitudeSpan() / 3;
        int minimalLongitudeSpan = mMapView.getLongitudeSpan() / 3;

        if (mLastRequestedPosition != null
                && (Math.abs(mLastRequestedPosition.getLatitudeE6()
                - centerLocation.getLatitudeE6()) < minimalLatitudeSpan)
                && (Math.abs(mLastRequestedPosition.getLongitudeE6()
                - centerLocation.getLongitudeE6()) < minimalLongitudeSpan)) {
            return;
        }

        if (mMapView.getZoomLevel() < ZOOMLEVEL_MIN) {
            return;
        }

        requestUpdate();
    }

    @Override
    public void onZoom(byte zoomLevel) {
        Log.d(TAG, "onZoom");
        boolean isZoomedEnough = true;

        if (zoomLevel < ZOOMLEVEL_MIN) {
            isZoomedEnough = false;
            oldZoomLevel = zoomLevel;
            return;
        }

        if (zoomLevel < oldZoomLevel) {
            isZoomedEnough = false;
        }

        if (isZoomedEnough && zoomLevel >= oldZoomLevel) {
            oldZoomLevel = zoomLevel;
            return;
        }

        requestUpdate();
        isZoomedEnough = true;
        oldZoomLevel = zoomLevel;
    }

    private void requestUpdate() {
        Bundle extras = fillExtrasWithBoundingRect();
        mWorkerFragment.requestUpdate(extras);
    }

    private Bundle fillExtrasWithBoundingRect() {
        Bundle bundle = new Bundle();

        int latSpan = (int) (mMapView.getLatitudeSpan() * SPAN_ENLARGEMENT_FAKTOR);
        int lonSpan = (int) (mMapView.getLongitudeSpan() * SPAN_ENLARGEMENT_FAKTOR);
        GeoPoint center = mMapView.getMapCenter();
        mLastRequestedPosition = center;
        ParceableBoundingBox boundingBox = new ParceableBoundingBox(
                center.getLatitudeE6() + (latSpan / 2), center.getLongitudeE6()
                + (lonSpan / 2),
                center.getLatitudeE6() - (latSpan / 2), center.getLongitudeE6()
                - (lonSpan / 2));
        bundle.putSerializable(Extra.BOUNDING_BOX, boundingBox);

        return bundle;
    }

    private void centerMap(GeoPoint geoPoint, boolean force) {
        Log.d(TAG, "centerMap: force = " + force + " isCentered = "
                + isCentered + " geoPoint = " + geoPoint);
        if (force || !isCentered) {
            setCenterWithOffset(geoPoint);
        }
    }

    private void setCenterWithOffset(GeoPoint geoPoint) {
        if (geoPoint == null) {
            return;
        }

        boolean land = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        GeoPoint actualGeoPoint;

        if (mHeightFull) {
            actualGeoPoint = geoPoint;
        } else {
            if(land){
                Projection projection = mMapView.getProjection();
                Point point = new Point();
                point = projection.toPixels(geoPoint, point);
                int horizontalOffset = mMapView.getWidth() / 4;
                point.x -= horizontalOffset;
                actualGeoPoint = projection.fromPixels(point.x, point.y);
            }else{
                Projection projection = mMapView.getProjection();
                Point point = new Point();
                point = projection.toPixels(geoPoint, point);
                int mVerticalOffset = mMapView.getHeight() / 4;
                point.y -= mVerticalOffset;
                actualGeoPoint = projection.fromPixels(point.x, point.y);
            }
        }

        mMapController.setCenter(actualGeoPoint);
       // mMapController.setZoom(17);
        isCentered = true;
    }

    public void setHeightFull(boolean heightFull) {
        mHeightFull = heightFull;
    }

    private void setZoomIntern(int zoom) {
        mMapView.setZoomListener(null);
        mMapController.setZoom(zoom);
        mMapView.setZoomListener(this);
        oldZoomLevel = zoom;
    }

    private void setCursor(Cursor cursor) {
        UtilsMisc.dumpCursorCompare(TAG, mCursor, cursor);
        if (cursor == mCursor) {
            return;
        }

        mCursor = cursor;
        mPoisItemizedOverlay.setCursor(mCursor);

        if (mWorkerFragment instanceof CombinedWorkerFragment) {
            markItemClear();
        }
    }

    @Override
    public void onTap(OverlayItem item, ContentValues values) {
        markItem(values, false);
        if (mListener != null) {
            mListener.onShowDetail(this, values);
        }
    }

    private void showSearch() {
        FragmentManager fm = getFragmentManager();
        SearchDialogFragment searchDialog = SearchDialogFragment.newInstance(
                false, true);

        searchDialog.setTargetFragment(this, 0);
        searchDialog.show(fm, SearchDialogFragment.TAG);
    }

    @Override
    public void onSearch(Bundle bundle) {
        Bundle boundingBoxExtras = fillExtrasWithBoundingRect();
        bundle.putAll(boundingBoxExtras);

        mWorkerFragment.requestSearch(bundle);
    }

    @Override
    public void onUpdate(WorkerFragment fragment) {
        setCursor(fragment.getCursor(WorkerFragment.MAP_CURSOR));

        if (mListener != null) {
            mListener.onRefreshing(fragment.isRefreshing());
        }
    }

    @Override
    public void markItem(ContentValues values, boolean centerToItem) {
        Log.d(TAG, "markItem");
        GeoPoint point = new GeoPoint(values.getAsDouble(POIs.LATITUDE),
                values.getAsDouble(POIs.LONGITUDE));
        markItemIntern(point, centerToItem);
    }

    private void markItemIntern(GeoPoint point, boolean centerToItem) {
        mCurrLocationOverlay.setItem(point);
        if (centerToItem) {
            centerMap(point, true);
        }
    }

    public void markItemClear() {
        mCurrLocationOverlay.unsetItem();
    }

    public void onEventMainThread(MyLocationManager.LocationEvent locationEvent) {
        mLocation = locationEvent.location;

        float accuracy = mLocation.getAccuracy();

        if(accuracy > 100){
            accuracy = 100;
        }

        GeoPoint geoPoint = new GeoPoint(mLocation.getLatitude(),
                mLocation.getLongitude());
        mCurrLocationOverlay.setLocation(geoPoint, accuracy);
        if (mMapView != null && !mMapView.hasInitializedCenter()) {
            centerMap(geoPoint, false);
        }

        mCurrentLocationGeoPoint = geoPoint;
    }

    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        private static final float MIN_DIRECTION_DELTA = 10;

        private float mDirection;

        @Override
        public void onSensorChanged(SensorEvent event) {
            float direction = event.values[0];
            if (direction > 180) {
                direction -= 360;
            }

            if (isAdded()) {
                direction -= UtilsMisc.calcRotationOffset(getActivity()
                        .getWindowManager().getDefaultDisplay());
            }

            float lastDirection = mDirection;
            if (Math.abs(direction - lastDirection) < MIN_DIRECTION_DELTA) {
                return;
            }

            updateDirection(direction);
            mDirection = direction;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        private void updateDirection(float direction) {
            mCompass.updateDirection(direction);
        }
    };
}
