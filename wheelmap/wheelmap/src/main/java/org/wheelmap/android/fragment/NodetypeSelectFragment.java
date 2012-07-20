package org.wheelmap.android.fragment;

import java.util.ArrayList;

import org.wheelmap.android.fragment.EditPositionFragment.OnEditPositionListener;
import org.wheelmap.android.model.CategoryNodeTypesAdapter;
import org.wheelmap.android.model.CategoryOrNodeType;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.online.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

public class NodetypeSelectFragment extends SherlockListFragment {
	public static final String TAG = NodetypeSelectFragment.class
			.getSimpleName();
	private int mNodeTypeSelected;
	private CheckedTextView oldCheckedView;

	private OnNodetypeSelectListener mListener;

	public interface OnNodetypeSelectListener {
		public void onSelect(int nodetype);
	}

	public static NodetypeSelectFragment newInstance(int nodetype) {
		Bundle b = new Bundle();
		b.putInt(Extra.NODETYPE, nodetype);

		NodetypeSelectFragment f = new NodetypeSelectFragment();
		f.setArguments(b);

		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof OnEditPositionListener) {
			mListener = (OnNodetypeSelectListener) activity;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mNodeTypeSelected = getArguments().getInt(Extra.NODETYPE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_nodetype_select,
				container, false);

		ArrayList<CategoryOrNodeType> types = CategoryOrNodeType
				.createTypesList(getActivity(), false);
		setListAdapter(new PickOnlyNodeTypesAdapter(getActivity(), types));

		return view;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		CategoryOrNodeType item = (CategoryOrNodeType) l.getAdapter().getItem(
				position);
		switch (item.type) {
		case NODETYPE:
			mNodeTypeSelected = item.id;
			if (oldCheckedView != null)
				oldCheckedView.setChecked(false);
			CheckedTextView view = (CheckedTextView) v
					.findViewById(R.id.search_type);
			view.setChecked(true);
			oldCheckedView = view;

			if (mListener != null) {
				mListener.onSelect(mNodeTypeSelected);
			}
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
