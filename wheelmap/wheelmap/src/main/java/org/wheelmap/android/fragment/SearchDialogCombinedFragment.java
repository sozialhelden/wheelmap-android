package org.wheelmap.android.fragment;

import org.holoeverywhere.LayoutInflater;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.online.R;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class SearchDialogCombinedFragment extends SearchDialogFragment
        implements OnCheckedChangeListener {

    WheelmapApp app;

    public final static SearchDialogCombinedFragment newInstance() {
        SearchDialogCombinedFragment f = new SearchDialogCombinedFragment();
        Bundle b = new Bundle();

        b.putBoolean(Extra.SHOW_DISTANCE, true);
        b.putBoolean(Extra.SHOW_MAP_HINT, true);
        f.setArguments(b);
        return f;
    }

    protected View createView() {
        LayoutInflater inflater = LayoutInflater.from(getSupportActivity());
        View v = inflater.inflate(R.layout.fragment_dialog_search_combined, null);
        app = (WheelmapApp) this.getActivity().getApplicationContext();

        v.findViewById(R.id.button_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                app.setSearching(true);

                sendSearchInstructions();
                dismiss();
            }
        });

        return v;
    }

    protected void bindViews(final View v) {
        super.bindViews(v);

        // TODO: hier radio button handling einbauen

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int id) {

    }
}