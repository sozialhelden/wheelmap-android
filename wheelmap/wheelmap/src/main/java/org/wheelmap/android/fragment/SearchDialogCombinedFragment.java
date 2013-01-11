package org.wheelmap.android.fragment;

import org.holoeverywhere.LayoutInflater;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.online.R;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class SearchDialogCombinedFragment extends SearchDialogFragment
		implements OnCheckedChangeListener {

	public final static SearchDialogCombinedFragment newInstance() {
		SearchDialogCombinedFragment f = new SearchDialogCombinedFragment();
		Bundle b = new Bundle();

		b.putBoolean(Extra.SHOW_DISTANCE, true);
		b.putBoolean(Extra.SHOW_MAP_HINT, true);
		f.setArguments(b);
		return f;
	}

	protected View createView() {
		return LayoutInflater.from(getActivity()).inflate(
				R.layout.fragment_dialog_search_combined, null);
	}

	protected void bindViews(View v) {
		super.bindViews(v);

		RadioGroup group = (RadioGroup) v
				.findViewById(R.id.radioGroupSearchMode);
		group.setOnCheckedChangeListener(this);

		// TODO: hier radio button handling einbauen
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int id) {
		if (id == R.id.radioEnableDistance)
			setSearchMode(false);
		else if (id == R.id.radioEnableBoundingBox)
			setSearchMode(true);
	}
}