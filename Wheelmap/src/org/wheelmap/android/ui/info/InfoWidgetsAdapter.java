package org.wheelmap.android.ui.info;

import java.util.List;

import org.wheelmap.android.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


class InfoSimpleView extends LinearLayout {

	private TextView title;
	private TextView first;
	protected Info info;

	public InfoSimpleView(Context context, Info info ) {
		super(context);
		this.info = info;
		this.initComponent(context);
	}

	protected int getLayout() {
		return R.layout.info_simple;
	}

	public InfoSimpleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.initComponent(context);
	}


	protected void initComponent(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
        // inflating of partial layout ignores layout_widht and layout_height attributes
		LinearLayout.LayoutParams parametri = new  LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		View v = inflater.inflate(getLayout(), null, false);
		this.addView(v, parametri);

		title = (TextView) findViewById(R.id.info_activity_title);
		first = (TextView) findViewById(R.id.info_activity_first_line);

		if (title != null)
			title.setText(info.getTitle());
		if (first != null)
			first.setText(info.getText());

	}
}

class InfoSimpleViewTwoLines extends InfoSimpleView {

	private TextView second;

	public InfoSimpleViewTwoLines(Context context, Info info ) {
		super(context, info);
	}

	@Override	
	protected int getLayout() {
		return R.layout.info_simple_two_lines;
	}

	@Override
	protected void initComponent(Context context) {
		super.initComponent(context);
		second = (TextView) findViewById(R.id.info_activity_second_line);
		if (second != null)
			second.setText(info.getSecondText());
	}
}

class InfoSimpleViewActivity extends InfoSimpleView {

	public InfoSimpleViewActivity(Context context, Info info ) {
		super(context, info);
	}

	@Override	
	protected int getLayout() {
		return R.layout.info_simple_activity;
	}
}

class InfoSimpleViewImage extends InfoSimpleView {

	private ImageView image;

	public InfoSimpleViewImage(Context context, Info info ) {
		super(context, info);
	}

	@Override	
	protected int getLayout() {
		return R.layout.info_simple_image;
	}

	@Override
	protected void initComponent(Context context) {
		super.initComponent(context);
		image = (ImageView) findViewById(R.id.info_activity_image);
		if (image != null)
			image.setImageResource(info.getText());
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
			return new InfoSimpleViewTwoLines(this.context, info );
		case NEXT_ACTIVITY:
			return new InfoSimpleViewActivity(this.context, info );
		case WITH_IMAGE:
			return new InfoSimpleViewImage(this.context, info );
		default:
			return new InfoSimpleView(this.context, info );
		}


	}

}
