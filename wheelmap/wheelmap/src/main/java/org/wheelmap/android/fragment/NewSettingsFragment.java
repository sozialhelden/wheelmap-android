package org.wheelmap.android.fragment;

import org.wheelmap.android.adapter.CategorySelectCursorAdapter;
import org.wheelmap.android.adapter.MergeAdapter;
import org.wheelmap.android.adapter.WheelchairStateSelectAdapter;
import org.wheelmap.android.manager.SupportManager.WheelchairAttributes;
import org.wheelmap.android.model.Support;
import org.wheelmap.android.model.UserCredentials;
import org.wheelmap.android.online.R;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

import de.akquinet.android.androlog.Log;

public class NewSettingsFragment extends SherlockListFragment implements
		LoaderCallbacks<Cursor> {
	private final static String TAG = NewSettingsFragment.class.getSimpleName();
	private static final int LOADER_ID_LIST = 0;
	private Uri mUri = Support.CategoriesContent.CONTENT_URI;
	private SharedPreferences mPrefs;

	private CategorySelectCursorAdapter mAdapterCatList;
	private MergeAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mAdapterCatList = new CategorySelectCursorAdapter(getActivity(), null,
				false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_settings_select, container,
				false);

		WheelchairStateSelectAdapter adapterWSList = new WheelchairStateSelectAdapter(
				getActivity());

		mAdapter = new MergeAdapter();
		mAdapter.addView(createSectionTitle(inflater,
				R.string.settings_wheelchair_state));
		mAdapter.addAdapter(adapterWSList);
		mAdapter.addView(createSectionTitle(inflater,
				R.string.settings_category_filter));
		mAdapter.addAdapter(mAdapterCatList);
		mAdapter.addView(createSectionTitle(inflater,
				R.string.settings_login_information));
		mAdapter.addAdapter(new DeleteLoginAdapter(inflater));

		return v;
	}

	private View createSectionTitle(LayoutInflater inflater, int textId) {
		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.item_list_sectiontitle, null);
		TextView title = (TextView) layout.findViewById(R.id.text);
		title.setText(getResources().getString(textId));
		return layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(LOADER_ID_LIST, null, this);
		setListAdapter(mAdapter);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		MergeAdapter adapter = (MergeAdapter) l.getAdapter();
		Object item = l.getItemAtPosition(position);
		if (item instanceof WheelchairAttributes) {
			clickWheelStateItem((WheelchairAttributes) item, adapter);
		} else if (item instanceof Cursor) {
			clickCategorieItem((Cursor) item);
		} else if (item instanceof String
				&& ((String) item).equals("deletelogin")) {
			clickDeleteLoginData();
		}
	}

	private void clickWheelStateItem(WheelchairAttributes item,
			MergeAdapter adapter) {
		boolean isSet = mPrefs.getBoolean(item.prefsKey, true);
		boolean toggleSet = !isSet;
		mPrefs.edit().putBoolean(item.prefsKey, toggleSet).commit();
		adapter.notifyDataSetChanged();
	}

	private void clickCategorieItem(Cursor cursor) {
		int catId = Support.CategoriesContent.getCategoryId(cursor);
		boolean selected = Support.CategoriesContent.getSelected(cursor);

		ContentResolver resolver = getActivity().getContentResolver();
		ContentValues values = new ContentValues();
		if (selected) {
			values.put(Support.CategoriesContent.SELECTED,
					Support.CategoriesContent.SELECTED_NO);
		} else {
			values.put(Support.CategoriesContent.SELECTED,
					Support.CategoriesContent.SELECTED_YES);
		}

		String whereClause = "( " + Support.CategoriesContent.CATEGORY_ID
				+ " = ?)";
		String[] whereValues = new String[] { Integer.toString(catId) };
		resolver.update(mUri, values, whereClause, whereValues);

		Log.d(TAG,
				"Name = " + Support.CategoriesContent.getLocalizedName(cursor));
	}

	private void clickDeleteLoginData() {
		UserCredentials credentials = new UserCredentials(getActivity());
		credentials.logout();
	}

	private class DeleteLoginAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public DeleteLoginAdapter(LayoutInflater inflater) {
			mInflater = inflater;
		}

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public Object getItem(int arg0) {
			return new String("deletelogin");
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public boolean isEnabled(int position) {
			UserCredentials credentials = new UserCredentials(getActivity()
					.getApplicationContext());
			if (credentials.isLoggedIn())
				return true;
			else
				return false;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout layout = (LinearLayout) mInflater.inflate(
					R.layout.item_settings_delete_logindata, null);

			return layout;
		}

	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(getActivity(), mUri,
				Support.CategoriesContent.PROJECTION, null, null,
				Support.CategoriesContent.DEFAULT_SORT_ORDER);

	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapterCatList.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

}
