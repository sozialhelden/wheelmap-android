package org.wheelmap.android.ui;

import org.wheelmap.android.R;
import org.wheelmap.android.model.UserCredentials;
import org.wheelmap.android.model.Wheelmap;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class POIDetailActivityEditable extends Activity {
	private final static String TAG = "poidetail";
	
	private Long poiID;
	
	//private ImageView iconImage = null;
	private EditText nameText = null;
	private EditText categoryText = null;
	private EditText nodetypeText = null;
	private EditText commentText = null;
	private EditText addressText = null;
	private EditText websiteText = null;
	private EditText phoneText = null;
	private ImageView mStateIcon = null;
	private TextView mWheelchairStateText = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_editable);


		poiID=getIntent().getLongExtra(Wheelmap.POIs.EXTRAS_POI_ID, -1);

		if (poiID != -1) {
			load();
		}
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

		// Use the ContentUris method to produce the base URI for the contact with _ID == 23.
		Uri poiUri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI, Long.toString(poiID));

		// Then query for this specific record:
		Cursor cur = managedQuery(poiUri, null, null, null, null);


		if (cur.moveToFirst()) {		
			/*		jo.put("name",POIHelper.getName(cur));
			jo.put("street",POIHelper.getStreet(cur));
			jo.put("postcode",POIHelper.getPostcode(cur));
			jo.put("city",POIHelper.getCity(cur));
			jo.put("website",POIHelper.getWebsite(cur));
			jo.put("wheelchair",POIHelper.getWheelchair(cur).getId());
			jo.put("phone",POIHelper.getPhone(cur));
			jo.put("comment",POIHelper.getComment(cur));
			jo.put( "type", POIHelper.getNodeTypeId( cur ));
			jo.put( "category", POIHelper.getCategoryId( cur ));
			 */
			cur.close();

		}
	}

}	