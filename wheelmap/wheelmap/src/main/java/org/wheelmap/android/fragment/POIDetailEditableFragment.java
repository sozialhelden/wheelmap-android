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

import java.util.Map;

import android.os.Handler;
import com.google.inject.Inject;
import org.wheelmap.android.modules.ICredentials;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.fragment.ErrorDialogFragment.OnErrorDialogListener;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.manager.SupportManager.Category;
import org.wheelmap.android.manager.SupportManager.NodeType;
import org.wheelmap.android.manager.SupportManager.WheelchairAttributes;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.PrepareDatabaseHelper;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.service.SyncServiceHelper;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.DetachableResultReceiver.Receiver;
import org.wheelmap.android.utils.UtilsMisc;

import roboguice.inject.InjectView;
import wheelmap.org.WheelchairState;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
		OnErrorDialogListener, Receiver, OnClickListener, LoaderCallbacks<Cursor> {
	public final static String TAG = POIDetailEditableFragment.class
			.getSimpleName();

	private final static int LOADER_CONTENT = 0;
	private final static int LOADER_TMP = 1;

	private static final int DIALOG_ID_NEWPOI = 1;
	private static final int DIALOG_ID_NETWORK_ERROR = 2;

	@Inject
	private ICredentials mCredentials;

	@InjectView(R.id.title_container)
	private LinearLayout title_container;
	@InjectView(R.id.name)
	private EditText nameText;
	@InjectView(R.id.nodetype)
	private TextView nodetypeText;
	@InjectView(R.id.comment)
	private EditText commentText;
	@InjectView(R.id.street)
	private EditText streetText;
	@InjectView(R.id.housenum)
	private EditText housenumText;
	@InjectView(R.id.postcode)
	private EditText postcodeText;
	@InjectView(R.id.city)
	private EditText cityText;
	@InjectView(R.id.website)
	private EditText websiteText;
	@InjectView(R.id.phone)
	private EditText phoneText;
	@InjectView(R.id.state_icon)
	private ImageView state_icon;
	@InjectView(R.id.state_text)
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
	private LinearLayout edit_geolocation_container;

	private Long poiID = Extra.ID_UNKNOWN;
	private String wmID;
	private WheelchairState mWheelchairState;
	private double mLatitude;
	private double mLongitude;
	private int mNodeType;

	private Map<WheelchairState, WheelchairAttributes> mWSAttributes;
	private OnPOIDetailEditableListener mListener;

	private boolean mTemporaryStored;
	private DetachableResultReceiver mReceiver;

	public interface OnPOIDetailEditableListener {
		public void onEditSave();

		public void onEditWheelchairState(WheelchairState state);

		public void onEditGeolocation(double latitude, double longitude);

		public void onEditNodetype(int nodetype);

		public void requestExternalEditedState(
				POIDetailEditableFragment fragment);
		public void onStoring(boolean isRefreshing);

	}

	public static POIDetailEditableFragment newInstance(long poiId) {
		Bundle b = new Bundle();
		b.putLong(Extra.POI_ID, poiId);

		POIDetailEditableFragment fragment = new POIDetailEditableFragment();
		fragment.setArguments(b);

		return fragment;
	}

	public POIDetailEditableFragment() {
		Log.d(TAG, "constructor called ");
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
		Log.d(TAG, "onCreate");
		setHasOptionsMenu(true);
		mWSAttributes = SupportManager.wsAttributes;
		poiID = getArguments().getLong(Extra.POI_ID);
		mReceiver = new DetachableResultReceiver(new Handler());
		mReceiver.setReceiver(this);
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
		Log.d(TAG, "onActivityCreated");

		retrieve(savedInstanceState);
		if (!mCredentials.isLoggedIn()) {
			FragmentManager fm = getFragmentManager();
			LoginDialogFragment loginDialog = new LoginDialogFragment();
			loginDialog.show(fm, LoginDialogFragment.TAG);
		}

	}

	private void retrieve(Bundle bundle) {
		boolean loadTempStore = mTemporaryStored
				|| (bundle != null && bundle
						.containsKey(Extra.TEMPORARY_STORED));
		int loaderId;
		if (loadTempStore)
			loaderId = LOADER_TMP;
		else
			loaderId = LOADER_CONTENT;

		Log.d(TAG, "retrieve: init loader id = " + loaderId);
		getLoaderManager().initLoader(loaderId, null, this);
	}

	@Override
	public void onPause() {
		super.onPause();
		storeTemporary();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
		outState.putBoolean(Extra.TEMPORARY_STORED, true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.ab_detaileditable_fragment, menu);
		super.onCreateOptionsMenu(menu, inflater);
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
		ContentValues values = retrieveContentValues();
		if (!values.containsKey(POIs.NODETYPE_ID)) {
			showErrorMessage(getString(R.string.error_category_missing_title),
					getString(R.string.error_category_missing_message),
					Extra.UNKNOWN);
			return;
		} else if (mWheelchairState == WheelchairState.UNKNOWN) {
			showErrorMessage(
					getString(R.string.error_wheelchairstate_missing_title),
					getString(R.string.error_wheelchairstate_missing_message),
					Extra.UNKNOWN);
			return;
		}

		values.put(POIs.DIRTY, POIs.DIRTY_ALL);
		PrepareDatabaseHelper.editCopy(getActivity().getContentResolver(),
				poiID, values);
		SyncServiceHelper.executeUpdateServer(getActivity(), mReceiver);
	}

	private void quit() {
		if (mListener != null) {
			mListener.onEditSave();
		}
	}

	@Override
	public void onErrorDialogClose(int id) {
		Log.d(TAG, "onErrorDialogClose");
		if (id == DIALOG_ID_NEWPOI || id == DIALOG_ID_NETWORK_ERROR)
			quit();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arguments) {
		Log.d(TAG, "onCreateLoader: id = " + id);
		Uri uri = null;
		if (id == LOADER_CONTENT)
			uri = ContentUris.withAppendedId(POIs.CONTENT_URI_COPY, poiID);
		else
			uri = POIs.CONTENT_URI_TMP;

		Log.d(TAG, "onCreateLoader: uri = " + uri);
		return new CursorLoader(getActivity(), uri, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d(TAG, "onLoadFinished loader id = " + loader.getId());
		load(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {

	}

	private void load(Cursor cursor) {

		if (cursor == null || cursor.getCount() < 1)
			return;

		UtilsMisc.dumpCursorToLog(TAG, cursor);
		cursor.moveToFirst();

		WheelchairState state = POIHelper.getWheelchair(cursor);
		String name = POIHelper.getName(cursor);
		String comment = POIHelper.getComment(cursor);
		double latitude = POIHelper.getLatitude(cursor);
		double longitude = POIHelper.getLongitude(cursor);
		int nodeType = POIHelper.getNodeTypeId(cursor);

		setGeolocation(latitude, longitude);
		setNodetype(nodeType);
		setWheelchairState(state);
		nameText.setText(name);
		commentText.setText(comment);

		streetText.setText(POIHelper.getStreet(cursor));
		housenumText.setText(POIHelper.getHouseNumber(cursor));
		postcodeText.setText(POIHelper.getPostcode(cursor));
		cityText.setText(POIHelper.getCity(cursor));

		websiteText.setText(POIHelper.getWebsite(cursor));
		phoneText.setText(POIHelper.getPhone(cursor));

		wmID = POIHelper.getWMId(cursor);
		if (TextUtils.isEmpty(wmID))
			showGeolocationEditor(true);

		retrieveExternalEditedState();
	}

	private void showGeolocationEditor(boolean show) {
		if (show)
			edit_geolocation_container.setVisibility(View.VISIBLE);
		else
			edit_geolocation_container.setVisibility(View.GONE);
	}

	private void retrieveExternalEditedState() {
		Log.d(TAG, "retrieveExternalEditedState");
		if (mListener != null)
			mListener.requestExternalEditedState(this);
	}

	private ContentValues retrieveContentValues() {
		ContentValues values = new ContentValues();

		String name = nameText.getText().toString();
		if (!TextUtils.isEmpty(name))
			values.put(POIs.NAME, name);

		SupportManager sm = WheelmapApp.getSupportManager();

		if (mNodeType != SupportManager.UNKNOWN_TYPE) {
			NodeType nodeType = sm.lookupNodeType(mNodeType);
			Category category = sm.lookupCategory(nodeType.categoryId);

			values.put(POIs.CATEGORY_ID, nodeType.categoryId);
			values.put(POIs.CATEGORY_IDENTIFIER, category.identifier);
			values.put(POIs.NODETYPE_IDENTIFIER, nodeType.identifier);
			values.put(POIs.NODETYPE_ID, mNodeType);

		}

		values.put(POIs.LATITUDE, mLatitude);
		values.put(POIs.LONGITUDE, mLongitude);

		values.put(POIs.WHEELCHAIR, mWheelchairState.getId());
		String description = commentText.getText().toString();
		if (!TextUtils.isEmpty(description))
			values.put(POIs.DESCRIPTION, description);

		String street = streetText.getText().toString();
		if (!TextUtils.isEmpty(street))
			values.put(POIs.STREET, street);
		String housenum = housenumText.getText().toString();
		if (!TextUtils.isEmpty(housenum))
			values.put(POIs.HOUSE_NUM, housenum);
		String postcode = postcodeText.getText().toString();
		if (!TextUtils.isEmpty(postcode))
			values.put(POIs.POSTCODE, postcode);
		String city = cityText.getText().toString();
		if (!TextUtils.isEmpty(city))
			values.put(POIs.CITY, city);

		String website = websiteText.getText().toString();
		if (!TextUtils.isEmpty(website))
			values.put(POIs.WEBSITE, website);
		String phone = phoneText.getText().toString();
		if (!TextUtils.isEmpty(phone))
			values.put(POIs.PHONE, phone);
		return values;
	}

	public void setWheelchairState(WheelchairState state) {
		if (state == null)
			return;

		// Log.d(TAG, "setWheelchairState state = " + state.name());
		mWheelchairState = state;

		int stateColor = getResources().getColor(
				mWSAttributes.get(state).colorId);

		title_container.setBackgroundColor(stateColor);
		state_icon.setImageResource(mWSAttributes.get(state).drawableId);
		state_text.setTextColor(stateColor);
		state_text.setText(mWSAttributes.get(state).titleStringId);
	}

	public void setGeolocation(double latitude, double longitude) {
		if (latitude == Extra.UNKNOWN || longitude == Extra.UNKNOWN)
			return;

		// Log.d(TAG, "setGeolocation: latitude = " + latitude + " longitude = "
		// + longitude);
		mLatitude = latitude;
		mLongitude = longitude;

		String positionText = String.format("%s: (%.6f:%.6f)", getResources()
				.getString(R.string.position_geopoint), mLatitude, mLongitude);
		position_text.setText(positionText);
	}

	public void setNodetype(int nodetype) {
		if (nodetype == SupportManager.UNKNOWN_TYPE)
			return;

		// Log.d(TAG, "setNodetype: nodetype = " + nodetype);
		mNodeType = nodetype;
		SupportManager manager = WheelmapApp.getSupportManager();
		NodeType nodeType = manager.lookupNodeType(nodetype);
		nodetypeText.setText(nodeType.localizedName);
	}

	public void showErrorMessage(String title, String message, int id) {
		FragmentManager fm = getFragmentManager();
		ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance(
				title, message, id );
		if (errorDialog == null)
			return;
		errorDialog.setOnErrorDialogListener(this);
		errorDialog.show(fm, ErrorDialogFragment.TAG);
	}

	public void showNetworkErrorMessage(SyncServiceException e, int id ) {
		FragmentManager fm = getFragmentManager();
		ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance( e, id );
		if ( errorDialog == null)
			return;
		errorDialog.setOnErrorDialogListener(this);
		errorDialog.show(fm, ErrorDialogFragment.TAG);
	}

	public void closeKeyboard() {
		View view = getView().findFocus();
		if (view == null || !(view instanceof EditText))
			return;

		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

	}

	private void storeTemporary() {
		ContentValues values = retrieveContentValues();
		if (!TextUtils.isEmpty(wmID))
			values.put(POIs.WM_ID, wmID);

		long id = PrepareDatabaseHelper.storeTemporary(getActivity()
				.getContentResolver(), values);
		Log.d(TAG, "storeTemporary wmId = " + wmID + " id = " + id);
		getLoaderManager().destroyLoader(LOADER_CONTENT);
		mTemporaryStored = true;
	}

	private void storingStatus( boolean storing ) {
		if ( mListener != null)
			mListener.onStoring( storing );
	}

	private void showNewPoiOrQuit() {
		if (TextUtils.isEmpty(wmID)) {
			showErrorMessage(getString(R.string.error_newpoi_title),
					getString(R.string.error_newpoi_message), DIALOG_ID_NEWPOI);
		} else {
			quit();
		}
	}

	/** {@inheritDoc} */
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Log.d(TAG, "onReceiveResult resultCode = " + resultCode);
		switch (resultCode) {
			case SyncService.STATUS_RUNNING: {
				storingStatus(true);
				break;
			}
			case SyncService.STATUS_FINISHED: {
				storingStatus(false);
				showNewPoiOrQuit();
				break;
			}
			case SyncService.STATUS_ERROR: {
				storingStatus(false);
				showNewPoiOrQuit();
				break;
			}
		}
	}
}
