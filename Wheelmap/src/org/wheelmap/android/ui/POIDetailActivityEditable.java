package org.wheelmap.android.ui;

import java.util.HashMap;

import org.wheelmap.android.R;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.manager.SupportManager.NodeType;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.UserCredentials;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.service.SyncService;

import wheelmap.org.WheelchairState;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class POIDetailActivityEditable extends Activity {
	private final static String TAG = "poidetail";
	
	// Definition of the one requestCode we use for receiving resuls.
	static final private int SELECT_WHEELCHAIRSTATE = 0;

	
	private Long poiID;
	
	//private ImageView iconImage = null;
	private EditText nameText = null;
	private TextView nodetypeText = null;
	private EditText commentText = null;
	private EditText addressText = null;
	private EditText websiteText = null;
	private EditText phoneText = null;
	private ImageView mStateIcon = null;
	private TextView mWheelchairStateText = null;
	
	private WheelchairState mWheelChairState;
	
	private HashMap<WheelchairState, Integer> mWheelchairStateDrawablesMap = new HashMap<WheelchairState, Integer>();
	private HashMap<WheelchairState, Integer> mWheelchairStateTextColorMap = new HashMap<WheelchairState, Integer>();
	private HashMap<WheelchairState, Integer> mWheelchairStateTextsMap = new HashMap<WheelchairState, Integer>();


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_editable);
		
		nameText = (EditText) findViewById(R.id.name);
		nodetypeText = (TextView) findViewById(R.id.nodetype);

		phoneText = (EditText) findViewById(R.id.phone);
		addressText = (EditText) findViewById(R.id.addr);
		commentText = (EditText) findViewById(R.id.comment);
		websiteText = (EditText) findViewById(R.id.website);
		mStateIcon = (ImageView) findViewById(R.id.wheelchair_state_icon);
		mWheelchairStateText = (TextView) findViewById(R.id.wheelchair_state_text);

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

		mWheelchairStateText.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onEditWheelchairState(v);
			}
		});



		poiID=getIntent().getLongExtra(Wheelmap.POIs.EXTRAS_POI_ID, -1);

		if (poiID != -1) {
			load();
		}
	}
	
	public void onEditWheelchairState(View v) {
		// Start the activity whose result we want to retrieve. The
		// result will come back with request code GET_CODE.
		Intent intent = new Intent(POIDetailActivityEditable.this,
				WheelchairStateActivity.class);
		intent.putExtra(Wheelmap.POIs.WHEELCHAIR,
				(long) mWheelChairState.getId());
		startActivityForResult(intent, SELECT_WHEELCHAIRSTATE);
	}
	
	

	@Override
	public void onPause() {
		//TODO save temporary edited data (like in th enotebook API sample app)

		super.onPause();
	}
	
	public void onSaveClick(View v) {
		saveChanges();
		finish();
	}
	
	


	private void saveChanges() {

		// check if logged in
		UserCredentials userCredentials = new UserCredentials(this);

	//	if (userCredentials.isLoggedIn()) {

			ContentValues values = new ContentValues();
			//values.put(Wheelmap.POIs.NAME, jo.get("name").toString());
			/*
				int categoryId = jo.getInt( "category" );			
				String categoryIdentifier = SupportManager.get().lookupCategory(categoryId).identifier;
				values.put(Wheelmap.POIs.CATEGORY_ID, categoryId );
				values.put(Wheelmap.POIs.CATEGORY_IDENTIFIER, categoryIdentifier );

				int nodeTypeId = jo.getInt( "type");
				NodeType nodeType = SupportManager.get().lookupNodeType( nodeTypeId );
				String nodeTypeIdentifier = nodeType.identifier;
				values.put(Wheelmap.POIs.NODETYPE_ID, nodeTypeId );
				values.put(Wheelmap.POIs.NODETYPE_IDENTIFIER, nodeTypeIdentifier);

				values.put(Wheelmap.POIs.STREET, jo.get("street").toString());
				values.put(Wheelmap.POIs.POSTCODE, jo.get("postcode").toString());
				values.put(Wheelmap.POIs.CITY, jo.get("city").toString());
				values.put(Wheelmap.POIs.WEBSITE, jo.get("website").toString());
				values.put(Wheelmap.POIs.PHONE, jo.get("phone").toString());
				values.put(Wheelmap.POIs.WHEELCHAIR, jo.get("wheelchair").toString());
				values.put(Wheelmap.POIs.WHEELCHAIR_DESC, jo.get("comment").toString());
				values.put(Wheelmap.POIs.UPDATE_TAG, Wheelmap.UPDATE_ALL_NEW );
			 */


			//Uri poiUri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI, String.valueOf( poiID));
			//this.getContentResolver().update(poiUri, values, "", null);

		//	final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, SyncService.class);
		//	intent.putExtra(SyncService.EXTRA_WHAT, SyncService.WHAT_UPDATE_SERVER );
		//	startService(intent);
	//	} else
//		{
			// start login activity
//			startActivity(new Intent(this, LoginActivity.class));		
		//}
	}


	private void load() {

		Uri poiUri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI_POI_ID,
				String.valueOf(poiID));

		// Then query for this specific record:
		Cursor cur = managedQuery(poiUri, null, null, null, null);

		if (cur.getCount() < 1) {
			cur.close();
			return;
		}

		cur.moveToFirst();
		
		SupportManager manager = WheelmapApp.getSupportManager();
		WheelchairState state = POIHelper.getWheelchair(cur);
		String name = POIHelper.getName(cur);
		String comment = POIHelper.getComment(cur);
		int lat = (int) (POIHelper.getLatitude(cur) * 1E6);
		int lon = (int) (POIHelper.getLongitude(cur) * 1E6);
		int nodeTypeId = POIHelper.getNodeTypeId(cur);
		int categoryId = POIHelper.getCategoryId(cur);

		NodeType nodeType = manager.lookupNodeType(nodeTypeId);
	
		setWheelchairState(state);
		nameText.setText(name);
		String category =  manager.lookupCategory(categoryId).localizedName;
		nodetypeText.setText(nodeType.localizedName);
		commentText.setText(comment);
		addressText.setText(POIHelper.getAddress(cur));
		websiteText.setText(POIHelper.getWebsite(cur));
		phoneText.setText(POIHelper.getPhone(cur));
		
		cur.close();

	}
	
	private void setWheelchairState(WheelchairState newState) {
		mWheelChairState = newState;
		mStateIcon.setImageResource(mWheelchairStateDrawablesMap.get(newState));
		mWheelchairStateText.setTextColor(getResources().getColor(mWheelchairStateTextColorMap
				.get(newState)));
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
							Wheelmap.POIs.CONTENT_URI_POI_ID, String.valueOf(poiID));
					ContentValues values = new ContentValues();
					values.put(Wheelmap.POIs.WHEELCHAIR, newState.getId());
					values.put(Wheelmap.POIs.UPDATE_TAG,
							Wheelmap.UPDATE_WHEELCHAIR_STATE);
					this.getContentResolver().update(poiUri, values, "", null);

					final Intent intent = new Intent(Intent.ACTION_SYNC, null,
							POIDetailActivityEditable.this, SyncService.class);
					intent.putExtra(SyncService.EXTRA_WHAT,
							SyncService.WHAT_UPDATE_SERVER);
					startService(intent);

					load();
				}
			}
		}
	}


}	