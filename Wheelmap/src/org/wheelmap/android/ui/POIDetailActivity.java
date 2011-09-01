package org.wheelmap.android.ui;

import java.util.HashMap;

import org.wheelmap.android.R;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.Wheelmap;

import wheelmap.org.WheelchairState;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class POIDetailActivity extends Activity {

	private TextView name=null;

	private TextView comment=null;
	private TextView address=null;
	private TextView website=null;
	private TextView phone=null;
	private ImageView mStateIcon = null;
	private TextView mWheelchairStateText=null;
	private HashMap<WheelchairState, Integer> mWheelchairStateDrawablesMap = new HashMap<WheelchairState, Integer>();
	private HashMap<WheelchairState, Integer> mWheelchairStateTextColorMap = new HashMap<WheelchairState, Integer>();
	private HashMap<WheelchairState, Integer> mWheelchairStateTextsMap = new HashMap<WheelchairState, Integer>();


	private WheelchairState mWheelChairState;


	private Long poiID;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);   

		name=(TextView)findViewById(R.id.name);

		phone=(TextView)findViewById(R.id.phone);
		address=(TextView)findViewById(R.id.addr);
		comment=(TextView)findViewById(R.id.comment);
		website=(TextView)findViewById(R.id.website);
		mStateIcon = (ImageView)findViewById(R.id.wheelchair_state_icon);
		mWheelchairStateText=(TextView)findViewById(R.id.wheelchair_state_text);

		mWheelchairStateDrawablesMap.put(WheelchairState.YES, new Integer(R.drawable.wheelchair_state_enabled));
		mWheelchairStateDrawablesMap.put(WheelchairState.NO, new Integer(R.drawable.wheelchair_state_disabled));
		mWheelchairStateDrawablesMap.put(WheelchairState.LIMITED, new Integer(R.drawable.wheelchair_state_limited));
		mWheelchairStateDrawablesMap.put(WheelchairState.UNKNOWN, new Integer(R.drawable.wheelchair_state_unknown));

		mWheelchairStateTextColorMap.put(WheelchairState.YES, new Integer(R.color.wheel_enabled));
		mWheelchairStateTextColorMap.put(WheelchairState.NO, new Integer(R.color.wheel_disabled));
		mWheelchairStateTextColorMap.put(WheelchairState.LIMITED, new Integer(R.color.wheel_limited));
		mWheelchairStateTextColorMap.put(WheelchairState.UNKNOWN, new Integer(R.color.wheel_unknown));

		mWheelchairStateTextsMap.put(WheelchairState.YES, new Integer(R.string.ws_enabled_title));
		mWheelchairStateTextsMap.put(WheelchairState.NO, new Integer(R.string.ws_disabled_title));
		mWheelchairStateTextsMap.put(WheelchairState.LIMITED, new Integer(R.string.ws_limited_title));
		mWheelchairStateTextsMap.put(WheelchairState.UNKNOWN, new Integer(R.string.ws_unknown_title));

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

	@Override
	public void onPause() {
		//save();

		super.onPause();
	}

	public void onEditWheelchairState(View v) {
		// Start the activity whose result we want to retrieve.  The
		// result will come back with request code GET_CODE.
		Intent intent = new Intent(POIDetailActivity.this, WheelchairStateActivity.class);
		intent.putExtra(Wheelmap.POIs.WHEELCHAIR, (long)mWheelChairState.getId());
		startActivityForResult(intent, SELECT_WHEELCHAIRSTATE);
	}

	private void setWheelchairState(WheelchairState newState) {
		mWheelChairState = newState;
		mStateIcon.setImageResource(mWheelchairStateDrawablesMap.get(newState));
		mWheelchairStateText.setTextColor(mWheelchairStateTextColorMap.get(newState));
		mWheelchairStateText.setText(mWheelchairStateTextsMap.get(newState));
	}

	private void load() {

		// Use the ContentUris method to produce the base URI for the contact with _ID == 23.
		Uri poiUri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI, String.valueOf( poiID));

		// Then query for this specific record:
		Cursor cur = managedQuery(poiUri, null, null, null, null);

		if (cur.moveToFirst()) {	

			setWheelchairState(POIHelper.getWheelchair(cur));
			name.setText(POIHelper.getName(cur));
			comment.setText(POIHelper.getComment(cur));
			address.setText(POIHelper.getAddress(cur));
			website.setText(POIHelper.getWebsite(cur));
			phone.setText(POIHelper.getPhone(cur));
			cur.close();
		}
	}

	/**
	 * This method is called when the sending activity has finished, with the
	 * result it supplied.
	 * 
	 * @param requestCode The original request code as given to
	 *                    startActivity().
	 * @param resultCode From sending activity as per setResult().
	 * @param data From sending activity as per setResult().
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		// You can use the requestCode to select between multiple child
		// activities you may have started.  Here there is only one thing
		// we launch.
		if (requestCode == SELECT_WHEELCHAIRSTATE) {
			// This is a standard resultCode that is sent back if the
			// activity doesn't supply an explicit result.  It will also
			// be returned if the activity failed to launch.
			if (resultCode == RESULT_OK) {
				// newly selected wheelchair state as action data
				if (data != null) {
					WheelchairState newState = WheelchairState.valueOf(Integer.parseInt(data.getAction()) );
					setWheelchairState(newState);
					Uri poiUri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI, String.valueOf( poiID));
					ContentValues values = new ContentValues();
					values.put(Wheelmap.POIs.WHEELCHAIR, newState.getId());
					this.getContentResolver().update(poiUri, values, "", null);
				}
			}
		}
	}

	// Definition of the one requestCode we use for receiving resuls.
	static final private int SELECT_WHEELCHAIRSTATE = 0;

}	
