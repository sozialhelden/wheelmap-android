package org.wheelmap.android.actionbar;

import org.wheelmap.android.online.R;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.actionbarsherlock.widget.ShareActionProvider;

public class DirectionsActionProvider extends ShareActionProvider {

	public DirectionsActionProvider(Context context) {
		super(context);
	}

	@Override
	public View onCreateActionView() {
		View view = super.onCreateActionView();
		ImageView image = (ImageView) view.findViewById(R.id.abs__image);
		image.setImageResource(R.drawable.ic_menu_directions);

		return view;
	}

}
