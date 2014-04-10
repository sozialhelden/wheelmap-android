package org.wheelmap.android.fragment;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.RadioButton;
import org.holoeverywhere.widget.Spinner;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.mapping.node.Photos;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.online.R;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class SearchDialogCombinedFragment extends SearchDialogFragment
        implements OnCheckedChangeListener {

    EditText editText;

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

        v.findViewById(R.id.button_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSearchInstructions();
                dismiss();

            }
        });

        editText= (EditText) v.findViewById(R.id.search_keyword);
        editText.requestFocus();

        return v;
    }

    protected void bindViews(final View v) {
        super.bindViews(v);
        /*
        RadioGroup group = (RadioGroup) v
                .findViewById(R.id.radioGroupSearchMode);
        group.setOnCheckedChangeListener(this);*/

        // TODO: hier radio button handling einbauen
        /*

        ((RadioButton)v.findViewById(R.id.radioEnableDistance)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSearchMode(false);
                if(isChecked){
                    v.findViewById(R.id.search_spinner_distance).setEnabled(true);
                    ((RadioButton)v.findViewById(R.id.radioEnableBoundingBox)).setChecked(false);
                }
            }
        });

        ((RadioButton)v.findViewById(R.id.radioEnableBoundingBox)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        setSearchMode(true);
                        if(isChecked){
                            v.findViewById(R.id.search_spinner_distance).setEnabled(false);
                            ((RadioButton)v.findViewById(R.id.radioEnableDistance)).setChecked(false);
                        }
                    }
                });   */

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int id) {
        /*if (id == R.id.radioEnableDistance) {
            setSearchMode(false);
        } else if (id == R.id.radioEnableBoundingBox) {
            setSearchMode(true);
        }*/
    }
}