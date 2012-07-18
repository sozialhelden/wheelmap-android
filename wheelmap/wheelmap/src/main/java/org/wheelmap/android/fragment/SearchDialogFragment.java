package org.wheelmap.android.fragment;

import java.util.ArrayList;

import org.wheelmap.android.model.CategoryNodeTypesAdapter;
import org.wheelmap.android.model.CategoryOrNodeType;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.SyncService;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.actionbarsherlock.app.SherlockDialogFragment;

import de.akquinet.android.androlog.Log;

public class SearchDialogFragment extends SherlockDialogFragment implements
		OnItemSelectedListener, OnClickListener {
	public final static String TAG = SearchDialogFragment.class.getSimpleName();
	public final static String ARGUMENT_SHOW_DISTANCE = "org.wheelmap.android.ARGUMENT_SHOW_DISTANCE";
	public final static String ARGUMENT_SHOW_MAP_HINT = "org.wheelmap.android.ARGUMENT_SHOW_MAP_HINT";

	public final static int PERFORM_SEARCH = 1;

	private EditText mKeywordText;

	private int mCategorySelected = -1;
	private int mNodeTypeSelected = -1;
	private float mDistance = -1;

	public interface OnSearchDialogListener {
		public void onSearch(Bundle bundle);
	}

	public final static SearchDialogFragment newInstance(boolean showDistance,
			boolean showMapHint) {
		SearchDialogFragment dialog = new SearchDialogFragment();
		Bundle b = new Bundle();

		b.putBoolean(ARGUMENT_SHOW_DISTANCE, showDistance);
		b.putBoolean(ARGUMENT_SHOW_MAP_HINT, showMapHint);
		dialog.setArguments(b);
		return dialog;

	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		HoloAlertDialogBuilder builder = new HoloAlertDialogBuilder(
				getActivity());
		builder.setTitle(R.string.title_search);
		builder.setIcon(R.drawable.ic_menu_search_wm_holo_light);
		builder.setNeutralButton(R.string.search_execute, this);

		View view = getActivity().getLayoutInflater().inflate(
				R.layout.fragment_dialog_search, null);
		builder.setView(view);
		bindViews(view);

		Dialog d = builder.create();
		return d;
	}

	private void bindViews(View v) {

		mKeywordText = (EditText) v.findViewById(R.id.search_keyword);

		Spinner categorySpinner = (Spinner) v
				.findViewById(R.id.search_spinner_categorie_nodetype);

		ArrayList<CategoryOrNodeType> searchTypes = CategoryOrNodeType
				.createTypesList(getActivity(), true);
		categorySpinner.setAdapter(new CategoryNodeTypesAdapter(getActivity(),
				searchTypes, CategoryNodeTypesAdapter.SEARCH_MODE));
		categorySpinner.setOnItemSelectedListener(this);

		Spinner distanceSpinner = (Spinner) v
				.findViewById(R.id.search_spinner_distance);

		MyCustomSpinnerAdapter distanceSpinnerAdapter = MyCustomSpinnerAdapter
				.createFromResource(getActivity(), R.array.distance_array,
						R.layout.simple_my_spinner_item);

		distanceSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		distanceSpinner.setAdapter(distanceSpinnerAdapter);
		distanceSpinner.setOnItemSelectedListener(this);
		distanceSpinner.setPromptId(R.string.search_distance);
		distanceSpinner.setSelection(3);

	}

	@Override
	public void onResume() {
		super.onResume();

		LinearLayout mapHintContainer = (LinearLayout) getDialog()
				.findViewById(R.id.search_map_hint);
		if (getArguments().getBoolean(ARGUMENT_SHOW_MAP_HINT))
			mapHintContainer.setVisibility(View.VISIBLE);

		LinearLayout distanceContainer = (LinearLayout) getDialog()
				.findViewById(R.id.search_spinner_distance_container);
		if (getArguments().getBoolean(ARGUMENT_SHOW_DISTANCE))
			distanceContainer.setVisibility(View.VISIBLE);
	}

	public void onItemSelected(AdapterView<?> adapterView, View view,
			int position, long id) {
		int viewId = adapterView.getId();

		switch (viewId) {
		case R.id.search_spinner_categorie_nodetype: {
			CategoryOrNodeType search = (CategoryOrNodeType) adapterView
					.getAdapter().getItem(position);
			switch (search.type) {
			case CATEGORY:
				mCategorySelected = search.id;
				break;
			case NODETYPE:
				mNodeTypeSelected = search.id;
				break;
			}
			break;
		}
		case R.id.search_spinner_distance: {
			String distance = (String) adapterView.getItemAtPosition(position);
			try {
				mDistance = Float.valueOf(distance);
			} catch (NumberFormatException e) {
				// ignore
			}
			break;
		}
		default:
			// noop
		}

	}

	public void onNothingSelected(AdapterView<?> parent) {

	}

	private static class MyCustomSpinnerAdapter extends
			ArrayAdapter<CharSequence> {

		public MyCustomSpinnerAdapter(Context context, int textViewResourceId,
				CharSequence[] strings) {
			super(context, textViewResourceId, strings);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			TextView text;

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.simple_my_spinner_item,
						parent, false);
			} else {
				view = convertView;
			}

			text = (TextView) view.findViewById(android.R.id.text1);
			text.setText(getItem(position));
			return view;
		}

		public static MyCustomSpinnerAdapter createFromResource(
				Context context, int textArrayResId, int textViewResId) {
			CharSequence[] strings = context.getResources().getTextArray(
					textArrayResId);
			return new MyCustomSpinnerAdapter(context, textViewResId, strings);
		}

	}

	private void sendSearchInstructions() {
		Bundle bundle = new Bundle();

		String keyword = mKeywordText.getText().toString();
		if (keyword.length() > 0)
			bundle.putString(SearchManager.QUERY, keyword);

		Log.d(TAG, "mCategory = " + mCategorySelected + " mNodeType = "
				+ mNodeTypeSelected);
		if (mCategorySelected != -1)
			bundle.putInt(SyncService.EXTRA_CATEGORY, mCategorySelected);
		else if (mNodeTypeSelected != -1)
			bundle.putInt(SyncService.EXTRA_NODETYPE, mNodeTypeSelected);

		if (mDistance != -1)
			bundle.putFloat(SyncService.EXTRA_DISTANCE_LIMIT, mDistance);

		OnSearchDialogListener listener = (OnSearchDialogListener) getTargetFragment();
		listener.onSearch(bundle);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		sendSearchInstructions();
		dismiss();
	}

}
