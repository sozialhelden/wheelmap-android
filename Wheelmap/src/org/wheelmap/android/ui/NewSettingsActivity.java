package org.wheelmap.android.ui;

import org.wheelmap.android.R;
import org.wheelmap.android.model.CategorySelectCursorAdapter;
import org.wheelmap.android.model.MergeAdapter;
import org.wheelmap.android.model.Support;
import org.wheelmap.android.model.WheelchairStateAdapter;
import org.wheelmap.android.model.WheelchairStateAdapter.WheelchairStateItem;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;


public class NewSettingsActivity extends ListActivity {

	private final static String TAG = "category";
	private Uri mUri = Support.CategoriesContent.CONTENT_URI;
	private SharedPreferences mPrefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_category_select);
		
		mPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		Cursor cursor = managedQuery(mUri, Support.CategoriesContent.PROJECTION,
				null, null, Support.CategoriesContent.DEFAULT_SORT_ORDER );
		startManagingCursor(cursor);

		CategorySelectCursorAdapter adapterCatList = new CategorySelectCursorAdapter( this, cursor );		
		View catTitleView = createCatTitle();
		WheelchairStateAdapter adapterWSList = new WheelchairStateAdapter( this );
		
		View wsTitleView = createWheelStateTitle();
		
		MergeAdapter adapter = new MergeAdapter();
		adapter.addView( wsTitleView );
		adapter.addAdapter( adapterWSList);
		adapter.addView( catTitleView );
		adapter.addAdapter( adapterCatList );
		
		this.setListAdapter( adapter );
	}
	
	private View createWheelStateTitle() {
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		LinearLayout layout = (LinearLayout) inflater.inflate( R.layout.settings_wheelstate_item_title, null);
		return layout;
	}
	
	private View createCatTitle() {
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		LinearLayout layout = (LinearLayout) inflater.inflate( R.layout.settings_category_item_title, null);
		return layout;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		MergeAdapter adapter = (MergeAdapter) l.getAdapter();
		Object item = l.getItemAtPosition(position);
		if ( item instanceof WheelchairStateItem ) {
			clickWheelStateItem( (WheelchairStateItem) item, adapter );
			
		}
		if ( item instanceof Cursor ) {
			clickCategorieItem( (Cursor) item );
		}
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
	
}
