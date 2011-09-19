package org.wheelmap.android.ui;

import org.wheelmap.android.R;
import org.wheelmap.android.model.CategorySelectCursorAdapter;
import org.wheelmap.android.model.Support;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class CategorySelectActivity extends ListActivity {

	private final static String TAG = "category";
	private Uri mUri = Support.CategoriesContent.CONTENT_URI;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_category_select);

		Cursor cursor = managedQuery(mUri, Support.CategoriesContent.PROJECTION,
				null, null, Support.CategoriesContent.DEFAULT_SORT_ORDER );
		startManagingCursor(cursor);

		CategorySelectCursorAdapter adapter = new CategorySelectCursorAdapter( this, cursor );
		setListAdapter(adapter);

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Cursor cursor = (Cursor) l.getItemAtPosition(position);
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
