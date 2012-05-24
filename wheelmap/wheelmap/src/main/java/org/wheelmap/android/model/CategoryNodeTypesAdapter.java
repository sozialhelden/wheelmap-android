package org.wheelmap.android.model;

import java.util.ArrayList;

import org.wheelmap.android.online.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

interface ItemViewText {
	public void setText(String text);
}

class NodeTypeSearchItemView extends FrameLayout implements ItemViewText {
	private CheckedTextView mText;

	public NodeTypeSearchItemView(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.search_nodetype, this, true);

		mText = (CheckedTextView) findViewById(R.id.search_type);
	}

	public void setText(String text) {
		mText.setText(text);
	}

}

class CategorySearchItemView extends FrameLayout implements ItemViewText {
	private TextView mText;

	public CategorySearchItemView(Context context, int type) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		int resource;
		if (type == CategoryNodeTypesAdapter.SEARCH_MODE)
			resource = R.layout.search_category;
		else
			resource = R.layout.search_category_noselect;

		inflater.inflate(resource, this, true);
		mText = (TextView) findViewById(R.id.search_type);
	}

	public void setText(String text) {
		mText.setText(text);
	}
}

public class CategoryNodeTypesAdapter extends BaseAdapter implements SpinnerAdapter {
	public static final int SEARCH_MODE = 0;
	public static final int SELECT_MODE = 1;

	private int mType;

	private Context mContext;
	private ArrayList<CategoryOrNodeType> items;

	public CategoryNodeTypesAdapter(Context context,
			ArrayList<CategoryOrNodeType> items, int type) {
		mContext = context;
		this.items = items;
		mType = type;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {		
		return items.get( position ).type.ordinal();
	}
	
	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (mType == SELECT_MODE)
			return getDropDownView(position, convertView, parent);
		else
			return getSelectedItemView(position, convertView, parent);
	}

	private View getSelectedItemView(int position, View convertView,
			ViewGroup parent) {
		View view;
		TextView text;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.simple_my_spinner_item, parent,
					false);
		} else {
			view = convertView;
		}

		text = (TextView) view.findViewById(android.R.id.text1);
		text.setText(items.get(position).text);
		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View useView;
		CategoryOrNodeType item = items.get(position);
		
		if (item.type == CategoryOrNodeType.Types.CATEGORY
				|| item.type == CategoryOrNodeType.Types.NO_SELECTION)
			if ( convertView instanceof CategorySearchItemView )
				useView = convertView;
			else
				useView = new CategorySearchItemView(mContext, mType);
		else
			if ( convertView instanceof NodeTypeSearchItemView )
				useView = convertView;
			else
				useView = new NodeTypeSearchItemView(mContext);

		((ItemViewText) useView).setText(item.text);

		return useView;
	}
}
