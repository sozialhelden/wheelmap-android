package org.wheelmap.android.ui.info;

import java.util.List;

import org.wheelmap.android.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

class InfoSimpleView extends LinearLayout {

	private TextView title;
	private TextView first;
	private Info info;

	public InfoSimpleView(Context context, Info info ) {
		super(context);
		this.info = info;
		this.initComponent(context);
	}

	public InfoSimpleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.initComponent(context);
	}


	private void initComponent(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.info_simple, null, false);
		this.addView(v);

		title = (TextView) findViewById(R.id.info_activity_title);
		first = (TextView) findViewById(R.id.info_activity_first_line);

		title.setText(info.getTitle());
		first.setText(info.getText());

	}
}

public class InfoWidgetsAdapter extends BaseAdapter {

	private Context context;
	private List<Info> infoList;

	public InfoWidgetsAdapter(Context context, List<Info> infoList ) { 
		this.infoList = infoList;
		this.context = context;
	}

	@Override
	public int getCount() {
		return infoList.size();
	}

	@Override
	public Object getItem(int position) {
		return infoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Info info = infoList.get(position);
		switch (info.getInfoType()) {
		case SIMPLE_TEXT:
			return new InfoSimpleView(this.context, info );
		case DOUBLE_TEXT:
			return new InfoSimpleView(this.context, info );
		case NEXT_ACTIVITY:
			return new InfoSimpleView(this.context, info );
		case WITH_IMAGE:
			return new InfoSimpleView(this.context, info );
		default:
			return new InfoSimpleView(this.context, info );
		}


	}

}
