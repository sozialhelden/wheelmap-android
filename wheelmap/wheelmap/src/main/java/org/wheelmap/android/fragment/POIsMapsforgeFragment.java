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
import org.mapsforge.android.maps.MapController;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.MapView.OnMoveListener;
import org.mapsforge.android.maps.MapView.OnZoomListener;
import org.mapsforge.android.maps.overlay.CircleOverlay;
import org.mapsforge.android.maps.overlay.OverlayCircle;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.app.WheelmapApp.Capability;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.ui.mapsforge.ConfigureMapView;
import org.wheelmap.android.ui.mapsforge.POIsCursorMapsforgeOverlay;
import org.wheelmap.android.utils.ParceableBoundingBox;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragment;

public class POIsMapsforgeFragment extends SherlockFragment implements OnMoveListener, OnZoomListener {
	private final static String TAG = "mapsforge";
	public final static String EXTRA_CREATE_WORKER_FRAGMENT = "org.wheelmap.android.CREATE_WORKER_FRAGMENT";
	public static final String EXTRA_CENTER_AT_LAT = "org.wheelmap.android.ui.Mapsforge.CENTER_AT_LAT";
	public static final String EXTRA_CENTER_AT_LON = "org.wheelmap.android.ui.Mapsforge.CENTER_AT_LON";
	public static final String EXTRA_CENTER_ZOOM = "org.wheelmap.android.ui.Mapsforge.CENTER_ZOOM";
	public static final String EXTRA_NO_RETRIEVAL = "org.wheelmap.android.ui.Mapsforge.NO_RETRIEVAL";
	
	private POIsMapsforgeWorkerFragment mWorkerFragment;
	
	private MapView mMapView;
	private MapController mMapController;
	private POIsCursorMapsforgeOverlay mPoisItemizedOverlay;
	private MyLocationOverlay mCurrLocationOverlay;
	private GeoPoint mLastRequestedPosition;

	private boolean mIsRecreated;
	private boolean isCentered;
	private boolean isZoomedEnough;
	private int oldZoomLevel = 18;
	private static final float SPAN_ENLARGEMENT_FAKTOR = 1.3f;
	private static final byte ZOOMLEVEL_MIN = 16;
	private GeoPoint mLastGeoPointE6;

	private OnPOIsMapsforgeFragmentListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		if ( activity instanceof OnPOIsMapsforgeFragmentListener )
			mListener = (OnPOIsMapsforgeFragmentListener)activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = (LinearLayout) inflater.inflate( R.layout.fragment_mapsforge,container, false );
		
		System.gc();
		mMapView = (MapView) v.findViewById(R.id.map);
		
		mMapView.setClickable(true);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setScaleBar(true);

		ConfigureMapView.pickAppropriateMap(getActivity().getApplicationContext(), mMapView);

		mMapController = mMapView.getController();

		// overlays
		mPoisItemizedOverlay = new POIsCursorMapsforgeOverlay(getActivity());
		mCurrLocationOverlay = new MyLocationOverlay();

		Capability cap = WheelmapApp.getCapabilityLevel();
		if (cap == Capability.DEGRADED_MIN || cap == Capability.DEGRADED_MAX) {
			mPoisItemizedOverlay.enableLowDrawQuality(true);
			mCurrLocationOverlay.enableLowDrawQuality(true);
			mCurrLocationOverlay.enableUseOnlyOneBitmap(true);

		}
		mMapView.getOverlays().add(mPoisItemizedOverlay);
		mMapView.getOverlays().add(mCurrLocationOverlay);
		mMapController.setZoom(oldZoomLevel); // Zoon 1 is world view
		mMapView.setMoveListener(this);
		mMapView.setZoomListener(this);
		
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if ( getArguments() == null || getArguments().getBoolean( EXTRA_CREATE_WORKER_FRAGMENT, true )) {
			FragmentManager fm = getFragmentManager();
			mWorkerFragment = (POIsMapsforgeWorkerFragment) fm.findFragmentByTag( POIsMapsforgeWorkerFragment.TAG);
			if ( mWorkerFragment == null ) {
				mWorkerFragment = new POIsMapsforgeWorkerFragment();
				fm.beginTransaction().add( mWorkerFragment, POIsMapsforgeWorkerFragment.TAG ).commit();
				mWorkerFragment.setTargetFragment( this, 0 );
			} else {
				mIsRecreated = true;	
			}
		}
		
		if ( savedInstanceState != null ) {
			executeTargetCenterExtras( savedInstanceState );
		}

		if ( getArguments() != null ) {
			executeRetrieval( getArguments() );
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		WheelmapApp.getSupportManager().cleanReferences();
		System.gc();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	private void executeTargetCenterExtras(Bundle extras) {
		if (extras.containsKey(EXTRA_CENTER_AT_LAT)) {
			int lat = extras.getInt(EXTRA_CENTER_AT_LAT);
			int lon = extras.getInt(EXTRA_CENTER_AT_LON);
			int zoom = extras.getInt(EXTRA_CENTER_ZOOM, 18);

			GeoPoint gp = new GeoPoint(lat, lon);
			mMapController.setCenter(gp);
			mMapView.setZoomListener(null);
			mMapController.setZoom(zoom); // Zoon 1 is world view
			mMapView.setZoomListener(this);
			isCentered = true;
			isZoomedEnough = true;
			oldZoomLevel = zoom;
		}
	}
	
	private void executeRetrieval(Bundle extras) {
		boolean retrieval = !extras.getBoolean(EXTRA_NO_RETRIEVAL, false);
		if (retrieval) {
			mMapView.getViewTreeObserver().addOnGlobalLayoutListener(
					new OnGlobalLayoutListener() {

						@Override
						public void onGlobalLayout() {
							mMapController.setZoom(18); // Zoon 1 is world view
							isZoomedEnough = true;
							oldZoomLevel = 18;
							requestUpdate();
							mMapView.getViewTreeObserver()
									.removeGlobalOnLayoutListener(this);
						}
					});
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		GeoPoint gp = mMapView.getMapCenter();
		outState.putInt(EXTRA_CENTER_AT_LAT, gp.getLatitudeE6());
		outState.putInt(EXTRA_CENTER_AT_LON, gp.getLongitudeE6());
		outState.putInt(EXTRA_CENTER_ZOOM, mMapView.getZoomLevel());
	}
	
	@Override
	public void onMove(float vertical, float horizontal) {
		GeoPoint centerLocation = mMapView.getMapCenter();
		int minimalLatitudeSpan = mMapView.getLatitudeSpan() / 3;
		int minimalLongitudeSpan = mMapView.getLongitudeSpan() / 3;

		if (mLastRequestedPosition != null
				&&
				(Math.abs(mLastRequestedPosition.getLatitudeE6()
						- centerLocation.getLatitudeE6()) < minimalLatitudeSpan)
				&& (Math.abs(mLastRequestedPosition.getLongitudeE6()
						- centerLocation.getLongitudeE6()) < minimalLongitudeSpan))
			return;

		if (mMapView.getZoomLevel() < ZOOMLEVEL_MIN)
			return;
		
		requestUpdate();
	}

	@Override
	public void onZoom(byte zoomLevel) {
		boolean isZoomedEnough = true;
		
		if (zoomLevel < ZOOMLEVEL_MIN ) {
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
		mWorkerFragment.requestUpdate( extras );
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
		bundle.putSerializable(SyncService.EXTRA_BOUNDING_BOX, boundingBox);
		
		return bundle;
	}
	
	protected void centerMap( GeoPoint geoPoint ) {
		if (!isCentered) {
			mMapController.setCenter(geoPoint);
			isCentered = true;
		}

		// we got the first time current position so center map on it
		if (mLastGeoPointE6 == null && !isCentered) {
			mMapController.setCenter(geoPoint);
			isCentered = true;
		}
	}
	
	protected void updateCurrentLocation( GeoPoint geoPoint, Location location ) {
		mLastGeoPointE6 = geoPoint;
		mCurrLocationOverlay.setLocation(mLastGeoPointE6, location.getAccuracy());
	}
	
	protected void setCursor( Cursor cursor ) {
		mPoisItemizedOverlay.setCursor( cursor );
	}
	
	public void navigateToLocation() {
		mMapController.setCenter( mLastGeoPointE6 );
		requestUpdate();
	}
	
	public void startSearch( Bundle extras ) {
		Bundle boundingBoxExtras = fillExtrasWithBoundingRect();
		extras.putAll( boundingBoxExtras );
		
		mWorkerFragment.executeSearch( extras );
	}
	
	public interface OnPOIsMapsforgeFragmentListener {

	}
	
	private static class MyLocationOverlay extends CircleOverlay<OverlayCircle> {
		OverlayCircle mCircleLarge, mCircleSmall;
		private final static float RADIUS_SMALL_CIRCLE = 2.0f;
		private final static int NUMBER_OF_CIRCLES = 2;

		public MyLocationOverlay() {
			super(null, null);

			Paint fillPaintDark = new Paint(Paint.ANTI_ALIAS_FLAG);
			fillPaintDark.setARGB(60, 127, 159, 239);

			Paint outlinePaintDark = new Paint(Paint.ANTI_ALIAS_FLAG);
			outlinePaintDark.setARGB(255, 79, 92, 140);
			outlinePaintDark.setStrokeWidth(4);
			outlinePaintDark.setStyle(Style.STROKE);

			Paint fillPaintLight = new Paint(Paint.ANTI_ALIAS_FLAG);
			fillPaintLight.setARGB(255, 47, 111, 223);

			Paint outlinePaintLight = new Paint(Paint.ANTI_ALIAS_FLAG);
			outlinePaintLight.setARGB(255, 132, 132, 132);
			outlinePaintLight.setStrokeWidth(10);
			outlinePaintLight.setStyle(Style.STROKE);

			mCircleLarge = new OverlayCircle(fillPaintDark, outlinePaintDark);
			mCircleSmall = new OverlayCircle(fillPaintLight, outlinePaintLight);
		}

		public void setLocation(GeoPoint center, float radius) {
			mCircleLarge.setCircleData(center, radius);
			mCircleSmall.setCircleData(center, RADIUS_SMALL_CIRCLE);
			populate();
		}

		@Override
		public int size() {
			return NUMBER_OF_CIRCLES;
		}

		@Override
		protected OverlayCircle createCircle(int i) {
			if (i == 1)
				return mCircleLarge;
			else
				return mCircleSmall;
		}
	}
	
}
