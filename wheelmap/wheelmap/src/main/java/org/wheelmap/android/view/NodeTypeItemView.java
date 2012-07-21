package org.wheelmap.android.view;

import org.wheelmap.android.online.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;

public class NodeTypeItemView extends FrameLayout implements TypeItemView {
	private CheckedTextView mText;

	public NodeTypeItemView(Context context) {
		super(context);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.search_nodetype, this, true);
		mText = (CheckedTextView) findViewById(R.id.text);
	}

	public void setText(String text) {
		mText.setText(text);
	}
}
