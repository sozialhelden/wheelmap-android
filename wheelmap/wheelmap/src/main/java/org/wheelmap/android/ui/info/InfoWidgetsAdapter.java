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
package org.wheelmap.android.ui.info;

import java.util.List;

import org.wheelmap.android.online.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


class InfoSimpleView extends LinearLayout {

	private TextView title;
	protected TextView first;
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
		setBackgroundDrawable( getResources().getDrawable( R.drawable.list_item_background ));

		title = (TextView) findViewById(R.id.info_activity_title);
		first = (TextView) findViewById(R.id.info_activity_first_line);

		if (title != null)
			title.setText(info.getTitle());
		if (first != null)
			first.setText(info.getText());

	}
}

class InfoSimpleViewTwoLines extends InfoSimpleView {

	protected TextView second;

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



class InfoSimpleViewTwoUrls extends InfoSimpleViewTwoLines {

	public InfoSimpleViewTwoUrls(Context context, Info info ) {
		super(context, info);
	}

	@Override	
	protected int getLayout() {
		return R.layout.info_simple_two_urls;
	}

	public void onFirstLinkClick(View V) {
		Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://fiwio.com"));
		getContext().startActivity(intent);
	}

	public void onSecondLinkClick(View V) {
		Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://harakalovci.net"));
		getContext().startActivity(intent);
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
	public boolean isEnabled(int position) {
		if (infoList.get(position).getInfoType() == InfoTypes.WITH_TWO_LINKS ||
				( infoList.get(position).getUrl() != null &&
				infoList.get(position).getUrl().length() == 0))
			return false;
		else
			return true;
	}

	private OnClickListener mOnFirstClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
           	Info info = (Info) v.getTag();
            Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(info.getUrl()));
			context.startActivity(intent);
            }
    };

    private OnClickListener mOnSecondClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
         	Info info = (Info) v.getTag();
            Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(info.getSecondUrl()));
			context.startActivity(intent);
       }
    };

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Info info = infoList.get(position);
		switch (info.getInfoType()) {
		case SIMPLE_TEXT:
			return new InfoSimpleView(this.context, info );
		case DOUBLE_TEXT:
			return new InfoSimpleViewTwoLines(this.context, info );
		case WITH_TWO_LINKS:
			InfoSimpleViewTwoUrls result = new InfoSimpleViewTwoUrls(this.context, info );
			result.first.setOnClickListener(mOnFirstClickListener);
			result.second.setOnClickListener(mOnSecondClickListener);
			result.first.setTag(info);
			result.second.setTag(info);
			return result;
		case NEXT_ACTIVITY:
			return new InfoSimpleViewActivity(this.context, info );
		case WITH_IMAGE:
			return new InfoSimpleViewImage(this.context, info );
		default:
			return new InfoSimpleView(this.context, info );
		}
	}
}
