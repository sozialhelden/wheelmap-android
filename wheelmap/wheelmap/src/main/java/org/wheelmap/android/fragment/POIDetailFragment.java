package org.wheelmap.android.fragment;

import java.util.Map;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapController;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.app.WheelmapApp.Capability;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.manager.SupportManager.NodeType;
import org.wheelmap.android.manager.SupportManager.WheelchairAttributes;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.online.R;
import org.wheelmap.android.overlays.OnTapListener;
import org.wheelmap.android.overlays.SingleItemOverlay;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.ui.mapsforge.ConfigureMapView;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.ViewTool;

import roboguice.inject.InjectView;
import wheelmap.org.WheelchairState;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;

import de.akquinet.android.androlog.Log;

public class POIDetailFragment extends RoboSherlockFragment implements
		OnClickListener, OnTapListener, LoaderCallbacks<Cursor>,
		DetachableResultReceiver.Receiver {

	public final static String TAG = "poidetail";

	private final static String EXTRA_WM_ID = "org.wheelmap.android.WM_ID";
	private final static String EXTRA_POI_ID = "org.wheelmap.android.POI_ID";
	private final static int LOADER_CONTENT = 0;

	@InjectView(R.id.title_name)
	TextView nameText;
	@InjectView(R.id.title_category)
	TextView categoryText;
	@InjectView(R.id.nodetype)
	TextView nodetypeText;
	@InjectView(R.id.phone)
	TextView phoneText;
	@InjectView(R.id.addr)
	TextView addressText;
	@InjectView(R.id.comment)
	TextView commentText;
	@InjectView(R.id.website)
	TextView websiteText;
	@InjectView(R.id.wheelchair_state_icon)
	ImageView stateIcon;
	@InjectView(R.id.wheelchair_state_text)
	TextView stateText;
	@InjectView(R.id.wheelchair_state_layout)
	ViewGroup stateLayout;

	private Button mMapButton;
	private Capability mCap;

	private SupportManager mSupportManager;
	private Map<WheelchairState, WheelchairAttributes> mWSAttributes;

	private WheelchairState mWheelChairState;

	public interface OnPOIDetailListener {
		void onEdit(long poiId);

		void onEditWheelchairState(WheelchairState wState);

		void onShowLargeMapAt(int lat, int lon);
	}

	private OnPOIDetailListener mListener;
	private MapView mapView;
	private MapController mapController;

	private long poiID;
	private long wmID;

	private DetachableResultReceiver mReceiver;

	public static POIDetailFragment newInstanceWithWMID(Long wmID) {
		Bundle b = new Bundle();
		b.putLong(EXTRA_WM_ID, wmID);
		POIDetailFragment f = new POIDetailFragment();
		f.setArguments(b);
		return f;
	}

	public static POIDetailFragment newInstanceWithPOIID(Long poiID) {
		Bundle b = new Bundle();
		b.putLong(EXTRA_POI_ID, poiID);
		POIDetailFragment f = new POIDetailFragment();
		f.setArguments(b);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof OnPOIDetailListener)
			mListener = (OnPOIDetailListener) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			wmID = getArguments().getLong(EXTRA_WM_ID, -1);
			if (wmID != -1) {
				requestData(wmID);
			}
		}

		setHasOptionsMenu(true);

		mCap = WheelmapApp.getCapabilityLevel();
		mSupportManager = WheelmapApp.getSupportManager();
		mWSAttributes = mSupportManager.wsAttributes;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_detail, container, false);

		int stubId;
		if (mCap == Capability.DEGRADED_MAX)
			stubId = R.id.stub_button;
		else
			stubId = R.id.stub_map;

		ViewStub stub = (ViewStub) v.findViewById(stubId);
		stub.inflate();

		if (mCap == Capability.DEGRADED_MAX)
			assignButton(v);
		else
			assignMapView(v);

		return v;

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		stateLayout.setOnClickListener(this);
	}

	private void assignMapView(View v) {
		mapView = (MapView) v.findViewById(R.id.map);

		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);
		ConfigureMapView.pickAppropriateMap(getActivity(), mapView);
		mapController = mapView.getController();
		mapController.setZoom(18);
	}

	private void assignButton(View v) {
		mMapButton = (Button) v.findViewById(R.id.btn_map);
		mMapButton.setOnClickListener(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(LOADER_CONTENT, getArguments(), this);

	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		ViewTool.logMemory();
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
		mSupportManager.cleanReferences();
		ViewTool.nullViewDrawables(getView());
		mapView = null;
		mapController = null;
		System.gc();
		System.gc(); // to be sure ;-)
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.ab_detail_fragment, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
		case R.id.menu_edit:
			if (mListener != null)
				mListener.onEdit(poiID);
			return true;
		case R.id.menu_share:
			shareItem();
			return true;
		case R.id.menu_directions:
			directionsItem();
			return true;
		default:
			// noop
		}

		return false;
	}

	public WheelchairState getWheelchairState() {
		return mWheelChairState;
	}

	public long getPoiId() {
		return poiID;
	}

	private void requestData(Long wmID) {
		mReceiver = new DetachableResultReceiver(new Handler());
		mReceiver.setReceiver(this);

		final Intent intent = new Intent(Intent.ACTION_SYNC, null,
				getActivity(), SyncService.class);
		intent.putExtra(SyncService.EXTRA_WHAT, SyncService.WHAT_RETRIEVE_NODE);
		intent.putExtra(SyncService.EXTRA_WHEELMAP_ID, wmID);
		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
		getActivity().startService(intent);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		switch (id) {
		case R.id.wheelchair_state_layout:
			if (mListener != null) {
				mListener.onEditWheelchairState(mWheelChairState);
				return;
			}
			break;
		default:
			//
		}

	}

	/** {@inheritDoc} */
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Log.d(TAG, "onReceiveResult in list resultCode = " + resultCode);
		switch (resultCode) {
		case SyncService.STATUS_RUNNING: {
			break;
		}
		case SyncService.STATUS_FINISHED: {
			break;
		}
		case SyncService.STATUS_ERROR: {
			break;
		}
		default: {
			// noop
		}
		}
	}

	@Override
	public void onTap(OverlayItem item, long poiId) {
		int lat = item.getPoint().getLatitudeE6();
		int lon = item.getPoint().getLongitudeE6();

		if (mListener != null) {
			mListener.onShowLargeMapAt(lat, lon);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arguments) {
		if (arguments != null) {
			poiID = arguments.getLong(EXTRA_POI_ID, -1);
			wmID = arguments.getLong(EXTRA_WM_ID, -1);
		}

		String whereClause = null;
		String whereValues[] = null;

		Uri uri = null;
		if (poiID != -1l) {
			uri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI_POI_ID,
					String.valueOf(poiID));
			whereValues = null;
		} else if (wmID != -1l) {
			whereClause = "( " + POIs.WM_ID + " = ? )";

			whereValues = new String[] { String.valueOf(wmID) };
			uri = Wheelmap.POIs.CONTENT_URI;
		}

		return new CursorLoader(getActivity(), uri, null, whereClause,
				whereValues, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		load(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {

	}

	private void load(Cursor c) {
		if (c == null)
			return;

		if (c.getCount() < 1)
			return;

		c.moveToFirst();
		poiID = POIHelper.getId(c);
		WheelchairState state = POIHelper.getWheelchair(c);
		String name = POIHelper.getName(c);
		String comment = POIHelper.getComment(c);
		final int lat = (int) (POIHelper.getLatitude(c) * 1E6);
		final int lon = (int) (POIHelper.getLongitude(c) * 1E6);
		int nodeTypeId = POIHelper.getNodeTypeId(c);
		int categoryId = POIHelper.getCategoryId(c);

		NodeType nodeType = mSupportManager.lookupNodeType(nodeTypeId);
		// iconImage.setImageDrawable(nodeType.iconDrawable);

		setWheelchairState(state);
		nameText.setText(name);
		String category = mSupportManager.lookupCategory(categoryId).localizedName;
		categoryText.setText(category);
		nodetypeText.setText(nodeType.localizedName);
		commentText.setText(comment);
		addressText.setText(POIHelper.getAddress(c));
		websiteText.setText(POIHelper.getWebsite(c));
		phoneText.setText(POIHelper.getPhone(c));

		if (mCap == Capability.DEGRADED_MAX) {
			mMapButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mListener != null)
						mListener.onShowLargeMapAt(lat, lon);

				}
			});
		} else {
			SingleItemOverlay overlay = new SingleItemOverlay(this);
			overlay.setItem(name, comment, nodeType, state, lat, lon);
			overlay.enableLowDrawQuality(true);
			mapView.getOverlays().clear();
			mapView.getOverlays().add(overlay);
			mapController.setCenter(new GeoPoint(lat, lon));
		}
	}

	private void setWheelchairState(WheelchairState newState) {
		mWheelChairState = newState;
		stateIcon.setImageDrawable(mSupportManager.lookupWheelDrawable(newState
				.getId()));
		stateText.setTextColor(mWSAttributes.get(newState).colorId);
		stateText.setText(mWSAttributes.get(newState).titleStringId);
	}

	private void directionsItem() {

		Uri poiUri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI_POI_ID,
				String.valueOf(poiID));

		// Then query for this specific record:
		Cursor cur = getActivity().getContentResolver().query(poiUri, null,
				null, null, null);
		if (cur == null)
			return;

		if (cur.getCount() < 1) {
			cur.close();
			return;
		}

		cur.moveToFirst();
		String name = POIHelper.getName(cur);
		double lat = POIHelper.getLatitude(cur);
		double lon = POIHelper.getLongitude(cur);
		String street = POIHelper.getStreet(cur);
		String houseNum = POIHelper.getHouseNumber(cur);
		String postCode = POIHelper.getPostcode(cur);
		String city = POIHelper.getCity(cur);
		cur.close();

		Uri geoURI;
		if (street.length() > 0 && (postCode.length() > 0 || city.length() > 0)) {
			String address = street + "+" + houseNum + "+" + postCode + "+"
					+ city;
			geoURI = Uri.parse("geo:0,0?q=" + address.replace(" ", "+"));
		} else {
			geoURI = Uri.parse("geo:" + String.valueOf(lat) + ","
					+ String.valueOf(lon) + "?z=17");
		}

		Log.d(TAG, "geoURI = " + geoURI.toString());

		Intent sharingIntent = new Intent(Intent.ACTION_VIEW);
		sharingIntent.setData(geoURI);
		startActivity(Intent.createChooser(sharingIntent, getResources()
				.getString(R.string.title_view_using)));
	}

	private void shareItem() {

		Uri poiUri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI_POI_ID,
				String.valueOf(poiID));

		// Then query for this specific record:
		Cursor cur = getActivity().getContentResolver().query(poiUri, null,
				null, null, null);
		if (cur == null)
			return;

		if (cur.getCount() < 1) {
			cur.close();
			return;
		}

		cur.moveToFirst();
		String wmId = POIHelper.getWMId(cur);
		String name = POIHelper.getName(cur);
		String comment = POIHelper.getComment(cur);
		String address = POIHelper.getAddress(cur);
		String website = POIHelper.getWebsite(cur);
		cur.close();

		StringBuilder sb = new StringBuilder(name);

		if (comment.length() > 0) {
			sb.append(", ");
			sb.append(comment);
		}

		if (address.length() > 0) {
			sb.append(", ");
			sb.append(address);
		}

		if (website.length() > 0) {
			sb.append(", ");
			sb.append(website);
		}

		sb.append(", ");
		sb.append("http://wheelmap.org/nodes/" + String.valueOf(wmId));

		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent
				.putExtra(android.content.Intent.EXTRA_TEXT, sb.toString());
		startActivity(Intent.createChooser(sharingIntent, getResources()
				.getString(R.string.title_share_using)));
	}

}
