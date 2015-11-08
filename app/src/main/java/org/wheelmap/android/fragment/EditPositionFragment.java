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

import org.mapsforge.android.maps.GeoPoint;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.views.MapView;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.ParceableBoundingBox;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import de.akquinet.android.androlog.Log;
import de.greenrobot.event.EventBus;

public class EditPositionFragment extends Fragment implements DisplayFragment,
        MapListener {

    public static final String TAG = EditPositionFragment.class.getSimpleName();

    private IMapController mMapController;

    private static String baseUrl = "http://a.tiles.mapbox.com/v3/%s/";

    private static String tileUrl;

    private static final byte ZOOMLEVEL_MIN = 16;

    private OnlineTileSourceBase mMapBoxTileSource; private static final float SPAN_ENLARGEMENT_FAKTOR = 1.3f;

    private MapView mMapView;

    private IGeoPoint mLastRequestedPosition;

    private EventBus mBus;

    private WorkerFragment mWorkerFragment;

    private DisplayFragmentListener mDisplayFramentListener;

    private LinearLayout text_move_map;

    private TextView text_position;

    private double mCrrLatitude;

    private double mCrrLongitude;

    private final static int VERTICAL_DELTA = 20;

    private int mVerticalDelta;

    private ImageButton positionSave;

    private OnEditPositionListener mListener;


    @Override
    public void onUpdate(WorkerFragment fragment) {
        if (mDisplayFramentListener != null) {
            mDisplayFramentListener.onRefreshing(fragment.isRefreshing());
        }
    }

    @Override
    public void markItem(ContentValues values, boolean centerToItem) {

    }

    @Override
    public void onRefreshStarted() {

    }

   public interface OnEditPositionListener {
        public void onEditPosition(double latitude, double longitude);
   }
    public static EditPositionFragment newInstance(double latitude,
            double longitude) {
        Bundle b = new Bundle();
        b.putDouble(Extra.LATITUDE, latitude);
        b.putDouble(Extra.LONGITUDE, longitude);

        EditPositionFragment f = new EditPositionFragment();
        f.setArguments(b);

        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnEditPositionListener) {
            mListener = (OnEditPositionListener) activity;
        }

        if (activity instanceof DisplayFragmentListener) {
            mDisplayFramentListener = (DisplayFragmentListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            executeState(savedInstanceState);
        } else if (getArguments() != null) {
            executeState(getArguments());
        }

        tileUrl = String.format(Locale.US, baseUrl, getString(R.string.mapbox_key));
                mMapBoxTileSource = new XYTileSource("Mapbox", null, 3, 21, 256, ".png", new String[] { tileUrl });
                mBus = EventBus.getDefault();
                mVerticalDelta = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, (float) VERTICAL_DELTA,
                getResources().getDisplayMetrics());

        attachWorkerFragment();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_position_edit, container,
                false);

        text_position = (TextView) v.findViewById(R.id.position_edit_text);
        text_move_map = (LinearLayout) v.findViewById(R.id.position_move_map);

        mMapView = (MapView) v.findViewById(R.id.map);
        mMapView.setTileSource(mMapBoxTileSource);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);

        mMapController = mMapView.getController();

        mMapView.setBuiltInZoomControls(true);
        mMapController = mMapView.getController();
        mMapController.setZoom(18);
        mMapController.setCenter(new org.osmdroid.mapsforge.wrapper.GeoPoint(new GeoPoint(mCrrLatitude, mCrrLongitude)));


        mMapView.setMapListener(this);

        positionSave = (ImageButton) v.findViewById(R.id.position_save);

        positionSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                save();
            }
        });
        return v;
    }

    public void save(){
        Bundle b = new Bundle();
        b.putDouble(Extra.LATITUDE,mCrrLatitude);
        b.putDouble(Extra.LONGITUDE,mCrrLongitude);
        Intent intent = getActivity().getIntent().putExtras(b);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putDouble(Extra.LATITUDE, mCrrLatitude);
        outState.putDouble(Extra.LONGITUDE, mCrrLongitude);

        super.onSaveInstanceState(outState);
    }

    private void executeState(Bundle state) {
        if (state == null) {
            return;
        }

        mCrrLatitude = state.getDouble(Extra.LATITUDE);
        mCrrLongitude = state.getDouble(Extra.LONGITUDE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ab_positionedit_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return false;
    }

    @Override
    public boolean onScroll(ScrollEvent event) {

         try{
             getResources();
         }catch(Exception e){
             //not attached
             return false;
         }

         Log.d(TAG, "onMove");

         text_move_map.setVisibility(View.GONE);

         IGeoPoint centerLocation = mMapView.getMapCenter();
         int minimalLatitudeSpan = mMapView.getLatitudeSpan() / 3;
         int minimalLongitudeSpan = mMapView.getLongitudeSpan() / 3;

         String positionText = String.format("%s: (%.6f:%.6f)", getResources()
                 .getString(R.string.position_geopoint), centerLocation.getLatitude(), centerLocation.getLongitude());

         text_position.setText(positionText);

         mCrrLatitude = centerLocation.getLatitude();
         mCrrLongitude = centerLocation.getLongitude();

         if (mLastRequestedPosition != null
                 && (Math.abs(mLastRequestedPosition.getLatitudeE6()
                 - centerLocation.getLatitudeE6()) < minimalLatitudeSpan)
                 && (Math.abs(mLastRequestedPosition.getLongitudeE6()
                 - centerLocation.getLongitudeE6()) < minimalLongitudeSpan)) {
             return false;
         }

         if (mMapView.getZoomLevel() < ZOOMLEVEL_MIN) {
             return false;
         }

         requestUpdate();
         return false;

    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        return false;
    }

   private void requestUpdate() {
       if(true){
          return;
       }
       Bundle extras = fillExtrasWithBoundingRect();
       mWorkerFragment.requestUpdate(extras);
   }

    private Bundle fillExtrasWithBoundingRect() {
        Bundle bundle = new Bundle();

        int latSpan = (int) (mMapView.getLatitudeSpan() * SPAN_ENLARGEMENT_FAKTOR);
        int lonSpan = (int) (mMapView.getLongitudeSpan() * SPAN_ENLARGEMENT_FAKTOR);
        IGeoPoint center = mMapView.getMapCenter();
        mLastRequestedPosition = center;
        ParceableBoundingBox boundingBox = new ParceableBoundingBox(
                center.getLatitudeE6() + (latSpan / 2), center.getLongitudeE6()
                + (lonSpan / 2),
                center.getLatitudeE6() - (latSpan / 2), center.getLongitudeE6()
                - (lonSpan / 2));
        bundle.putSerializable(Extra.BOUNDING_BOX, boundingBox);

        return bundle;
    }

    private void attachWorkerFragment() {
        Fragment fragment = null;
        if (getArguments() == null
                || getArguments()
                .getBoolean(Extra.CREATE_WORKER_FRAGMENT, true)) {
            //mHeightFull = true;
            FragmentManager fm = getActivity().getSupportFragmentManager();
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
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fragment = (Fragment) fm.findFragmentByTag(CombinedWorkerFragment.TAG);
        }

        mWorkerFragment = (WorkerFragment) fragment;
        mWorkerFragment.registerDisplayFragment(this);
        Log.d(TAG, "result mWorkerFragment = " + mWorkerFragment);
    }
}
