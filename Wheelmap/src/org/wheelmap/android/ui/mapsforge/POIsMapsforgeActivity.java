/*
Copyright (C) 2011 Michal Harakal and Michael Kroez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS-IS" BASIS
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
        
 */

package org.wheelmap.android.ui.mapsforge;

import org.mapsforge.android.maps.CircleOverlay;
import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapController;
import org.mapsforge.android.maps.OverlayCircle;
import org.wheelmap.android.R;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.manager.MyLocationManager;
import org.wheelmap.android.model.QueriesBuilderHelper;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.ui.InfoActivity;
import org.wheelmap.android.ui.NewSettingsActivity;
import org.wheelmap.android.ui.POIsListActivity;
import org.wheelmap.android.ui.mapsforge.MyMapView.MapViewTouchMove;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.ParceableBoundingBox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ProgressBar;
import android.widget.TextView;

public class POIsMapsforgeActivity extends MapActivity implements
		DetachableResultReceiver.Receiver, MapViewTouchMove {

	private final static String TAG = "mapsforge";

	public static final String EXTRA_NO_RETRIEVAL = "org.wheelmap.android.ui.Mapsforge.NO_RETRIEVAL";
	public static final String EXTRA_CENTER_AT_LAT = "org.wheelmap.android.ui.Mapsforge.CENTER_AT_LAT";
	public static final String EXTRA_CENTER_AT_LON = "org.wheelmap.android.ui.Mapsforge.CENTER_AT_LON";
	public static final String EXTRA_CENTER_ZOOM = "org.wheelmap.android.ui.Mapsforge.CENTER_ZOOM";


	/** State held between configuration changes. */
	private State mState;

	private MapController mMapController;
	private MyMapView mMapView;
	private POIsCursorMapsforgeOverlay mPoisItemizedOverlay;
	private MyLocationOverlay mCurrLocationOverlay;

	private ProgressBar mProgressBar;

	private MyLocationManager mLocationManager;
	private GeoPoint mLastGeoPointE6;
	private boolean isCentered;

	private static final int ZOOMLEVEL_MIN = 16;
	private static final float SPAN_ENLARGEMENT_FAKTOR = 1.3f;
	private boolean isInForeground;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.gc();

		setContentView(R.layout.activity_mapsforge);
		mMapView = (MyMapView) findViewById(R.id.map);
		mProgressBar = (ProgressBar) findViewById(R.id.progressbar_map);

		mMapView.setClickable(true);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setScaleBar(true);

		ConfigureMapView.pickAppropriateMap(this, mMapView);

		mMapController = mMapView.getController();

		// overlays
		mPoisItemizedOverlay = new POIsCursorMapsforgeOverlay(this);
		runQuery();

		mMapView.getOverlays().add(mPoisItemizedOverlay);

		mCurrLocationOverlay = new MyLocationOverlay();
		mMapView.getOverlays().add(mCurrLocationOverlay);
		mMapView.registerListener(this);
		mMapController.setZoom(18); // Zoon 1 is world view

		isCentered = false;

		if (getIntent() != null) {
			Bundle extras = getIntent().getExtras();

			executeTargetCenterExtras(extras);
			executeRetrieval(extras);
		}

		mState = (State) getLastNonConfigurationInstance();
		final boolean previousState = mState != null;
		if (previousState) {
			// Start listening for SyncService updates again
			mState.mReceiver.setReceiver(this);
			updateRefreshStatus();

		} else {
			mState = new State();
			mState.mReceiver.setReceiver(this);
			updateRefreshStatus();
		}

		mLocationManager = MyLocationManager.get(mState.mReceiver, true);

		TextView listView = (TextView) findViewById(R.id.switch_list);

		// Attach event handlers
		listView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(POIsMapsforgeActivity.this,
						POIsListActivity.class);
				intent.putExtra(POIsMapsforgeActivity.EXTRA_NO_RETRIEVAL, false);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(intent);
				overridePendingTransition(0, 0);

			}

		});
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Bundle extras = intent.getExtras();
		executeTargetCenterExtras(extras);
		executeRetrieval(extras);
	}

	@Override
	protected void onResume() {
		super.onResume();
		isInForeground = true;
		Log.d(TAG, "onResume isInForeground = " + isInForeground);
		// We dont need that here as we do a runQuery anyways

		// mPoisItemizedOverlay.reload();
		mLocationManager.register(mState.mReceiver, true);
		runQuery();
	}

	@Override
	public void onPause() {
		super.onPause();
		isInForeground = false;
		Log.d(TAG, "onPause isInForeground = " + isInForeground);
		mPoisItemizedOverlay.deactivateCursor();
		mLocationManager.release(mState.mReceiver);
		System.gc();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.d("lowmemory", "mapsforge - onLowMemory");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		WheelmapApp.getSupportManager().cleanReferences();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		executeTargetCenterExtras( savedInstanceState );
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		GeoPoint gp = mMapView.getMapCenter();
		outState.putInt(EXTRA_CENTER_AT_LAT, gp.getLatitudeE6());
		outState.putInt(EXTRA_CENTER_AT_LON, gp.getLongitudeE6());
		outState.putInt( EXTRA_CENTER_ZOOM, mMapView.getZoomLevel());
	}

	private void executeTargetCenterExtras(Bundle extras) {
		if (extras == null) {
			return;
		}
		if (extras.containsKey(EXTRA_CENTER_AT_LAT)) {
			int lat = extras.getInt(EXTRA_CENTER_AT_LAT);
			int lon = extras.getInt(EXTRA_CENTER_AT_LON);
			int zoom = extras.getInt( EXTRA_CENTER_ZOOM, 18 );
			
			GeoPoint gp = new GeoPoint(lat, lon);
			mMapController.setCenter(gp);
			mMapController.setZoom(zoom); // Zoon 1 is world view
			isCentered = true;
		}
	}

	private void executeRetrieval(Bundle extras) {
		boolean retrieval = ! extras.getBoolean(EXTRA_NO_RETRIEVAL, false);
		Log.d( TAG, "retrieval = " + retrieval );
		if (retrieval) {
			mMapView.getViewTreeObserver().addOnGlobalLayoutListener(
					new OnGlobalLayoutListener() {

						@Override
						public void onGlobalLayout() {
							mMapController.setZoom(18); // Zoon 1 is world view
							requestUpdate();
							mMapView.getViewTreeObserver()
									.removeGlobalOnLayoutListener(this);
						}
					});
		}
	}

	private void runQuery() {
		// Run query
		Uri uri = Wheelmap.POIs.CONTENT_URI;
		Cursor cursor = getContentResolver().query(
				uri,
				Wheelmap.POIs.PROJECTION,
				QueriesBuilderHelper
						.userSettingsFilter(getApplicationContext()), null,
				Wheelmap.POIs.DEFAULT_SORT_ORDER);

		mPoisItemizedOverlay.setCursor(cursor);
	}

	/** {@inheritDoc} */
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Log.d(TAG, "onReceiveResult in mapsforge resultCode = " + resultCode);
		switch (resultCode) {
		case SyncService.STATUS_RUNNING: {
			mState.mSyncing = true;
			updateRefreshStatus();
			break;
		}
		case SyncService.STATUS_FINISHED: {
			mState.mSyncing = false;
			updateRefreshStatus();
			break;
		}
		case SyncService.STATUS_ERROR: {
			mState.mSyncing = false;
			updateRefreshStatus();
			SyncServiceException e = resultData
					.getParcelable(SyncService.EXTRA_ERROR);
			showErrorDialog(e);
			break;
		}
		case MyLocationManager.WHAT_LOCATION_MANAGER_UPDATE: {
			Location location = (Location) resultData
					.getParcelable(MyLocationManager.EXTRA_LOCATION_MANAGER_LOCATION);
			GeoPoint geoPoint = calcGeoPoint(location);
			if (!isCentered) {
				mMapController.setCenter(geoPoint);
				isCentered = true;
			}

			// we got the first time current position so center map on it
			if (mLastGeoPointE6 == null && !isCentered) {
				// findViewById(R.id.btn_title_gps).setVisibility(View.VISIBLE);
				mMapController.setCenter(geoPoint);
			}
			mLastGeoPointE6 = geoPoint;
			mCurrLocationOverlay.setLocation(mLastGeoPointE6,
					location.getAccuracy());
			break;
		}

		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Clear any strong references to this Activity, we'll reattach to
		// handle events on the other side.
		mState.mReceiver.clearReceiver();
		return mState;
	}

	private void updateRefreshStatus() {
		if (mState.mSyncing)
			mProgressBar.setVisibility(View.VISIBLE);
		else
			mProgressBar.setVisibility(View.GONE);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		startActivity(new Intent(this, NewSettingsActivity.class));
		return super.onPrepareOptionsMenu(menu);
	}

	public void onListClick(View v) {
		Intent intent = new Intent(this, POIsListActivity.class);
		// intent.putExtra(POIsMapsforgeActivity.EXTRA_NO_RETRIEVAL, false);
		intent.putExtra(POIsListActivity.EXTRA_IS_RECREATED, false);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(intent);
		overridePendingTransition(0, 0);

	}

	public void onCenterClick(View v) {
		if (mLastGeoPointE6 != null) {
			mMapController.setCenter(mLastGeoPointE6);
			requestUpdate();
		}
	}

	private void fillExtrasWithBoundingRect(Bundle bundle) {
		int latSpan = (int) (mMapView.getLatitudeSpan() * SPAN_ENLARGEMENT_FAKTOR);
		int lonSpan = (int) (mMapView.getLongitudeSpan() * SPAN_ENLARGEMENT_FAKTOR);
		GeoPoint center = mMapView.getMapCenter();
		mMapView.setLastRequestedLocation(center);
		ParceableBoundingBox boundingBox = new ParceableBoundingBox(
				center.getLatitudeE6() + (latSpan / 2), center.getLongitudeE6()
						+ (lonSpan / 2),
				center.getLatitudeE6() - (latSpan / 2), center.getLongitudeE6()
						- (lonSpan / 2));
		bundle.putSerializable(SyncService.EXTRA_BOUNDING_BOX, boundingBox);
	}

	private void requestUpdate() {
		Bundle extras = new Bundle();
		//
		fillExtrasWithBoundingRect(extras);

		// trigger off background sync
		final Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
				SyncService.class);
		intent.putExtras(extras);
		intent.putExtra(SyncService.EXTRA_WHAT, SyncService.WHAT_RETRIEVE_NODES);
		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mState.mReceiver);
		startService(intent);

	}

	public void onInfoClick(View v) {
		Intent intent = new Intent(this, InfoActivity.class);
		startActivity(intent);
	}

	@Override
	public void onMapViewTouchMoveEnough() {
		if (mMapView.getZoomLevel() >= ZOOMLEVEL_MIN) {
			requestUpdate();
		}
	}

	/**
	 * State specific to {@link HomeActivity} that is held between configuration
	 * changes. Any strong {@link Activity} references <strong>must</strong> be
	 * cleared before {@link #onRetainNonConfigurationInstance()}, and this
	 * class should remain {@code static class}.
	 */
	private static class State {
		public DetachableResultReceiver mReceiver;
		public boolean mSyncing = false;

		private State() {
			mReceiver = new DetachableResultReceiver(new Handler());
		}
	}

	private void showErrorDialog(SyncServiceException e) {
		if (!isInForeground)
			return;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (e.getErrorCode() == SyncServiceException.ERROR_NETWORK_FAILURE)
			builder.setTitle(R.string.error_network_title);
		else
			builder.setTitle(R.string.error_occurred);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setMessage(e.getRessourceString());
		builder.setNeutralButton(R.string.okay,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private GeoPoint calcGeoPoint(Location location) {
		int lat = (int) (location.getLatitude() * 1E6);
		int lng = (int) (location.getLongitude() * 1E6);
		return new GeoPoint(lat, lng);
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
