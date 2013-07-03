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
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.online.R;
import org.wheelmap.android.overlays.ConfigureMapView;
import org.wheelmap.android.overlays.POILocationEditableOverlay;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

public class EditPositionFragment extends Fragment implements
        OnTouchListener {

    public static final String TAG = EditPositionFragment.class.getSimpleName();

    private MapController mMapController;

    private MapView mMapView;

    private POILocationEditableOverlay mMapOverlay;

    private double mCrrLatitude;

    private double mCrrLongitude;

    private final static int VERTICAL_DELTA = 20;

    private int mVerticalDelta;

    private OnEditPositionListener mListener;

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_position_edit, container,
                false);

        mMapView = (MapView) v.findViewById(R.id.map);

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
        mMapView.setOnTouchListener(this);

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
            case R.id.menu_save:
                if (mListener != null) {
                    mListener.onEditPosition(mCrrLatitude, mCrrLongitude);
                }
                break;
            default:
                // noop
        }

        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                GeoPoint geoPoint = mMapView.getProjection().fromPixels(
                        (int) event.getX(), (int) event.getY() + mVerticalDelta);
                mCrrLatitude = geoPoint.getLatitude();
                mCrrLongitude = geoPoint.getLongitude();
                mMapOverlay.setPosition(geoPoint);
                return true;
        }
        return false;
    }

}
