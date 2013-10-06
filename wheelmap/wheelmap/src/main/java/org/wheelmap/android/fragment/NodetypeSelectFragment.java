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
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.ListFragment;
import org.holoeverywhere.widget.ListView;
import org.wheelmap.android.adapter.TypesAdapter;
import org.wheelmap.android.fragment.EditPositionFragment.OnEditPositionListener;
import org.wheelmap.android.model.CategoryOrNodeType;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.online.R;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import java.util.ArrayList;

public class NodetypeSelectFragment extends ListFragment {

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
        setListAdapter(new PickOnlyNodeTypesAdapter(getSupportActivity(), types));

        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        CategoryOrNodeType item = (CategoryOrNodeType) l.getAdapter().getItem(
                position);
        switch (item.type) {
            case NODETYPE:
                mNodeTypeSelected = item.id;
                // if (oldCheckedView != null)
                // oldCheckedView.setChecked(false);
                // CheckedTextView view = (CheckedTextView) v
                // .findViewById(R.id.search_type);
                // view.setChecked(true);
                // oldCheckedView = view;

                if (mListener != null) {
                    mListener.onSelect(mNodeTypeSelected);
                }
                break;
            default:
                //
        }
    }

    private static class PickOnlyNodeTypesAdapter extends TypesAdapter {

        public PickOnlyNodeTypesAdapter(Context context,
                ArrayList<CategoryOrNodeType> items) {
            super(context, items, TypesAdapter.SELECT_MODE);
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
