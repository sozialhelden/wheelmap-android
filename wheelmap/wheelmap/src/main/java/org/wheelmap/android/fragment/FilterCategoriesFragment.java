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
package org.wheelmap.android.fragment;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.ListFragment;
import org.holoeverywhere.widget.ListView;
import org.wheelmap.android.adapter.CategorySelectCursorAdapter;
import org.wheelmap.android.adapter.MergeAdapter;
import org.wheelmap.android.model.Support;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.akquinet.android.androlog.Log;

public class FilterCategoriesFragment extends ListFragment implements
        LoaderCallbacks<Cursor> {

    private final static String TAG = FilterCategoriesFragment.class.getSimpleName();

    private static final int LOADER_ID_LIST = 0;

    private Uri mUri = Support.CategoriesContent.CONTENT_URI;

    private SharedPreferences mPrefs;

    private CategorySelectCursorAdapter mAdapterCatList;

    private MergeAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getSupportActivity());
        mAdapterCatList = new CategorySelectCursorAdapter(getSupportActivity(), null,
                false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings_select, container,
                false);

        mAdapter = new MergeAdapter();

        mAdapter.addView(createSectionTitle(inflater,
                R.string.settings_category_filter));
        mAdapter.addAdapter(mAdapterCatList);

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
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        MergeAdapter adapter = (MergeAdapter) l.getAdapterSource();
        Object item = l.getItemAtPosition(position);
        if (item instanceof Cursor) {
            clickCategorieItem((Cursor) item);
        }
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

       // WheelmapApp.getCategoryChoosedPrefs().edit().putBoolean(catId+"",selected);

        String whereClause = "( " + Support.CategoriesContent.CATEGORY_ID
                + " = ?)";
        String[] whereValues = new String[]{Integer.toString(catId)};
        resolver.update(mUri, values, whereClause, whereValues);

        Log.d(TAG,
                "Name = " + Support.CategoriesContent.getLocalizedName(cursor));
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
