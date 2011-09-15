package org.wheelmap.android.ui;

import org.wheelmap.android.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class POIsListItemView extends RelativeLayout {

	private  TextView poiCategory;
	private TextView poiName;
	private TextView poiDistance;
	private ImageView poiIcon;

	/**
	 * constructor creates a new line item of list
	 * @param context 
	 */
	public POIsListItemView(Context context) {
		super(context);
		// inflate rating
		LayoutInflater inflater = (LayoutInflater) context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.pois_list_item, this, true);

		poiName = (TextView) findViewById(R.id.list_item_place_name);
		poiCategory = (TextView) findViewById(R.id.list_item_category);
		poiDistance = (TextView) findViewById(R.id.list_item_distance);
		poiIcon = (ImageView)findViewById(R.id.place_type_icon);
	}

	/**
	 * Convenience method to set the properties of POI
	 */
	public void setName(String name) {
		poiName.setText(name);
	}

	public void setCategory(String name) {
		poiCategory.setText(name);
	}

	public void setDistance(String name) {
		poiDistance.setText(name);
	}

	public void setIcon(Drawable drawable) {
		poiIcon.setImageDrawable( drawable );
	}
}
