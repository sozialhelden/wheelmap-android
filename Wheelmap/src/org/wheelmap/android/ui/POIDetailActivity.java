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
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class POIDetailActivity extends Activity {

	private TextView name=null;
	private TextView address=null;
	private EditText notes=null;
	private RadioGroup types=null;
	private Long poiID;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);   

		name=(TextView)findViewById(R.id.name);
		address=(TextView)findViewById(R.id.addr);
		notes=(EditText)findViewById(R.id.notes);
		types=(RadioGroup)findViewById(R.id.wheel_chair_type);

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
			address.setText(POIHelper.getAddress(cur));
			notes.setText(cur.getString(cur.getColumnIndexOrThrow(Wheelmap.POIsColumns.WEBSITE)));

			switch (POIHelper.getWheelchair(cur)) {
			case UNKNOWN: {
				types.check(R.id.unknown);
				break;
			}
			case YES:
				types.check(R.id.yes);
				break;
			case LIMITED:
				types.check(R.id.limited);
				break;
			case NO:
				types.check(R.id.no);
				break;
			default:
				types.check(R.id.unknown);
				break;
			}

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
                	WheelchairState newState = WheelchairState.valueOf(data.getAction());
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
