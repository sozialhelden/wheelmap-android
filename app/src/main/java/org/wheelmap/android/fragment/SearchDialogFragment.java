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

import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.model.CategoryOrNodeType;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.online.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.AdapterView.OnItemSelectedListener;

import de.akquinet.android.androlog.Log;

public class SearchDialogFragment extends DialogFragment implements
        OnItemSelectedListener, OnClickListener, OnEditorActionListener {

    public final static String TAG = SearchDialogFragment.class.getSimpleName();

    private EditText mKeywordText;

    private int mCategorySelected = Extra.UNKNOWN;

    private int mNodeTypeSelected = Extra.UNKNOWN;

    private float mDistance = Extra.UNKNOWN;

    private boolean mEnableBoundingBoxSearch = false;

    private Spinner mCategorySpinner;

    private Spinner mDistanceSpinner;

    public interface OnSearchDialogListener {

        public void onSearch(Bundle bundle);
    }

    public final static SearchDialogFragment newInstance(boolean showDistance,
            boolean showMapHint) {
        SearchDialogFragment dialog = new SearchDialogFragment();
        Bundle b = new Bundle();

        b.putBoolean(Extra.SHOW_DISTANCE, showDistance);
        b.putBoolean(Extra.SHOW_MAP_HINT, showMapHint);
        dialog.setArguments(b);
        return dialog;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = createView();
        builder.setView(view);
        bindViews(view);

        Dialog d = builder.create();
        return d;
    }

    protected View createView() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.fragment_dialog_search_combined, null);

        return v;
    }

    @SuppressWarnings("unchecked")
    protected void bindViews(View v) {

        mKeywordText = (EditText) v.findViewById(R.id.search_keyword);
        mKeywordText.setOnEditorActionListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private View mapHintContainer;

    private View distanceContainer;


    protected void setSearchMode(boolean enableBoundingBoxSearch) {
        Log.d(TAG, "enableBoundingBoxSearch = " + enableBoundingBoxSearch);

        mEnableBoundingBoxSearch = enableBoundingBoxSearch;
        mapHintContainer.setEnabled(mEnableBoundingBoxSearch);
        distanceContainer.setEnabled(!mEnableBoundingBoxSearch);

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view,
            int position, long id) {

        if (adapterView == mCategorySpinner) {
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
        } else if (adapterView == mDistanceSpinner) {
            String distance = (String) adapterView.getItemAtPosition(position);
            try {
                mDistance = Float.valueOf(distance);
            } catch (NumberFormatException e) {
                mDistance = Extra.UNKNOWN;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    protected void sendSearchInstructions() {
        Bundle b = createSearchBundle();

        WheelmapApp app = (WheelmapApp) this.getActivity().getApplicationContext();
        app.setSaved(true);

        OnSearchDialogListener listener = (OnSearchDialogListener) getTargetFragment();
        listener.onSearch(b);
    }

    protected Bundle createSearchBundle() {
        Bundle bundle = new Bundle();

        String keyword = mKeywordText.getText().toString();
        if (keyword.length() > 0) {
            bundle.putString(SearchManager.QUERY, keyword);
        }

        return bundle;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        sendSearchInstructions();
        dismiss();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            sendSearchInstructions();
            dismiss();
            return true;
        }

        return false;
    }

}
