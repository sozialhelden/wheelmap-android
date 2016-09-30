package org.wheelmap.android.tango;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.auto.value.AutoValue;

import org.wheelmap.android.activity.base.BaseActivity;
import org.wheelmap.android.net.UploadTangoMeasurementExecutor;
import org.wheelmap.android.online.R;
import org.wheelmap.android.online.databinding.TangoAdditionalInformationActivityBinding;
import org.wheelmap.android.online.databinding.TangoUploadActivityBinding;
import org.wheelmap.android.utils.Arguments;
import org.wheelmap.android.utils.ViewTool;
import org.wheelmap.android.view.progress.ProgressCompleteView;
import org.wheelmap.android.view.progress.ProgressCompleteView.Status;

import rx.functions.Action1;

public class TangoUploadActivity extends BaseActivity {

    private TangoUploadActivityBinding binding;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, TangoUploadActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = TangoUploadActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int tintColor = ContextCompat.getColor(this, R.color.green_btn);
        ViewTool.setBackgroundTint(binding.btn, tintColor);

        View.OnClickListener okClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "No Implemeted yet", Toast.LENGTH_SHORT).show();
            }
        };

        binding.btn.setOnClickListener(okClickListener);

        binding.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        int color = ContextCompat.getColor(this, R.color.disabled_tint_color);
        binding.progress.setColor(Status.LOADING, color);
        binding.progress.setColor(Status.SUCCESS, color);
        binding.progress.setColor(Status.ERROR, color);

        setStatus(Status.LOADING);

        MeasurementUploadManager.getInstance().getExecutor().uploadReady().subscribe(new Action1<UploadTangoMeasurementExecutor.Status>() {
            @Override
            public void call(UploadTangoMeasurementExecutor.Status status) {
                switch (status) {
                    case ERROR:
                        setStatus(Status.ERROR);
                        break;
                    case SUCCESS:
                        setStatus(Status.SUCCESS);
                        break;
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                setStatus(Status.ERROR);
            }
        });

    }

    private void setStatus(Status status) {
        if (binding.progress.getStatus() != status) {
            binding.progress.setStatus(status);
        }

        int tintColor = ContextCompat.getColor(this, R.color.green_btn);
        int disabledColor = ContextCompat.getColor(this, R.color.disabled_tint_color);

        if (Build.VERSION.SDK_INT >= 19) {
            TransitionManager.beginDelayedTransition((ViewGroup) binding.getRoot());
        }

        switch (status) {
            case LOADING: {
                binding.title.setText(R.string.tango_uploading_loading_title);
                binding.text.setText("");
                ViewTool.setBackgroundTint(binding.btn, disabledColor);
                binding.btn.setText(R.string.tango_uploading_btn_ok);
                binding.btn.setEnabled(false);
                break;
            }
            case SUCCESS: {
                setResult(RESULT_OK);
                binding.btn.setText(R.string.tango_uploading_btn_ok);
                binding.title.setText(R.string.tango_uploading_success_title);
                binding.text.setText(R.string.tango_uploading_success_text);
                ViewTool.setBackgroundTint(binding.btn, tintColor);
                binding.btn.setEnabled(true);
                binding.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                break;
            }
            case ERROR: {
                binding.btn.setText(R.string.tango_uploading_btn_retry);
                binding.title.setText(R.string.tango_uploading_failed_title);
                binding.text.setText(R.string.tango_uploading_failed_text);
                ViewTool.setBackgroundTint(binding.btn, tintColor);
                binding.btn.setEnabled(true);
                binding.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setStatus(Status.LOADING);
                        MeasurementUploadManager.getInstance().getExecutor().retry();
                    }
                });
                break;
            }
        }

    }

}
