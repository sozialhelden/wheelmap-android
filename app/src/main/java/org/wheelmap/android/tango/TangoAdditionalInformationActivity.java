package org.wheelmap.android.tango;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.google.auto.value.AutoValue;

import org.wheelmap.android.activity.base.BaseActivity;
import org.wheelmap.android.model.api.MeasurementInfo;
import org.wheelmap.android.online.R;
import org.wheelmap.android.online.databinding.TangoAdditionalInformationActivityBinding;
import org.wheelmap.android.utils.Arguments;
import org.wheelmap.android.utils.ViewTool;

public class TangoAdditionalInformationActivity extends BaseActivity {

    private TangoAdditionalInformationActivityBinding binding;
    private Args args;

    public static Intent newIntent(Context context, MeasurementInfo metaData) {
        Intent intent = new Intent(context, TangoAdditionalInformationActivity.class);
        intent.putExtras(new AutoValue_TangoAdditionalInformationActivity_Args(metaData).toBundle());
        intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        args = Args.fromBundle(getIntent().getExtras());

        binding = TangoAdditionalInformationActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        int tintColor = ContextCompat.getColor(this, R.color.green_btn);
        ViewTool.setBackgroundTint(binding.saveBtn, tintColor);

        binding.skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MeasurementInfo info = args.metaData()
                        .withDescription("");
                openUploadActivity(info);

            }
        });

        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MeasurementInfo info = args.metaData()
                        .withDescription(binding.commentEditText.getText().toString().trim());
                openUploadActivity(info);
            }
        });

        binding.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void openUploadActivity(MeasurementInfo info) {
        MeasurementUploadManager.getInstance().getExecutor().uploadMetaData(info);

        Intent intent = TangoUploadActivity.newIntent(TangoAdditionalInformationActivity.this);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in_medium, 0);
        finish();
    }

    @AutoValue
    static abstract class Args extends Arguments {
        abstract MeasurementInfo metaData();
    }

}
