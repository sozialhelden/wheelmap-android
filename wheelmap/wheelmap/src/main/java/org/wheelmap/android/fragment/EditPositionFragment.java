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
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.online.R;
import org.wheelmap.android.overlays.ConfigureMapView;
import org.wheelmap.android.overlays.OnTapListener;
import org.wheelmap.android.overlays.POILocationEditableOverlay;
import org.wheelmap.android.utils.ParceableBoundingBox;
import org.wheelmap.android.utils.UtilsMisc;
import org.mapsforge.android.maps.MapView.OnMoveListener;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.akquinet.android.androlog.Log;

public class EditPositionFragment extends Fragment implements DisplayFragment,
        OnMoveListener {

    public static final String TAG = EditPositionFragment.class.getSimpleName();

    private MapController mMapController;



    private static final byte ZOOMLEVEL_MIN = 16;

    private static final float SPAN_ENLARGEMENT_FAKTOR = 1.3f;

    private WorkerFragment mWorkerFragment;

    private DisplayFragmentListener mDisplayFramentListener;

    private LinearLayout text_move_map;

    private TextView text_position;

    private MapView mMapView;

    private POILocationEditableOverlay mMapOverlay;

    private double mCrrLatitude;

    private double mCrrLongitude;

    private final static int VERTICAL_DELTA = 20;

    private int mVerticalDelta;

    private ImageButton positionSave;

    private OnEditPositionListener mListener;

    private GeoPoint mLastRequestedPosition;

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

        mMapView = (MapView) v.findViewById(R.id.map);
        text_position = (TextView) v.findViewById(R.id.position_edit_text);
        text_move_map = (LinearLayout) v.findViewById(R.id.position_move_map);

        mMapView.setClickable(true);

        mMapView.setBuiltInZoomControls(true);
        ConfigureMapView.pickAppropriateMap(getActivity(), mMapView);
        mMapController = mMapView.getController();
        mMapController.setZoom(18);
        mMapController.setCenter(new GeoPoint(mCrrLatitude, mCrrLongitude));
        mMapOverlay = new POILocationEditableOverlay(mCrrLatitude,
                mCrrLongitude, getResources().getDrawable(
                R.drawable.ic_action_location_pin_wm));
        mMapOverlay.enableLowDrawQuality(true);
        mMapOverlay.enableUseOnlyOneBitmap(true);
        mMapView.getOverlays().add(mMapOverlay);
        mMapView.setMoveListener(this);

        //mMapView.setOnTouchListener(this);

        positionSave = (ImageButton) v.findViewById(R.id.position_save);

        positionSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onEditPosition(mCrrLatitude, mCrrLongitude);
                }
            }
        });




        return v;
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

        switch (id) {
            /*case R.id.menu_save:
                if (mListener != null) {
                    mListener.onEditPosition(mCrrLatitude, mCrrLongitude);
                }
                break;
            default:
                // noop
                */
        }

        return false;
    }

    @Override
    public void onMove(float vertical, float horizontal) {
        Log.d(TAG, "onMove");

        text_move_map.setVisibility(View.GONE);

        GeoPoint centerLocation = mMapView.getMapCenter();
        int minimalLatitudeSpan = mMapView.getLatitudeSpan() / 3;
        int minimalLongitudeSpan = mMapView.getLongitudeSpan() / 3;

        String positionText = String.format("%s: (%.6f:%.6f)", getResources()
                .getString(R.string.position_geopoint), centerLocation.getLatitude(), centerLocation.getLongitude());

        text_position.setText(positionText);

        mCrrLatitude = centerLocation.getLatitude();
        mCrrLongitude = centerLocation.getLongitude();
        mMapOverlay.setPosition(centerLocation);

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
    /*
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            //case MotionEvent.ACTION_MOVE:
                GeoPoint geoPoint = mMapView.getProjection().fromPixels(
                        (int) event.getX(), (int) event.getY() + mVerticalDelta);
                mCrrLatitude = geoPoint.getLatitude();
                mCrrLongitude = geoPoint.getLongitude();
                mMapOverlay.setPosition(geoPoint);

                return true;
        }
        return false;
    }  */

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

    private void attachWorkerFragment() {
        Fragment fragment = null;
        if (getArguments() == null
                || getArguments()
                .getBoolean(Extra.CREATE_WORKER_FRAGMENT, true)) {
            //mHeightFull = true;
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
}
