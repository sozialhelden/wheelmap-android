package org.wheelmap.android.view.progress;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.wheelmap.android.online.R;

import java.util.HashMap;
import java.util.Map;

public class ProgressCompleteView extends FrameLayout {

    public enum Status {
        LOADING,
        SUCCESS,
        ERROR
    }

    private CircularProgressView progressView;
    private ImageView completeImage;

    private Status status = Status.LOADING;

    private Map<Status, Integer> colors = new HashMap<>();

    public ProgressCompleteView(Context context) {
        super(context);
        init(context);
    }

    public ProgressCompleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ProgressCompleteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        progressView = new CircularProgressView(context);

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        progressView.setLayoutParams(params);
        progressView.startAnimation();
        addView(progressView);

        completeImage = new ImageView(context);
        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int margin = (int) Utils.dpToPx(context, 10);
        params.setMargins(margin, margin, margin, margin);
        completeImage.setLayoutParams(params);
        completeImage.setVisibility(View.GONE);
        addView(completeImage);

        setColor(Status.LOADING, getThemeAccentColor(getContext()));
        setColor(Status.ERROR, Color.RED);
        setColor(Status.SUCCESS, Color.GREEN);

        progressView.setColor(colors.get(Status.LOADING));

    }

    public void setColor(Status status, @ColorInt int color) {
        colors.put(status, color);
        if (status == this.status) {
            progressView.setColor(color);
        }
    }

    public void setStatus(Status status) {
        this.status = status;
        int color = colors.get(status);
        switch (status) {
            case LOADING:
                progressView.setColor(color);
                progressView.startAnimation();
                completeImage.setVisibility(View.GONE);
                break;
            case ERROR: {
                progressView.completeAnimation(color);
                completeImage.setVisibility(View.VISIBLE);
                AnimatedVectorDrawableCompat drawable = AnimatedVectorDrawableCompat.create(getContext(), R.drawable.ic_error_animated);
                drawable.setTint(color);
                animateDrawable(drawable);
                break;
            }
            case SUCCESS: {
                progressView.completeAnimation(color);
                AnimatedVectorDrawableCompat drawable = AnimatedVectorDrawableCompat.create(getContext(), R.drawable.ic_check_animated);
                drawable.setTint(color);
                animateDrawable(drawable);
                break;
            }
        }
    }

    private void animateDrawable(final AnimatedVectorDrawableCompat drawable) {
        completeImage.setImageDrawable(null);
        completeImage.setVisibility(View.VISIBLE);
        progressView.addListener(new CircularProgressView.CircularProgressViewListener() {
            @Override
            public void onAnimationStart() {

            }

            @Override
            public void onAnimationEnd() {
                completeImage.setVisibility(View.VISIBLE);
                completeImage.setImageDrawable(drawable);
                drawable.start();
                progressView.removeListener(this);
            }
        });
    }

    public Status getStatus() {
        return status;
    }

    public static int getThemeAccentColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        return value.data;
    }
}
