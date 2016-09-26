package org.wheelmap.android.tango;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.google.auto.value.AutoValue;

import org.wheelmap.android.activity.base.BaseActivity;
import org.wheelmap.android.online.R;
import org.wheelmap.android.online.databinding.TangoAdditionalInformationActivityBinding;
import org.wheelmap.android.utils.Arguments;
import org.wheelmap.android.utils.ViewTool;

public class TangoAdditionalInformationActivity extends BaseActivity {

    private TangoAdditionalInformationActivityBinding binding;

    public static Intent newIntent(Context context, Uri fileUri) {
        Intent intent = new Intent(context, TangoAdditionalInformationActivity.class);
        intent.putExtras(new AutoValue_TangoConfirmPictureActivity_Args(fileUri).toBundle());
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = TangoAdditionalInformationActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        int tintColor = ContextCompat.getColor(this, R.color.green_btn);
        ViewTool.setBackgroundTint(binding.saveBtn, tintColor);

        View.OnClickListener okClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "No Implemeted yet", Toast.LENGTH_SHORT).show();
            }
        };

        binding.saveBtn.setOnClickListener(okClickListener);
        binding.skipBtn.setOnClickListener(okClickListener);

        binding.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @AutoValue
    static abstract class Args extends Arguments {
        abstract Uri uri();
    }

}
