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
package org.wheelmap.android.ui;

import org.wheelmap.android.online.R;
import org.wheelmap.android.model.CategorySelectCursorAdapter;
import org.wheelmap.android.model.MergeAdapter;
import org.wheelmap.android.model.Support;
import org.wheelmap.android.model.UserCredentials;
import org.wheelmap.android.model.WheelchairStateAdapter;
import org.wheelmap.android.model.WheelchairStateAdapter.WheelchairStateItem;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;


public class NewSettingsActivity extends ListActivity implements OnClickListener {

	private final static String TAG = "settings";
	private Uri mUri = Support.CategoriesContent.CONTENT_URI;
	private SharedPreferences mPrefs;
	private LayoutInflater mInflater;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_select);
		
		mPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ImageView titleLogo = (ImageView) findViewById( R.id.title_logo );
		titleLogo.setOnClickListener( this );
		
		Cursor cursor = managedQuery(mUri, Support.CategoriesContent.PROJECTION,
				null, null, Support.CategoriesContent.DEFAULT_SORT_ORDER );
		startManagingCursor(cursor);

		CategorySelectCursorAdapter adapterCatList = new CategorySelectCursorAdapter( this, cursor );		
		WheelchairStateAdapter adapterWSList = new WheelchairStateAdapter( this );
	
		MergeAdapter adapter = new MergeAdapter();
		adapter.addView( createWheelStateTitle() );
		adapter.addAdapter( adapterWSList);
		adapter.addView( createBlackBar());
		adapter.addView( createCatTitle() );
		adapter.addAdapter( adapterCatList );
		adapter.addView( createBlackBar());
		adapter.addAdapter( createAdapterDeleteLogin());
		
		this.setListAdapter( adapter );
		
	}
	
	private View createWheelStateTitle() {
		LinearLayout layout = (LinearLayout) mInflater.inflate( R.layout.settings_wheelstate_item_title, null);
		return layout;
	}
	
	private View createCatTitle() {

		LinearLayout layout = (LinearLayout) mInflater.inflate( R.layout.settings_category_item_title, null);
		return layout;
	}
	
	private View createBlackBar() {
		LinearLayout layout = (LinearLayout) mInflater.inflate( R.layout.settings_black_item, null);
		return layout;
	}
	
	private DeleteLoginAdapter createAdapterDeleteLogin() {
		return new DeleteLoginAdapter();
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		finish();
		return true;
	}
		
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		MergeAdapter adapter = (MergeAdapter) l.getAdapter();
		Object item = l.getItemAtPosition(position);
		if ( item instanceof WheelchairStateItem ) {
			clickWheelStateItem( (WheelchairStateItem) item, adapter );	
		} else if ( item instanceof Cursor ) {
			clickCategorieItem( (Cursor) item );
		} else if ( item instanceof String && ((String)item).equals( "deletelogin")) {
			clickDeleteLoginData();
		}
	}
	
	@Override
	public void onClick(View v) {
		String packageInfo = "de.studiorutton.android.offlinemap";
		try {
			PackageInfo info = getApplicationContext().getPackageManager().getPackageInfo( packageInfo, PackageManager.GET_ACTIVITIES );
		} catch (NameNotFoundException e) {
			return;
		}
	
		Intent intent = new Intent();
		intent.setComponent(new ComponentName( packageInfo, packageInfo + ".ui.MapFileDownloadActivity"));
		startActivity(intent);
	}
	
	private void clickWheelStateItem( WheelchairStateItem item, MergeAdapter adapter ) {
		boolean isSet = mPrefs.getBoolean( item.prefsKey, true );
		boolean toggleSet = !isSet;
		mPrefs.edit().putBoolean( item.prefsKey, toggleSet ).commit();
		
		adapter.notifyDataSetChanged();
	}
	
	private void clickCategorieItem( Cursor cursor ) {
		int catId = Support.CategoriesContent.getCategoryId(cursor );
		boolean selected = Support.CategoriesContent.getSelected( cursor );
		
		ContentResolver resolver = getContentResolver();
		ContentValues values = new ContentValues();
		if ( selected ) {
			values.put( Support.CategoriesContent.SELECTED, Support.CategoriesContent.SELECTED_NO );
		} else {
			values.put( Support.CategoriesContent.SELECTED, Support.CategoriesContent.SELECTED_YES );
		}
		
		String whereClause = "( " + Support.CategoriesContent.CATEGORY_ID + " = ?)";
		String[] whereValues = new String[]{ Integer.toString(catId) }; 
		resolver.update( mUri, values, whereClause, whereValues );
		
		Log.d(TAG,  "Name = " + Support.CategoriesContent.getLocalizedName( cursor ));		
	}
	
	private void clickDeleteLoginData() {
		UserCredentials credentials = new UserCredentials( this );
		credentials.logout();
	}
	
	private class DeleteLoginAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public Object getItem(int arg0) {
			return new String( "deletelogin" );
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
		
		@Override
		public boolean isEnabled(int position) {
			UserCredentials credentials = new UserCredentials( NewSettingsActivity.this );
			if ( credentials.isLoggedIn())
				return true;
			else
				return false;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout layout = (LinearLayout) mInflater.inflate( R.layout.settings_delete_logindata, null );
			
			return layout;
		}
		
	}
	
}
