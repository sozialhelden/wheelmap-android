package org.wheelmap.android.tango;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.google.auto.value.AutoValue;

import org.wheelmap.android.activity.base.BaseActivity;
import org.wheelmap.android.online.R;
import org.wheelmap.android.online.databinding.TangoTutorialActivityBinding;
import org.wheelmap.android.tango.mode.Mode;
import org.wheelmap.android.utils.Arguments;
import org.wheelmap.android.utils.ViewTool;

public class TangoTutorialActivity extends BaseActivity {

    private TangoTutorialActivityBinding binding;

    public static Intent newIntent(Context context, Mode mode) {
        Intent intent = new Intent(context, TangoTutorialActivity.class);
        intent.putExtras(new AutoValue_TangoTutorialActivity_Args(mode).toBundle());
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = TangoTutorialActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Args args = Args.fromBundle(getIntent().getExtras());

        Mode mode = args.mode();
        binding.title.setText(mode.tutorialTitle());
        binding.text.setText(mode.tutorialText());
        binding.image.setImageResource(mode.tutorialImage());

        int tintColor = ContextCompat.getColor(this, R.color.green_btn);
        ViewTool.setBackgroundTint(binding.startBtn, tintColor);

        View.OnClickListener finishClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        };

        binding.startBtn.setOnClickListener(finishClickListener);
        binding.closeBtn.setOnClickListener(finishClickListener);

    }

    @AutoValue
    abstract static class Args extends Arguments {
        public abstract Mode mode();
    }

}
