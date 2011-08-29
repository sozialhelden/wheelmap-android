package org.wheelmap.android.ui;

import org.wheelmap.android.R;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.Wheelmap;

import android.app.Activity;
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
}	
