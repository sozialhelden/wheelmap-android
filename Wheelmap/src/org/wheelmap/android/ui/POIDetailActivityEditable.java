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

package org.wheelmap.android.ui;

import java.util.HashMap;

import org.wheelmap.android.online.R;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.manager.SupportManager.NodeType;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.UserCredentials;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.ui.mapsforge.EditPositionActivity;

import wheelmap.org.WheelchairState;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class POIDetailActivityEditable extends Activity implements
		OnClickListener {
	private final static String TAG = "poidetail";

	// Definition of the one requestCode we use for receiving resuls.
	private static final int SELECT_WHEELCHAIRSTATE = 0;
	private static final int SELECT_GEOPOSITION = 1;
	private static final int PERFORM_LOGIN = 2;
	private static final int SELECT_NODETYPE = 3;

	private Long poiID;

	// private ImageView iconImage = null;
	private EditText nameText = null;
	private TextView nodetypeText = null;
	private EditText commentText = null;
	private EditText addressText = null;
	private EditText websiteText = null;
	private EditText phoneText = null;
	private ImageView mStateIcon = null;
	private TextView mWheelchairStateText = null;
	private TextView mPositionText = null;

	private RelativeLayout mEditWheelchairStateContainer;
	private RelativeLayout mEditGeolocationContainer;
	private RelativeLayout mEditNodeTypeContainer;

	private WheelchairState mWheelChairState;
	private int mLatitude;
	private int mLongitude;

	private HashMap<WheelchairState, Integer> mWheelchairStateDrawablesMap = new HashMap<WheelchairState, Integer>();
	private HashMap<WheelchairState, Integer> mWheelchairStateTextColorMap = new HashMap<WheelchairState, Integer>();
	private HashMap<WheelchairState, Integer> mWheelchairStateTextsMap = new HashMap<WheelchairState, Integer>();

	private int mNodeType;

	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_editable);

		mEditWheelchairStateContainer = (RelativeLayout) findViewById(R.id.edit_wheelchairstate);
		mEditWheelchairStateContainer.setOnClickListener(this);
		mEditGeolocationContainer = (RelativeLayout) findViewById(R.id.edit_geolocation);
		mEditGeolocationContainer.setOnClickListener(this);
		mEditNodeTypeContainer = (RelativeLayout) findViewById( R.id.edit_nodetype );
		mEditNodeTypeContainer.setOnClickListener(this);

		nameText = (EditText) findViewById(R.id.name);
		nodetypeText = (TextView) findViewById( R.id.nodetype );		
		phoneText = (EditText) findViewById(R.id.phone);
		addressText = (EditText) findViewById(R.id.addr);
		commentText = (EditText) findViewById(R.id.comment);
		websiteText = (EditText) findViewById(R.id.website);
		mStateIcon = (ImageView) findViewById(R.id.wheelchair_state_icon);
		mWheelchairStateText = (TextView) findViewById(R.id.wheelchair_state_text);
		mPositionText = (TextView) findViewById(R.id.edit_position_text);

		mWheelchairStateDrawablesMap.put(WheelchairState.YES, new Integer(
				R.drawable.wheelchair_state_enabled));
		mWheelchairStateDrawablesMap.put(WheelchairState.NO, new Integer(
				R.drawable.wheelchair_state_disabled));
		mWheelchairStateDrawablesMap.put(WheelchairState.LIMITED, new Integer(
				R.drawable.wheelchair_state_limited));
		mWheelchairStateDrawablesMap.put(WheelchairState.UNKNOWN, new Integer(
				R.drawable.wheelchair_state_unknown));

		mWheelchairStateTextColorMap.put(WheelchairState.YES, new Integer(
				R.color.wheel_enabled));
		mWheelchairStateTextColorMap.put(WheelchairState.NO, new Integer(
				R.color.wheel_disabled));
		mWheelchairStateTextColorMap.put(WheelchairState.LIMITED, new Integer(
				R.color.wheel_limited));
		mWheelchairStateTextColorMap.put(WheelchairState.UNKNOWN, new Integer(
				R.color.wheel_unknown));

		mWheelchairStateTextsMap.put(WheelchairState.YES, new Integer(
				R.string.ws_enabled_title));
		mWheelchairStateTextsMap.put(WheelchairState.NO, new Integer(
				R.string.ws_disabled_title));
		mWheelchairStateTextsMap.put(WheelchairState.LIMITED, new Integer(
				R.string.ws_limited_title));
		mWheelchairStateTextsMap.put(WheelchairState.UNKNOWN, new Integer(
				R.string.ws_unknown_title));

		poiID = getIntent().getLongExtra(Wheelmap.POIs.EXTRAS_POI_ID, -1);

		if (poiID != -1) {
			load();
		}

		UserCredentials credentials = new UserCredentials(
				getApplicationContext());
		if (!credentials.isLoggedIn()) {
			Intent intent = new Intent(this, LoginActivity.class);
			startActivityForResult(intent, PERFORM_LOGIN);
		}
	}

	@Override
	public void onPause() {
		// TODO save temporary edited data (like in th enotebook API sample app)
		super.onPause();
	}

	public void onSaveClick(View v) {
		saveChanges();

		Uri poiUri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI_POI_ID,
				String.valueOf(poiID));
		ContentValues values = new ContentValues();
		values.put(Wheelmap.POIs.UPDATE_TAG, Wheelmap.UPDATE_ALL_FIELDS);
		getContentResolver().update(poiUri, values, "", null);

		final Intent intent = new Intent(Intent.ACTION_SYNC, null,
				POIDetailActivityEditable.this, SyncService.class);
		intent.putExtra(SyncService.EXTRA_WHAT, SyncService.WHAT_UPDATE_SERVER);
		startService(intent);
		finish();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.edit_wheelchairstate: {
			Intent intent = new Intent(POIDetailActivityEditable.this,
					WheelchairStateActivity.class);
			intent.putExtra(Wheelmap.POIs.WHEELCHAIR,
					(long) mWheelChairState.getId());
			startActivityForResult(intent, SELECT_WHEELCHAIRSTATE);
			break;
		}
		case R.id.edit_geolocation: {
			Intent intent = new Intent(this, EditPositionActivity.class);
			intent.putExtra(EditPositionActivity.EXTRA_LATITUDE, mLatitude);
			intent.putExtra(EditPositionActivity.EXTRA_LONGITUDE, mLongitude);
			startActivityForResult(intent, SELECT_GEOPOSITION);
			break;
		}
		case R.id.edit_nodetype: {
			Intent intent = new Intent(this, NodeTypeSelectActivity.class);
			intent.putExtra(NodeTypeSelectActivity.EXTRA_NODETYPE, mNodeType );
			startActivityForResult(intent, SELECT_NODETYPE );
		}
		
		default:
			// do nothing
		}
	}

	private void saveChanges() {

		// check if logged in
		UserCredentials userCredentials = new UserCredentials(this);

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
		// intent.putExtra(SyncService.EXTRA_WHAT,
		// SyncService.WHAT_UPDATE_SERVER );
		// startService(intent);
		// } else
		// {
		// start login activity
		// startActivity(new Intent(this, LoginActivity.class));
		// }
	}

	private void load() {

		Uri poiUri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI_POI_ID,
				String.valueOf(poiID));

		// Then query for this specific record:
		Cursor cur = managedQuery(poiUri, null, null, null, null);
		if ( cur == null )
			return;

		if (cur.getCount() < 1) {
			// cur.close(); managed cursors dont need to be closed
			return;
		}

		cur.moveToFirst();

		SupportManager manager = WheelmapApp.getSupportManager();
		WheelchairState state = POIHelper.getWheelchair(cur);
		String name = POIHelper.getName(cur);
		String comment = POIHelper.getComment(cur);
		mLatitude = POIHelper.getLatitudeAsInt(cur);
		mLongitude = POIHelper.getLongitudeAsInt(cur);
		int nodeTypeId = POIHelper.getNodeTypeId(cur);
		int categoryId = POIHelper.getCategoryId(cur);

		NodeType nodeType = manager.lookupNodeType(nodeTypeId);

		setWheelchairState(state);
		nameText.setText(name);
		String category = manager.lookupCategory(categoryId).localizedName;
		nodetypeText.setText(nodeType.localizedName);
		String positionText = String.format("%s: %f %s: %f", getResources()
				.getString(R.string.position_latitude_short), mLatitude / 1E6,
				getResources().getString(R.string.position_longitude_short),
				mLongitude / 1E6);
		mPositionText.setText(positionText);
		commentText.setText(comment);
		addressText.setText(POIHelper.getAddress(cur));
		websiteText.setText(POIHelper.getWebsite(cur));
		phoneText.setText(POIHelper.getPhone(cur));

//		cur.close(); managed cursors dont need to be closed

	}

	private void setWheelchairState(WheelchairState newState) {
		mWheelChairState = newState;
		mStateIcon.setImageResource(mWheelchairStateDrawablesMap.get(newState));
		mWheelchairStateText.setTextColor(getResources().getColor(
				mWheelchairStateTextColorMap.get(newState)));
		mWheelchairStateText.setText(mWheelchairStateTextsMap.get(newState));
	}

	/**
	 * This method is called when the sending activity has finished, with the
	 * result it supplied.
	 * 
	 * @param requestCode
	 *            The original request code as given to startActivity().
	 * @param resultCode
	 *            From sending activity as per setResult().
	 * @param data
	 *            From sending activity as per setResult().
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// You can use the requestCode to select between multiple child
		// activities you may have started. Here there is only one thing
		// we launch.
		if (requestCode == SELECT_WHEELCHAIRSTATE) {
			// This is a standard resultCode that is sent back if the
			// activity doesn't supply an explicit result. It will also
			// be returned if the activity failed to launch.
			if (resultCode == RESULT_OK) {
				// newly selected wheelchair state as action data
				if (data != null) {
					WheelchairState newState = WheelchairState.valueOf(Integer
							.parseInt(data.getAction()));
					Uri poiUri = Uri.withAppendedPath(
							Wheelmap.POIs.CONTENT_URI_POI_ID,
							String.valueOf(poiID));
					ContentValues values = new ContentValues();
					values.put(Wheelmap.POIs.WHEELCHAIR, newState.getId());
					getContentResolver().update(poiUri, values, "", null);
					load();
				}
			}
		} else if (requestCode == SELECT_GEOPOSITION) {
			if (resultCode == RESULT_OK) {
				if (data != null) {
					Bundle bundle = data.getExtras();
					mLatitude = bundle
							.getInt(EditPositionActivity.EXTRA_LATITUDE);
					mLongitude = bundle
							.getInt(EditPositionActivity.EXTRA_LONGITUDE);
					Log.d( TAG, "onResult: mLatitude = " + mLatitude + " mLongitude = " + mLongitude );
					Uri poiUri = Uri.withAppendedPath(
							Wheelmap.POIs.CONTENT_URI_POI_ID,
							String.valueOf(poiID));
					ContentValues values = new ContentValues();
					values.put(Wheelmap.POIs.COORD_LAT, mLatitude);
					values.put(Wheelmap.POIs.COORD_LON, mLongitude);

					getContentResolver().update(poiUri, values, "", null);
					load();
				}
			}

		} else if (requestCode == PERFORM_LOGIN) {

			if (resultCode != RESULT_OK)
				finish();
		} else if (requestCode == SELECT_NODETYPE )
			if (resultCode == RESULT_OK) {
				if (data != null ) {
					Bundle bundle = data.getExtras();
					int newNodeType = bundle.getInt(NodeTypeSelectActivity.EXTRA_NODETYPE);
					if ( mNodeType == newNodeType )
						return;
					
					mNodeType = newNodeType;
					Uri poiUri = Uri.withAppendedPath(
							Wheelmap.POIs.CONTENT_URI_POI_ID,
							String.valueOf(poiID));
					ContentValues values = new ContentValues();
					values.put(Wheelmap.POIs.NODETYPE_ID, mNodeType);
					int categoryId = WheelmapApp.getSupportManager().lookupNodeType( newNodeType ).categoryId;
					values.put(Wheelmap.POIs.CATEGORY_ID, categoryId );
					getContentResolver().update(poiUri, values, "", null);
					load();
				}
			}
	}
}
