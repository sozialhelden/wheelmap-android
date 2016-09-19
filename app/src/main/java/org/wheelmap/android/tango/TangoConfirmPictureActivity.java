package org.wheelmap.android.tango;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.google.auto.value.AutoValue;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.wheelmap.android.activity.base.BaseActivity;
import org.wheelmap.android.online.R;
import org.wheelmap.android.online.databinding.TangoConfirmPictureActivityBinding;
import org.wheelmap.android.utils.Arguments;
import org.wheelmap.android.utils.ViewTool;

public class TangoConfirmPictureActivity extends BaseActivity {

    private TangoConfirmPictureActivityBinding binding;

    public static Intent newIntent(Context context, Uri fileUri) {
        Intent intent = new Intent(context, TangoConfirmPictureActivity.class);
        intent.putExtras(new AutoValue_TangoConfirmPictureActivity_Args(fileUri).toBundle());
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = TangoConfirmPictureActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final Args args = Args.fromBundle(getIntent().getExtras());

        ImageLoader.getInstance().displayImage(args.uri().toString(), binding.image);

        int tintColor = ContextCompat.getColor(this, R.color.green_btn);
        ViewTool.setBackgroundTint(binding.cancelBtn, tintColor);
        ViewTool.setBackgroundTint(binding.confirmBtn, tintColor);


        View.OnClickListener cancelClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        };

        binding.cancelBtn.setOnClickListener(cancelClickListener);
        binding.closeBtn.setOnClickListener(cancelClickListener);

        binding.confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = TangoAdditionalInformationActivity.newIntent(TangoConfirmPictureActivity.this, args.uri());
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in_medium, 0);
                finish();
            }
        });

    }

    @AutoValue
    static abstract class Args extends Arguments {
        abstract Uri uri();
    }

}
