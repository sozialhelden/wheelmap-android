package org.wheelmap.android.ui;

import makemachine.android.formgenerator.FormActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.Wheelmap;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class POIDetailActivityEditable extends FormActivity {
	
	public static final int OPTION_SAVE = 0;
	public static final int OPTION_CANCEL = 1;

	private Long poiID;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		generateForm( FormActivity.parseFileToString( this, "schema_poi.json" ) );

		poiID=getIntent().getLongExtra(Wheelmap.POIs.EXTRAS_POI_ID, -1);

		if (poiID != -1) {
			try {
				load();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onPause() {
		//save();

		super.onPause();
	}
	
	 @Override
		public boolean onCreateOptionsMenu( Menu menu ) 
		{
			menu.add( 0, OPTION_SAVE, 0, "Save" );
			menu.add( 0, OPTION_CANCEL, 0, "Cancel" );
			return true;
		}
		
		@Override
		public boolean onMenuItemSelected( int id, MenuItem item )
		{
			
			switch( item.getItemId() )
			{
				case OPTION_SAVE:
					saveChanges();
					break;
				case OPTION_CANCEL:
					finish();					
					break;
			}
			
			return super.onMenuItemSelected( id, item );
		}
		
		private void saveChanges() {
			JSONObject jo;
			jo = save();
			// TODO JSON object to values
			
			// TODO update DB
			
			
			
		}


	private void load() throws JSONException {
		
		JSONObject jo = new JSONObject();
		
		// Use the ContentUris method to produce the base URI for the contact with _ID == 23.
		Uri poiUri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI, Long.toString(poiID));
	   
		// Then query for this specific record:
		Cursor cur = managedQuery(poiUri, null, null, null, null);
		

		if (cur.moveToFirst()) {		
			jo.put("name",POIHelper.getName(cur));
			jo.put("address",POIHelper.getAddress(cur));
			jo.put("website",POIHelper.getWebsite(cur));
			jo.put("wheelchair",POIHelper.getWheelchair(cur).getId());
			jo.put("phone",POIHelper.getPhone(cur));
			cur.close();
			
		}
		populate(jo.toString());
		
	}
}	