package org.wheelmap.android.tango;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.wheelmap.android.activity.base.BaseActivity;
import org.wheelmap.android.online.databinding.TangoTutorialActivityBinding;

public class TangoTutorialActivity extends BaseActivity {

    private TangoTutorialActivityBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = TangoTutorialActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


    }
}
