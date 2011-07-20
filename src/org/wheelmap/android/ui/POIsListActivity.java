package org.wheelmap.android.ui;

import org.wheelmap.android.R;
import org.wheelmap.android.model.POIsCursorAdapter;
import org.wheelmap.android.model.Wheelmap;

import android.app.ListActivity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


public class POIsListActivity extends ListActivity {

	private Cursor mCursor;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		// Run query
		Uri uri = Wheelmap.POIs.CONTENT_URI;

		mCursor = managedQuery(uri, Wheelmap.POIs.PROJECTION, null, null, Wheelmap.POIs.DEFAULT_SORT_ORDER);
		startManagingCursor(mCursor);

		POIsCursorAdapter adapter = new POIsCursorAdapter(this, mCursor);
		setListAdapter(adapter);

		getListView().setTextFilterEnabled(true);
	}

	public void onRefreshClick(View v) {
		Toast.makeText(this, "Refreshing..", Toast.LENGTH_SHORT).show();
	}

	public void onSearchClick(View v) {
		Toast.makeText(this, "Searching..", Toast.LENGTH_SHORT).show();
	}

}
