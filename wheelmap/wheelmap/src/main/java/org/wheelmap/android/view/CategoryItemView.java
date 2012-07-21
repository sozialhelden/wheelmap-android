package org.wheelmap.android.view;

import org.wheelmap.android.adapter.TypesAdapter;
import org.wheelmap.android.online.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

public class CategoryItemView extends FrameLayout implements TypeItemView {
	private TextView mText;

	public CategoryItemView(Context context, int type) {
		super(context);
		LayoutInflater inflater = LayoutInflater.from(context);
		int resource;
		if (type == TypesAdapter.SEARCH_MODE)
			resource = R.layout.search_category;
		else
			resource = R.layout.search_category_noselect;

		inflater.inflate(resource, this, true);
		mText = (TextView) findViewById(R.id.text);
	}

	public void setText(String text) {
		mText.setText(text);
	}
}
