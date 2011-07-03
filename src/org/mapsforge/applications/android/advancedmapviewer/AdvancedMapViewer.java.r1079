/*
 * Copyright 2010, 2011 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.applications.android.advancedmapviewer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import org.mapsforge.android.maps.ArrayCircleOverlay;
import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapController;
import org.mapsforge.android.maps.MapDatabase;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.MapViewMode;
import org.mapsforge.android.maps.OverlayCircle;
import org.mapsforge.android.maps.MapView.TextField;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A map application which uses the features from the mapsforge library. The map can be centered
 * to the current GPS coordinate. A simple file browser for selecting the map file is also
 * included. Some preferences can be adjusted via the EditPreferences activity and screenshots
 * of the map may be taken in different image formats.
 */
public class AdvancedMapViewer extends MapActivity {
	private static final int DIALOG_ENTER_COORDINATES = 0;
	private static final int DIALOG_GPS_DISABLED = 1;
	private static final int DIALOG_INFO_MAP_FILE = 2;
	private static final String SCREENSHOT_DIRECTORY = "Pictures";
	private static final String SCREENSHOT_FILE_NAME = "Map screenshot";
	private static final int SCREENSHOT_QUALITY = 90;
	private static final int SELECT_MAP_FILE = 0;

	/**
	 * The default size of the memory card cache.
	 */
	static final short MEMORY_CARD_CACHE_SIZE_DEFAULT = 250;

	/**
	 * The maximum size of the memory card cache.
	 */
	static final short MEMORY_CARD_CACHE_SIZE_MAX = 500;

	/**
	 * The default move speed of the map.
	 */
	static final int MOVE_SPEED_DEFAULT = 10;

	/**
	 * The maximum move speed of the map.
	 */
	static final int MOVE_SPEED_MAX = 30;

	private ArrayCircleOverlay circleOverlay;
	private Paint circleOverlayFill;
	private Paint circleOverlayOutline;
	private boolean followGpsEnabled;
	private LocationListener locationListener;
	private LocationManager locationManager;
	private MapViewMode mapViewMode;
	private PowerManager powerManager;
	private SharedPreferences preferences;
	private Toast toast;
	private WakeLock wakeLock;
	ImageView gpsView;
	MapController mapController;
	MapView mapView;
	OverlayCircle overlayCircle;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// insert code here to handle touch events on the screen
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_info:
				return true;

			case R.id.menu_info_map_file:
				showDialog(DIALOG_INFO_MAP_FILE);
				return true;

			case R.id.menu_info_about:
				startActivity(new Intent(this, InfoView.class));
				return true;

			case R.id.menu_position:
				return true;

			case R.id.menu_position_gps_follow:
				if (this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					enableFollowGPS();
				} else {
					showDialog(DIALOG_GPS_DISABLED);
				}
				return true;

			case R.id.menu_position_enter_coordinates:
				showDialog(DIALOG_ENTER_COORDINATES);
				return true;

			case R.id.menu_position_map_center:
				// disable GPS follow mode if it is enabled
				disableFollowGPS(true);
				this.mapController.setCenter(this.mapView.getMapDatabase().getMapCenter());
				return true;

			case R.id.menu_screenshot:
				return true;

			case R.id.menu_screenshot_jpeg:
				captureScreenshotAsync(CompressFormat.JPEG);
				return true;

			case R.id.menu_screenshot_png:
				captureScreenshotAsync(CompressFormat.PNG);
				return true;

			case R.id.menu_preferences:
				startActivity(new Intent(this, EditPreferences.class));
				return true;

			case R.id.menu_mapfile:
				startFileBrowser();
				return true;

			default:
				return false;
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (this.mapView.getMapViewMode().requiresInternetConnection()) {
			menu.findItem(R.id.menu_info_map_file).setEnabled(false);
		} else {
			menu.findItem(R.id.menu_info_map_file).setEnabled(true);
		}

		if (this.locationManager == null || this.followGpsEnabled) {
			menu.findItem(R.id.menu_position_gps_follow).setEnabled(false);
		} else {
			menu.findItem(R.id.menu_position_gps_follow).setEnabled(true);
		}

		if (this.mapView.getMapViewMode().requiresInternetConnection()) {
			menu.findItem(R.id.menu_position_map_center).setEnabled(false);
		} else {
			menu.findItem(R.id.menu_position_map_center).setEnabled(true);
		}

		if (this.mapView.getMapViewMode().requiresInternetConnection()) {
			menu.findItem(R.id.menu_mapfile).setEnabled(false);
		} else {
			menu.findItem(R.id.menu_mapfile).setEnabled(true);
		}

		return true;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		// forward the event to the MapView
		return this.mapView.onTrackballEvent(event);
	}

	private void captureScreenshotAsync(final CompressFormat format) {
		new Thread() {
			@Override
			public void run() {
				try {
					File path = new File(Environment.getExternalStorageDirectory(),
							SCREENSHOT_DIRECTORY);
					// make sure the Pictures directory exists
					if (!path.exists() && !path.mkdirs()) {
						showToastOnUiThread("Could not create target directory");
						return;
					}

					// assemble the complete name for the screenshot file
					String fileName = path.getAbsolutePath() + File.separatorChar
							+ SCREENSHOT_FILE_NAME + "." + format.name().toLowerCase();

					if (AdvancedMapViewer.this.mapView.makeScreenshot(format,
							SCREENSHOT_QUALITY, fileName)) {
						// success
						showToastOnUiThread(fileName);
					} else {
						// failure
						showToastOnUiThread("Screenshot could not be saved");
					}
				} catch (IOException e) {
					showToastOnUiThread(e.getLocalizedMessage());
				}
			}

			private void showToastOnUiThread(final String message) {
				AdvancedMapViewer.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showToast(message);
					}
				});
			}
		}.start();
	}

	private void configureMapView() {
		// configure the MapView and activate the zoomLevel buttons
		this.mapView.setClickable(true);
		this.mapView.setBuiltInZoomControls(true);
		this.mapView.setFocusable(true);

		// set the localized text fields
		this.mapView.setText(TextField.KILOMETER, getString(R.string.unit_symbol_kilometer));
		this.mapView.setText(TextField.METER, getString(R.string.unit_symbol_meter));

		// get the map controller for this MapView
		this.mapController = this.mapView.getController();
	}

	private void enableFollowGPS() {
		this.circleOverlay = new ArrayCircleOverlay(this.circleOverlayFill,
				this.circleOverlayOutline, this);
		this.overlayCircle = new OverlayCircle();
		this.circleOverlay.addCircle(this.overlayCircle);
		this.mapView.getOverlays().add(this.circleOverlay);

		this.followGpsEnabled = true;
		this.locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
				AdvancedMapViewer.this.mapController.setCenter(point);
				AdvancedMapViewer.this.gpsView.setImageResource(R.drawable.stat_sys_gps_on);
				AdvancedMapViewer.this.overlayCircle.setCircleData(point, location
						.getAccuracy());
			}

			@Override
			public void onProviderDisabled(String provider) {
				disableFollowGPS(false);
				showDialog(DIALOG_GPS_DISABLED);
			}

			@Override
			public void onProviderEnabled(String provider) {
				// do nothing
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				if (status == LocationProvider.AVAILABLE) {
					AdvancedMapViewer.this.gpsView.setImageResource(R.drawable.stat_sys_gps_on);
				} else if (status == LocationProvider.OUT_OF_SERVICE) {
					AdvancedMapViewer.this.gpsView
							.setImageResource(R.drawable.stat_sys_gps_acquiring);
				} else {
					// must be TEMPORARILY_UNAVAILABLE
					AdvancedMapViewer.this.gpsView.setImageResource(R.anim.gps_animation);
					((AnimationDrawable) AdvancedMapViewer.this.gpsView.getDrawable()).start();
				}
			}
		};

		this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,
				this.locationListener);
		this.gpsView.setImageResource(R.drawable.stat_sys_gps_acquiring);
		this.gpsView.setVisibility(View.VISIBLE);
		this.gpsView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableFollowGPS(true);
			}
		});
	}

	/**
	 * Sets all file filters and starts the FilePicker.
	 */
	private void startFileBrowser() {
		// set the FileDisplayFilter
		FilePicker.setFileDisplayFilter(new FileFilter() {
			@Override
			public boolean accept(File file) {
				// accept only readable files
				if (file.canRead()) {
					if (file.isDirectory()) {
						// accept all directories
						return true;
					} else if (file.isFile() && file.getName().endsWith(".map")) {
						// accept all files with a ".map" extension
						return true;
					}
				}
				return false;
			}
		});

		// set the FileSelectFilter
		FilePicker.setFileSelectFilter(new FileFilter() {
			@Override
			public boolean accept(File file) {
				// accept only valid map files
				return MapView.isValidMapFile(file.getAbsolutePath());
			}
		});

		// start the FilePicker
		startActivityForResult(new Intent(this, FilePicker.class), SELECT_MAP_FILE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_MAP_FILE) {
			if (resultCode == RESULT_OK) {
				disableFollowGPS(true);
				if (data != null && data.getStringExtra("selectedFile") != null) {
					this.mapView.setMapFile(data.getStringExtra("selectedFile"));
				}
			} else if (resultCode == RESULT_CANCELED
					&& !this.mapView.getMapViewMode().requiresInternetConnection()
					&& !this.mapView.hasValidMapFile()) {
				finish();
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set up the layout views
		setContentView(R.layout.activity_advanced_map_viewer);
		this.mapView = (MapView) findViewById(R.id.mapView);
		this.gpsView = (ImageView) findViewById(R.id.gpsView);

		configureMapView();

		// get the pointers to different system services
		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		this.powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.wakeLock = this.powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "AMV");

		// set up the paint objects for the location overlay
		this.circleOverlayFill = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.circleOverlayFill.setStyle(Paint.Style.FILL);
		this.circleOverlayFill.setColor(Color.BLUE);
		this.circleOverlayFill.setAlpha(64);

		this.circleOverlayOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.circleOverlayOutline.setStyle(Paint.Style.STROKE);
		this.circleOverlayOutline.setColor(Color.BLUE);
		this.circleOverlayOutline.setAlpha(128);
		this.circleOverlayOutline.setStrokeWidth(3);

		if (savedInstanceState != null && savedInstanceState.getBoolean("locationListener")) {
			if (this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				enableFollowGPS();
			} else {
				showDialog(DIALOG_GPS_DISABLED);
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (id == DIALOG_ENTER_COORDINATES) {
			builder.setIcon(android.R.drawable.ic_menu_mylocation);
			builder.setTitle(R.string.menu_position_enter_coordinates);
			LayoutInflater factory = LayoutInflater.from(this);
			final View view = factory.inflate(R.layout.dialog_enter_coordinates, null);
			builder.setView(view);
			builder.setPositiveButton(R.string.go_to_position,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// disable GPS follow mode if it is enabled
							disableFollowGPS(true);

							// set the map center and zoom level
							AdvancedMapViewer.this.mapController.setCenter(new GeoPoint(Double
									.parseDouble(((EditText) view.findViewById(R.id.latitude))
											.getText().toString()), Double
									.parseDouble(((EditText) view.findViewById(R.id.longitude))
											.getText().toString())));
							AdvancedMapViewer.this.mapController.setZoom(((SeekBar) view
									.findViewById(R.id.zoomlevel)).getProgress());
						}
					});
			builder.setNegativeButton(R.string.cancel, null);
			return builder.create();
		} else if (id == DIALOG_GPS_DISABLED) {
			builder.setIcon(android.R.drawable.ic_menu_info_details);
			builder.setTitle(R.string.error);
			builder.setMessage(R.string.gps_disabled);
			builder.setPositiveButton(R.string.ok, null);
			return builder.create();
		} else if (id == DIALOG_INFO_MAP_FILE) {
			builder.setIcon(android.R.drawable.ic_menu_info_details);
			builder.setTitle(R.string.menu_info_map_file);
			LayoutInflater factory = LayoutInflater.from(this);
			builder.setView(factory.inflate(R.layout.dialog_info_map_file, null));
			builder.setPositiveButton(R.string.ok, null);
			return builder.create();
		} else {
			// do dialog will be created
			return null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// remove the toast messageText if visible
		if (this.toast != null) {
			this.toast.cancel();
			this.toast = null;
		}

		// switch off location updates
		if (this.locationManager != null) {
			if (this.locationListener != null) {
				this.locationManager.removeUpdates(this.locationListener);
				this.locationListener = null;
			}
			this.locationManager = null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// release the wake lock if necessary
		if (this.wakeLock.isHeld()) {
			this.wakeLock.release();
		}
	}

	@Override
	protected void onPrepareDialog(int id, final Dialog dialog) {
		if (id == DIALOG_ENTER_COORDINATES) {
			// latitude
			EditText editText = (EditText) dialog.findViewById(R.id.latitude);
			GeoPoint mapCenter = this.mapView.getMapCenter();
			editText.setText(Double.toString(mapCenter.getLatitude()));

			// longitude
			editText = (EditText) dialog.findViewById(R.id.longitude);
			editText.setText(Double.toString(mapCenter.getLongitude()));

			// zoom level
			SeekBar zoomlevel = (SeekBar) dialog.findViewById(R.id.zoomlevel);
			zoomlevel.setMax(this.mapView.getMaxZoomLevel());
			zoomlevel.setProgress(this.mapView.getZoomLevel());

			// zoom level value
			final TextView textView = (TextView) dialog.findViewById(R.id.zoomlevelValue);
			textView.setText(String.valueOf(zoomlevel.getProgress()));
			zoomlevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					textView.setText(String.valueOf(progress));
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// do nothing
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// do nothing
				}
			});
		} else if (id == DIALOG_INFO_MAP_FILE) {
			// map file name
			TextView textView = (TextView) dialog.findViewById(R.id.infoMapFileViewName);
			textView.setText(this.mapView.getMapFile());

			// map file name
			textView = (TextView) dialog.findViewById(R.id.infoMapFileViewDebug);
			MapDatabase mapDatabase = this.mapView.getMapDatabase();
			if (mapDatabase.isDebugFile()) {
				textView.setText(R.string.info_map_file_debug_yes);
			} else {
				textView.setText(R.string.info_map_file_debug_no);
			}

			// map file date
			textView = (TextView) dialog.findViewById(R.id.infoMapFileViewDate);
			Date date = new Date(mapDatabase.getMapDate());
			textView.setText(DateFormat.getDateTimeInstance().format(date));

			// map file area
			textView = (TextView) dialog.findViewById(R.id.infoMapFileViewArea);
			Rect mapArea = mapDatabase.getMapBoundary();
			textView.setText(mapArea.top / 1000000d + ", " + mapArea.left / 1000000d + " – \n"
					+ mapArea.bottom / 1000000d + ", " + mapArea.right / 1000000d);

			// map file start position
			textView = (TextView) dialog.findViewById(R.id.infoMapFileViewStart);
			GeoPoint startPosition = mapDatabase.getStartPosition();
			if (startPosition == null) {
				textView.setText(null);
			} else {
				textView.setText(startPosition.getLatitude() + ", "
						+ startPosition.getLongitude());
			}

			// map file comment text
			textView = (TextView) dialog.findViewById(R.id.infoMapFileViewComment);
			String commentText = mapDatabase.getCommentText();
			if (commentText == null) {
				textView.setText(null);
			} else {
				textView.setText(mapDatabase.getCommentText());
			}
		} else {
			super.onPrepareDialog(id, dialog);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Read the default shared preferences
		this.preferences = PreferenceManager.getDefaultSharedPreferences(this);

		// set the map settings
		this.mapView.setScaleBar(this.preferences.getBoolean("showScaleBar", false));
		if (this.preferences.contains("mapViewMode")) {
			this.mapViewMode = Enum.valueOf(MapViewMode.class, this.preferences.getString(
					"mapViewMode", MapView.getDefaultMapViewMode().name()));
			this.mapView.setMapViewMode(this.mapViewMode);
		}
		try {
			this.mapView.setTextScale(Float.parseFloat(this.preferences.getString("textScale",
					"1")));
		} catch (NumberFormatException e) {
			this.mapView.setTextScale(1);
		}

		// set the general settings
		if (this.preferences.getBoolean("fullscreen", false)) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		}
		if (this.preferences.getBoolean("wakeLock", false)) {
			if (!this.wakeLock.isHeld()) {
				this.wakeLock.acquire();
			}
		}
		this.mapView.setMemoryCardCachePersistence(this.preferences.getBoolean(
				"cachePersistence", false));
		this.mapView.setMemoryCardCacheSize(Math.min(this.preferences.getInt("cacheSize",
				MEMORY_CARD_CACHE_SIZE_DEFAULT), MEMORY_CARD_CACHE_SIZE_MAX));
		this.mapView.setMoveSpeed(Math.min(this.preferences.getInt("moveSpeed",
				MOVE_SPEED_DEFAULT), MOVE_SPEED_MAX) / 10f);

		// set the debug settings
		this.mapView.setFpsCounter(this.preferences.getBoolean("showFpsCounter", false));
		this.mapView.setTileFrames(this.preferences.getBoolean("showTileFrames", false));
		this.mapView.setTileCoordinates(this.preferences.getBoolean("showTileCoordinates",
				false));
		this.mapView.setWaterTiles(this.preferences.getBoolean("showWaterTiles", false));

		// check if the file browser needs to be displayed
		if (!this.mapView.getMapViewMode().requiresInternetConnection()
				&& !this.mapView.hasValidMapFile()) {
			startFileBrowser();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("locationListener", this.locationListener != null);
	}

	/**
	 * Disables the "Follow GPS mode" and removes the GPS icon.
	 * 
	 * @param showToastMessage
	 *            if a toast message should be displayed or not.
	 */
	void disableFollowGPS(boolean showToastMessage) {
		if (this.circleOverlay != null) {
			this.mapView.getOverlays().remove(this.circleOverlay);
			this.circleOverlay = null;
		}
		if (this.followGpsEnabled) {
			if (this.locationListener != null) {
				this.locationManager.removeUpdates(this.locationListener);
				this.locationListener = null;
			}
			this.gpsView.setVisibility(View.GONE);
			if (showToastMessage) {
				showToast(getString(R.string.follow_gps_disabled));
			}
			this.followGpsEnabled = false;
		}
	}

	/**
	 * Displays a text message via the toast notification system. If a previous message is still
	 * visible, the previous message is first removed.
	 * 
	 * @param text
	 *            the text message to display
	 */
	void showToast(String text) {
		if (this.toast == null) {
			this.toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
		} else {
			this.toast.cancel();
			this.toast.setText(text);
		}
		this.toast.show();
	}
}