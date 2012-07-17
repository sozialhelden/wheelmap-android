package org.wheelmap.android.fragment;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapController;
import org.mapsforge.android.maps.MapView;
import org.wheelmap.android.online.R;
import org.wheelmap.android.overlays.POILocationEditableOverlay;
import org.wheelmap.android.ui.mapsforge.ConfigureMapView;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class EditPositionFragment extends SherlockFragment implements
		OnTouchListener {
	public static final String TAG = EditPositionFragment.class.getSimpleName();
	public final static String ARGUMENT_LATITUDE = "org.wheelmap.android.ui.mapsforge.LATITUDE";
	public final static String ARGUMENT_LONGITUDE = "org.wheelmap.android.ui.mapsforge.LONGITUDE";

	private MapController mMapController;
	private MapView mMapView;
	private POILocationEditableOverlay mMapOverlay;

	private int mCrrLatitude;
	private int mCrrLongitude;

	private OnEditPositionListener mListener;

	public interface OnEditPositionListener {
		public void onEditPosition(int latitude, int longitude);
	}

	public static EditPositionFragment newInstance(int latitude, int longitude) {
		Bundle b = new Bundle();
		b.putInt(ARGUMENT_LATITUDE, latitude);
		b.putInt(ARGUMENT_LONGITUDE, longitude);

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

		mCrrLatitude = getArguments().getInt(ARGUMENT_LATITUDE);
		mCrrLongitude = getArguments().getInt(ARGUMENT_LONGITUDE);
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
						R.drawable.location_pin_wm_holo_light));
		mMapOverlay.enableLowDrawQuality(true);
		mMapOverlay.enableUseOnlyOneBitmap(true);
		mMapView.getOverlays().add(mMapOverlay);
		mMapView.setOnTouchListener(this);

		return v;
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
			if (mListener != null)
				mListener.onEditPosition(mCrrLatitude, mCrrLongitude);
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
					(int) event.getX(), (int) event.getY());
			mCrrLatitude = geoPoint.getLatitudeE6();
			mCrrLongitude = geoPoint.getLongitudeE6();
			mMapOverlay.setPosition(geoPoint);
			return true;
		}
		return false;
	}

}
