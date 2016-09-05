package org.wheelmap.android.tango;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import org.wheelmap.android.online.R;
import org.wheelmap.android.online.databinding.TangoModeSelectionViewBinding;

public class ModeSelectionView extends LinearLayout {

    private TangoModeSelectionViewBinding binding;

    public ModeSelectionView(Context context) {
        super(context);
        init();
    }

    public ModeSelectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ModeSelectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {

        binding = DataBindingUtil.inflate(
                LayoutInflater.from(getContext()),
                R.layout.tango_mode_selection_view,
                this,
                true
        );



    }
    
}
