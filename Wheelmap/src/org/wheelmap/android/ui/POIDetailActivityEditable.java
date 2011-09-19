package org.wheelmap.android.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import makemachine.android.formgenerator.FormActivity;
import makemachine.android.formgenerator.FormSpinner;
import makemachine.android.formgenerator.FormWidget;

import org.json.JSONException;
import org.json.JSONObject;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.manager.SupportManager.Category;
import org.wheelmap.android.manager.SupportManager.NodeType;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.UserCredentials;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.service.SyncService;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class POIDetailActivityEditable extends FormActivity {
	private final static String TAG = "poidetail";

	public static final int OPTION_SAVE = 0;
	public static final int OPTION_CANCEL = 1;

	private Long poiID;
	private FormSpinner nodeTypeSpinner;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		generateForm( FormActivity.parseFileToString( this, "schema_poi.json" ) );
		FormSpinner catSpinner = (FormSpinner) lookupWidget( "category" );
		catSpinner.setToggleHandler( new MyToggleHandler());
		addCategoryTypeSelection( catSpinner );

		nodeTypeSpinner = (FormSpinner) lookupWidget( "type" );

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

		// check if logged in
		UserCredentials userCredentials = new UserCredentials(this);

		if (userCredentials.isLoggedIn()) {

			JSONObject jo;
			jo = save();
			ContentValues values = new ContentValues();
			try {				
				values.put(Wheelmap.POIs.NAME, jo.get("name").toString());

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

			} catch (JSONException e) {
				Log.v( TAG, "Error with makemachine" + e.getMessage());
			}

			Uri poiUri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI, String.valueOf( poiID));
			this.getContentResolver().update(poiUri, values, "", null);

			final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, SyncService.class);
			intent.putExtra(SyncService.EXTRA_WHAT, SyncService.WHAT_UPDATE_SERVER );
			startService(intent);
		} else
		{
			// start login activity
			startActivity(new Intent(this, LoginActivity.class));		
		}
	}


	private void load() throws JSONException {

		JSONObject jo = new JSONObject();

		// Use the ContentUris method to produce the base URI for the contact with _ID == 23.
		Uri poiUri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI, Long.toString(poiID));

		// Then query for this specific record:
		Cursor cur = managedQuery(poiUri, null, null, null, null);


		if (cur.moveToFirst()) {		
			jo.put("name",POIHelper.getName(cur));
			jo.put("street",POIHelper.getStreet(cur));
			jo.put("postcode",POIHelper.getPostcode(cur));
			jo.put("city",POIHelper.getCity(cur));
			jo.put("website",POIHelper.getWebsite(cur));
			jo.put("wheelchair",POIHelper.getWheelchair(cur).getId());
			jo.put("phone",POIHelper.getPhone(cur));
			jo.put("comment",POIHelper.getComment(cur));
			jo.put( "type", POIHelper.getNodeTypeId( cur ));
			jo.put( "category", POIHelper.getCategoryId( cur ));
			cur.close();

		}
		populate(jo.toString());
	}

	private static class NodeTypeComparator implements Comparator<NodeType> {

		@Override
		public int compare(NodeType nt1, NodeType nt2) {
			return nt1.localizedName.compareTo( nt2.localizedName );
		}
	}

	private void addCategoryTypeSelection( FormSpinner spinner ) {
		JSONObject options = new JSONObject();
		List<Category> categories = SupportManager.get().getCategoryList();
		for( Category cat: categories ) {
			try {
				options.put( Integer.toString( cat.id ), cat.localizedName);
			} catch (JSONException e) {
				Log.v(TAG, e.getMessage());
			}
		}
		spinner.fillAdapter( options );
	}

	private void addNodeTypeSelection( FormSpinner spinner, int categoryId ) {
		JSONObject options = new JSONObject();
		List<NodeType> nodeTypes = SupportManager.get().getNodeTypeListByCategory( categoryId );
		Collections.sort( nodeTypes, new NodeTypeComparator());
		for( NodeType type: nodeTypes ) {
			try {
				options.put( Integer.toString(type.id), type.localizedName );
			} catch (JSONException e) {
				Log.v(TAG, e.getMessage());
			}
		}
		spinner.fillAdapter( options );
	}

	private class MyToggleHandler extends FormWidgetToggleHandler {
		public void toggle( FormWidget widget ) {
			super.toggle( widget );
			int categoryId = Integer.valueOf( widget.getValue());
			Log.d( TAG, "MyToggleHandler: widget.getValue = " + widget.getValue());
			addNodeTypeSelection( nodeTypeSpinner, categoryId );
			nodeTypeSpinner.setValue( Integer.toString( 0 ));
		}
	}
}	