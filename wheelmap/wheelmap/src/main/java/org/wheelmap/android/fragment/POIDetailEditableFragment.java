package org.wheelmap.android.fragment;

import java.util.Map;

import org.wheelmap.android.activity.POIDetailEditableActivity.ExternalEditableState;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.manager.SupportManager.NodeType;
import org.wheelmap.android.manager.SupportManager.WheelchairAttributes;
import org.wheelmap.android.model.CursorLoaderHelper;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Extra.What;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.UserCredentials;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.SyncService;

import roboguice.inject.InjectView;
import wheelmap.org.WheelchairState;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;

import de.akquinet.android.androlog.Log;

public class POIDetailEditableFragment extends RoboSherlockFragment implements
		OnClickListener, LoaderCallbacks<Cursor> {
	public final static String TAG = POIDetailEditableFragment.class
			.getSimpleName();

	private final static int LOADER_CONTENT = 0;

	@InjectView(R.id.title_container)
	private LinearLayout title_container;
	@InjectView(R.id.name)
	private EditText nameText;
	@InjectView(R.id.nodetype)
	private TextView nodetypeText;
	@InjectView(R.id.comment)
	private EditText commentText;
	@InjectView(R.id.addr)
	private EditText addressText;
	@InjectView(R.id.website)
	private EditText websiteText;
	@InjectView(R.id.phone)
	private EditText phoneText;
	@InjectView(R.id.wheelchair_state_icon)
	private ImageView state_icon;
	@InjectView(R.id.wheelchair_state_text)
	private TextView state_text;
	@InjectView(R.id.edit_position_text)
	private TextView position_text;

	@InjectView(R.id.wheelchair_state_layout)
	private RelativeLayout edit_state_container;
	@InjectView(R.id.edit_geolocation)
	private RelativeLayout edit_geolocation_touchable_container;
	@InjectView(R.id.edit_nodetype)
	private RelativeLayout edit_nodetype_container;

	@InjectView(R.id.edit_geolocation_container)
	private FrameLayout edit_geolocation_container;

	private Long poiID;
	private WheelchairState mWheelchairState;
	private int mLatitude;
	private int mLongitude;

	private int mNodeType;
	private Map<WheelchairState, WheelchairAttributes> mWSAttributes;

	private OnPOIDetailEditableListener mListener;

	public interface OnPOIDetailEditableListener {
		public void onEditSave();

		public void onEditWheelchairState(WheelchairState state);

		public void onEditGeolocation(int latitude, int longitude);

		public void onEditNodetype(int nodetype);

		public ExternalEditableState getExternalEditedState();
	}

	public static POIDetailEditableFragment newInstance(long poiId) {
		Bundle b = new Bundle();
		b.putLong(Extra.POI_ID, poiId);

		POIDetailEditableFragment fragment = new POIDetailEditableFragment();
		fragment.setArguments(b);

		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof OnPOIDetailEditableListener) {
			mListener = (OnPOIDetailEditableListener) activity;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWSAttributes = SupportManager.wsAttributes;
		setHasOptionsMenu(true);
		poiID = getArguments().getLong(Extra.POI_ID);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_detail_editable, container,
				false);

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		edit_state_container.setOnClickListener(this);
		edit_geolocation_touchable_container.setOnClickListener(this);
		edit_nodetype_container.setOnClickListener(this);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(LOADER_CONTENT, getArguments(), this);

		UserCredentials credentials = new UserCredentials(getActivity()
				.getApplicationContext());
		if (!credentials.isLoggedIn()) {
			FragmentManager fm = getActivity().getSupportFragmentManager();
			LoginDialogFragment loginDialog = new LoginDialogFragment();
			loginDialog.show(fm, LoginDialogFragment.TAG);
		}

	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		storeExternalEditedState();
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
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	private void storeExternalEditedState() {
		ExternalEditableState state = mListener.getExternalEditedState();
		if (state.state != null)
			setWheelchairState(state.state);
		if (state.nodetype != Extra.UNKNOWN)
			setNodetype(state.nodetype);
		if (state.latitude != Extra.UNKNOWN && state.longitude != Extra.UNKNOWN)
			setGeolocation(state.latitude, state.longitude);

		state.clear();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.ab_detaileditable_fragment, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
		case R.id.menu_save:
			save();
			break;
		default:
			// noop
		}

		return false;
	}

	@Override
	public void onClick(View v) {
		closeKeyboard();

		int id = v.getId();
		switch (id) {
		case R.id.wheelchair_state_layout: {
			if (mListener != null)
				mListener.onEditWheelchairState(mWheelchairState);
			break;
		}
		case R.id.edit_geolocation: {
			if (mListener != null)
				mListener.onEditGeolocation(mLatitude, mLongitude);
			break;
		}
		case R.id.edit_nodetype: {
			if (mListener != null)
				mListener.onEditNodetype(mNodeType);
			break;
		}

		default:
			// do nothing
		}
	}

	public void save() {
		saveChanges();

		ContentValues values = new ContentValues();
		values.put(Wheelmap.POIs.UPDATE_TAG, Wheelmap.UPDATE_ALL_FIELDS);
		getActivity().getContentResolver().update(getUriForPoiID(), values, "",
				null);

		final Intent intent = new Intent(Intent.ACTION_SYNC, null,
				getActivity(), SyncService.class);
		intent.putExtra(Extra.WHAT, What.UPDATE_SERVER);
		getActivity().startService(intent);

		if (mListener != null) {
			mListener.onEditSave();
		}
	}

	private Uri getUriForPoiID() {
		return Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI_POI_ID,
				String.valueOf(poiID));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arguments) {
		return CursorLoaderHelper.createPOIIdLoader(poiID);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		load(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {

	}

	private void load(Cursor cursor) {

		if (cursor == null || cursor.getCount() < 1)
			return;

		cursor.moveToFirst();

		SupportManager manager = WheelmapApp.getSupportManager();
		WheelchairState state = POIHelper.getWheelchair(cursor);
		String name = POIHelper.getName(cursor);
		String comment = POIHelper.getComment(cursor);
		mLatitude = POIHelper.getLatitudeAsInt(cursor);
		mLongitude = POIHelper.getLongitudeAsInt(cursor);
		int nodeTypeId = POIHelper.getNodeTypeId(cursor);

		NodeType nodeType = manager.lookupNodeType(nodeTypeId);
		updateWheelchairState(state);
		nameText.setText(name);
		nodetypeText.setText(nodeType.localizedName);
		String positionText = String.format("%s: (%f:%f)", getResources()
				.getString(R.string.position_geopoint), mLatitude / 1E6,
				mLongitude / 1E6);
		position_text.setText(positionText);
		commentText.setText(comment);
		addressText.setText(POIHelper.getAddress(cursor));
		websiteText.setText(POIHelper.getWebsite(cursor));
		phoneText.setText(POIHelper.getPhone(cursor));

		if (nodeTypeId == SupportManager.UNKNOWN_TYPE)
			showGeolocationEditor(true);
	}

	private void updateWheelchairState(WheelchairState newState) {
		mWheelchairState = newState;

		int stateColor = getResources().getColor(
				mWSAttributes.get(newState).colorId);

		title_container.setBackgroundColor(stateColor);
		state_icon.setImageResource(mWSAttributes.get(newState).drawableId);
		state_text.setTextColor(stateColor);
		state_text.setText(mWSAttributes.get(newState).titleStringId);
	}

	private void showGeolocationEditor(boolean show) {
		if (show)
			edit_geolocation_container.setVisibility(View.VISIBLE);
		else
			edit_geolocation_container.setVisibility(View.GONE);

	}

	private void saveChanges() {

		// check if logged in
		UserCredentials userCredentials = new UserCredentials(getActivity());

		// if (userCredentials.isLoggedIn()) {

		ContentValues values = new ContentValues();

		// values.put(Wheelmap.POIs.NAME, jo.get("name").toString());
		/*
		 * int categoryId = jo.getInt( "category" ); String categoryIdentifier =
		 * SupportManager.get().lookupCategory(categoryId).identifier;
		 * values.put(Wheelmap.POIs.CATEGORY_ID, categoryId );
		 * values.put(Wheelmap.POIs.CATEGORY_IDENTIFIER, categoryIdentifier );
		 * 
		 * int nodeTypeId = jo.getInt( "type"); NodeType nodeType =
		 * SupportManager.get().lookupNodeType( nodeTypeId ); String
		 * nodeTypeIdentifier = nodeType.identifier;
		 * values.put(Wheelmap.POIs.NODETYPE_ID, nodeTypeId );
		 * values.put(Wheelmap.POIs.NODETYPE_IDENTIFIER, nodeTypeIdentifier);
		 * 
		 * values.put(Wheelmap.POIs.STREET, jo.get("street").toString());
		 * values.put(Wheelmap.POIs.POSTCODE, jo.get("postcode").toString());
		 * values.put(Wheelmap.POIs.CITY, jo.get("city").toString());
		 * values.put(Wheelmap.POIs.WEBSITE, jo.get("website").toString());
		 * values.put(Wheelmap.POIs.PHONE, jo.get("phone").toString());
		 * values.put(Wheelmap.POIs.WHEELCHAIR,
		 * jo.get("wheelchair").toString());
		 * values.put(Wheelmap.POIs.WHEELCHAIR_DESC,
		 * jo.get("comment").toString()); values.put(Wheelmap.POIs.UPDATE_TAG,
		 * Wheelmap.UPDATE_ALL_NEW );
		 */

		// Uri poiUri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI,
		// String.valueOf( poiID));
		// this.getContentResolver().update(poiUri, values, "", null);

		// final Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
		// SyncService.class);
		// intent.putExtra(Extras.WHAT, Extras.What.UPDATE_SERVER);
		// startService(intent);
		// } else
		// {
		// start login activity
		// startActivity(new Intent(this, LoginActivity.class));
		// }
	}

	public void setWheelchairState(WheelchairState state) {
		ContentValues values = new ContentValues();
		values.put(Wheelmap.POIs.WHEELCHAIR, state.getId());
		getActivity().getContentResolver().update(getUriForPoiID(), values, "",
				null);
	}

	public void setGeolocation(int latitude, int longitude) {
		Log.d(TAG, "onResult: mLatitude = " + mLatitude + " mLongitude = "
				+ mLongitude);

		ContentValues values = new ContentValues();
		values.put(Wheelmap.POIs.COORD_LAT, latitude);
		values.put(Wheelmap.POIs.COORD_LON, longitude);

		getActivity().getContentResolver().update(getUriForPoiID(), values, "",
				null);
	}

	public void setNodetype(int nodetype) {
		if (mNodeType == nodetype)
			return;

		mNodeType = nodetype;
		ContentValues values = new ContentValues();
		values.put(Wheelmap.POIs.NODETYPE_ID, mNodeType);
		int categoryId = WheelmapApp.getSupportManager().lookupNodeType(
				nodetype).categoryId;
		values.put(Wheelmap.POIs.CATEGORY_ID, categoryId);
		getActivity().getContentResolver().update(getUriForPoiID(), values, "",
				null);
	}

	public void closeKeyboard() {
		View view = getView().findFocus();
		if (view == null || !(view instanceof EditText))
			return;

		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

	}
}
