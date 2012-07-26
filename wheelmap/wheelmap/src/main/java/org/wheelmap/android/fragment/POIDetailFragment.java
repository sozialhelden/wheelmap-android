package org.wheelmap.android.fragment;

import java.util.HashMap;
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
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.online.R;
import org.wheelmap.android.overlays.ConfigureMapView;
import org.wheelmap.android.overlays.OnTapListener;
import org.wheelmap.android.overlays.SingleItemOverlay;
import org.wheelmap.android.utils.ViewTool;

import roboguice.inject.InjectView;
import wheelmap.org.WheelchairState;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;

import de.akquinet.android.androlog.Log;

public class POIDetailFragment extends RoboSherlockFragment implements
		OnClickListener, OnTapListener, LoaderCallbacks<Cursor> {

	public final static String TAG = POIDetailFragment.class.getSimpleName();
	private final static int LOADER_CONTENT = 0;

	@InjectView(R.id.title_container)
	private RelativeLayout title_container;
	@InjectView(R.id.titlebar_title)
	private TextView nameText;
	@InjectView(R.id.titlebar_subtitle)
	private TextView categoryText;
	@InjectView(R.id.titlebar_icon)
	private ImageView nodetypeIcon;
	@InjectView(R.id.nodetype)
	private TextView nodetypeText;
	@InjectView(R.id.phone)
	private TextView phoneText;
	@InjectView(R.id.addr)
	private TextView addressText;
	@InjectView(R.id.comment)
	private TextView commentText;
	@InjectView(R.id.website)
	private TextView websiteText;
	@InjectView(R.id.state_icon)
	private ImageView stateIcon;
	@InjectView(R.id.state_text)
	private TextView stateText;
	@InjectView(R.id.wheelchair_state_layout)
	private ViewGroup stateLayout;

	private Button mMapButton;
	private Capability mCap;

	private Map<WheelchairState, WheelchairAttributes> mWSAttributes;
	private WheelchairState mWheelchairState;

	public interface OnPOIDetailListener {
		void onEdit(long poiId);

		void onEditWheelchairState(WheelchairState wState);

		void onShowLargeMapAt(GeoPoint point);
	}

	private OnPOIDetailListener mListener;
	private MapView mapView;
	private MapController mapController;

	private long poiId;

	private final static int ACTION_PROVIDER_DIRECTIONS = 0;
	private final static int ACTION_PROVIDER_SHARE = 1;
	private ShareActionProvider mShareActionProvider;
	private ShareActionProvider mDirectionsActionProvider;

	private final static Map<Integer, Intent> intentSaved = new HashMap<Integer, Intent>();

	public static POIDetailFragment newInstance(long id) {
		if (id == Extra.ID_UNKNOWN)
			return null;

		Bundle bundle = new Bundle();
		bundle.putLong(Extra.POI_ID, id);
		POIDetailFragment f = new POIDetailFragment();
		f.setArguments(bundle);
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

		setHasOptionsMenu(true);

		mCap = WheelmapApp.getCapabilityLevel();
		mWSAttributes = SupportManager.wsAttributes;
		poiId = getArguments().getLong(Extra.POI_ID, Extra.ID_UNKNOWN);
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
		WheelmapApp.getSupportManager().cleanReferences();
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
		Log.d(TAG, "onCreateOptionsMenu");
		inflater.inflate(R.menu.ab_detail_fragment, menu);
		createShareActionProvider(menu);
	}

	private void createShareActionProvider(Menu menu) {
		MenuItem menuItemShare = menu.findItem(R.id.menu_share);
		mShareActionProvider = (ShareActionProvider) menuItemShare
				.getActionProvider();
		mShareActionProvider
				.setShareHistoryFileName("ab_provider_share_history.xml");
		setIntentOnActionProvider(ACTION_PROVIDER_SHARE, mShareActionProvider);
		MenuItem menuItemDirection = menu.findItem(R.id.menu_directions);
		mDirectionsActionProvider = (ShareActionProvider) menuItemDirection
				.getActionProvider();
		mDirectionsActionProvider
				.setShareHistoryFileName("ab_provider_directions_history.xml");
		setIntentOnActionProvider(ACTION_PROVIDER_DIRECTIONS,
				mDirectionsActionProvider);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
		case R.id.menu_edit:
			if (mListener != null)
				mListener.onEdit(poiId);
			return true;
		default:
			// noop
		}

		return false;
	}

	public long getPoiId() {
		return poiId;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		switch (id) {
		case R.id.wheelchair_state_layout:
			if (mListener != null) {
				mListener.onEditWheelchairState(mWheelchairState);
				return;
			}
			break;
		default:
			//
		}

	}

	@Override
	public void onTap(OverlayItem item, long id) {

		if (mListener != null) {
			mListener.onShowLargeMapAt(item.getPoint());
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arguments) {
		Uri uri = ContentUris.withAppendedId(POIs.CONTENT_URI_COPY, poiId);
		return new CursorLoader(getActivity(), uri, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d(TAG, "onLoadFinished: poiid = " + poiId);
		load(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {

	}

	private void load(Cursor c) {
		if (c == null || c.getCount() < 1)
			return;

		c.moveToFirst();
		String wmIdString = POIHelper.getWMId(c);
		WheelchairState state = POIHelper.getWheelchair(c);
		String name = POIHelper.getName(c);
		String comment = POIHelper.getComment(c);
		final double lat = POIHelper.getLatitude(c);
		int latE6 = (int) (lat * 1E6);
		final double lon = POIHelper.getLongitude(c);
		int lonE6 = (int) (lon * 1E6);

		int nodeTypeId = POIHelper.getNodeTypeId(c);
		int categoryId = POIHelper.getCategoryId(c);

		SupportManager sm = WheelmapApp.getSupportManager();

		NodeType nodeType = sm.lookupNodeType(nodeTypeId);
		setWheelchairState(state);
		if (name != null && name.length() > 0)
			nameText.setText(name);
		else
			nameText.setText(nodeType.localizedName);

		String category = sm.lookupCategory(categoryId).localizedName;
		categoryText.setText(category);
		nodetypeText.setText(nodeType.localizedName);
		nodetypeIcon.setImageDrawable(nodeType.iconDrawable);
		commentText.setText(comment);

		String address = POIHelper.getAddress(c);
		addressText.setText(address);

		String website = POIHelper.getWebsite(c);
		websiteText.setText(website);
		phoneText.setText(POIHelper.getPhone(c));

		String street = POIHelper.getStreet(c);
		String houseNum = POIHelper.getHouseNumber(c);
		String postCode = POIHelper.getPostcode(c);
		String city = POIHelper.getCity(c);

		fillDirectionsActionProvider(lat, lon, street, houseNum, postCode, city);
		fillShareActionProvider(wmIdString, name, nodeType.localizedName,
				comment, address, website);

		if (mCap == Capability.DEGRADED_MAX) {
			mMapButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mListener != null)
						mListener.onShowLargeMapAt(new GeoPoint(lat, lon));

				}
			});
		} else {
			SingleItemOverlay overlay = new SingleItemOverlay(this);
			overlay.setItem(name, comment, nodeType, state, latE6, lonE6);
			overlay.enableLowDrawQuality(true);
			mapView.getOverlays().clear();
			mapView.getOverlays().add(overlay);
			mapController.setCenter(new GeoPoint(lat, lon));
		}
	}

	private void setWheelchairState(WheelchairState newState) {
		mWheelchairState = newState;

		int stateColor = getResources().getColor(
				mWSAttributes.get(newState).colorId);

		title_container.setBackgroundColor(stateColor);
		stateIcon.setImageResource(mWSAttributes.get(newState).drawableId);
		stateText.setTextColor(stateColor);
		stateText.setText(mWSAttributes.get(newState).titleStringId);

	}

	private Intent createExternIntent(String action) {
		Intent intent = new Intent(action);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		return intent;
	}

	private void fillDirectionsActionProvider(double lat, double lon,
			String street, String houseNum, String postCode, String city) {

		Uri geoURI;

		String latitude = Double.toString(lat);
		String longitude = Double.toString(lon);

		if (!TextUtils.isEmpty(street) && !TextUtils.isEmpty(houseNum)
				&& !TextUtils.isEmpty(postCode) && !TextUtils.isEmpty(city)) {
			StringBuilder sb = new StringBuilder();
			sb.append(street).append("+").append(houseNum).append("+")
					.append(postCode).append(city);
			geoURI = Uri.parse("geo:" + latitude + "," + longitude + "?q="
					+ sb.toString().replace(" ", "+"));
		} else {
			geoURI = Uri.parse("geo:" + latitude + "," + longitude + "?z=17");
		}

		Log.d(TAG, "geoURI = " + geoURI.toString());
		Intent intent = createExternIntent(Intent.ACTION_VIEW);
		intent.setData(geoURI);

		setIntentOrStore(ACTION_PROVIDER_DIRECTIONS, intent,
				mDirectionsActionProvider);
	}

	private void fillShareActionProvider(String wmId, String name, String type,
			String comment, String address, String website) {

		StringBuilder sb = new StringBuilder();
		if (!TextUtils.isEmpty(name)) {
			sb.append(name);
		} else
			sb.append(type);

		if (!TextUtils.isEmpty(comment)) {
			sb.append(", ");
			sb.append(comment);
		}

		if (!TextUtils.isEmpty(address)) {
			sb.append(", ");
			sb.append(address);
		}

		if (!TextUtils.isEmpty(website)) {
			sb.append(", ");
			sb.append(website);
		}

		if (sb.length() > 0)
			sb.append(", ");

		sb.append("http://wheelmap.org/nodes/" + wmId);
		Intent intent = createExternIntent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
		setIntentOrStore(ACTION_PROVIDER_SHARE, intent, mShareActionProvider);
	}

	private void setIntentOrStore(int apKey, Intent intent,
			ShareActionProvider provider) {
		if (provider == null)
			intentSaved.put(apKey, intent);
		else
			provider.setShareIntent(intent);
	}

	private void setIntentOnActionProvider(int apKey,
			ShareActionProvider provider) {
		if (intentSaved.containsKey(apKey))
			provider.setShareIntent(intentSaved.get(apKey));
	}

}
