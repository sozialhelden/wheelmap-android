package org.wheelmap.android.ui;

import java.util.ArrayList;

import org.wheelmap.android.online.R;
import org.wheelmap.android.model.CategoryNodeTypesAdapter;
import org.wheelmap.android.model.CategoryOrNodeType;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ListView;

public class NodeTypeSelectActivity extends ListActivity {

	public static final String EXTRA_NODETYPE = "org.wheelmap.android.EXTRA_NODETYPE";
	private int mNodeTypeSelected = -1;
	
	private CheckedTextView oldCheckedView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.activity_nodetype_select);

		ArrayList<CategoryOrNodeType> types = CategoryOrNodeType
				.createTypesList(this, false);
		setListAdapter(new PickOnlyNodeTypesAdapter(this, types));
		
		// Dont know how to set a checkbox to selected
		int nodeType;
		if ( getIntent().getExtras() != null)
			nodeType = getIntent().getExtras().getInt( EXTRA_NODETYPE );
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		CategoryOrNodeType item = (CategoryOrNodeType) l.getAdapter().getItem(
				position);
		switch (item.type) {
		case NODETYPE:
			mNodeTypeSelected = item.id;
			if ( oldCheckedView != null )
				oldCheckedView.setChecked( false );
			CheckedTextView view = (CheckedTextView) v.findViewById( R.id.search_type );
			view.setChecked( true );
			oldCheckedView = view;
			
			Intent intent = new Intent();
			intent.putExtra(EXTRA_NODETYPE, mNodeTypeSelected);
			setResult(RESULT_OK, intent);
			finish();
			break;
		default:
			//
		}
	}

	private static class PickOnlyNodeTypesAdapter extends
			CategoryNodeTypesAdapter {
		public PickOnlyNodeTypesAdapter(Context context,
				ArrayList<CategoryOrNodeType> items) {
			super(context, items, CategoryNodeTypesAdapter.SELECT_MODE);
		}

		@Override
		public boolean isEnabled(int position) {
			CategoryOrNodeType item = (CategoryOrNodeType) getItem(position);
			switch (item.type) {
			case CATEGORY:
				return false;
			case NODETYPE:
				return true;
			default:
				return false;
			}
		}
	}
}
