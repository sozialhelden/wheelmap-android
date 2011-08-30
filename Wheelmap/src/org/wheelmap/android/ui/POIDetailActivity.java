package org.wheelmap.android.ui;

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
import android.widget.TextView;

public class POIDetailActivity extends Activity {

	private TextView name=null;

	private TextView comment=null;
	private TextView address=null;
	private TextView website=null;
	private TextView phone=null;
	private TextView wheelchairstate=null;

	
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
		wheelchairstate = (TextView)findViewById(R.id.wheelchair_state_text);
		
		wheelchairstate.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Start the activity whose result we want to retrieve.  The
	            // result will come back with request code GET_CODE.
	            Intent intent = new Intent(POIDetailActivity.this, WheelchairStateActivity.class);
	            startActivityForResult(intent, SELECT_WHEELCHAIRSTATE);
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

	private void load() {

		// Use the ContentUris method to produce the base URI for the contact with _ID == 23.
		Uri poiUri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI, String.valueOf( poiID));

		// Then query for this specific record:
		Cursor cur = managedQuery(poiUri, null, null, null, null);

		if (cur.moveToFirst()) {	
			
			
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
