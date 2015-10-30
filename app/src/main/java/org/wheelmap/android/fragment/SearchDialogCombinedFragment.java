package org.wheelmap.android.fragment;

import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.online.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class SearchDialogCombinedFragment extends SearchDialogFragment
        implements OnCheckedChangeListener {

    WheelmapApp app;

    private String adress = null;

    public final static SearchDialogCombinedFragment newInstance() {
        SearchDialogCombinedFragment f = new SearchDialogCombinedFragment();
        Bundle b = new Bundle();

        b.putBoolean(Extra.SHOW_DISTANCE, true);
        b.putBoolean(Extra.SHOW_MAP_HINT, true);
        f.setArguments(b);
        return f;
    }

    protected View createView() {

        app = (WheelmapApp) this.getActivity().getApplicationContext();

        try{
            adress = app.getAddressString();
        }catch(Exception ex){
            // noop
        }

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.fragment_dialog_search_combined, null);


        if(adress != null){
            ((EditText)v.findViewById(R.id.search_keyword)).setText(adress);
            app.setAddressString(null);
        }

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