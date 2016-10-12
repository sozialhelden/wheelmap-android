package org.wheelmap.android.view.progress;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

class CircularProgressView extends View {

    private static final float INDETERMINANT_MIN_SWEEP = 15f;

    public interface CircularProgressViewListener {

        /**
         * Called when resetAnimation() is called.
         */
        void onAnimationStart();

        void onAnimationEnd();
    }


    private Paint paint;
    private int size = 0;
    private RectF bounds;

    private float indeterminateSweep, indeterminateRotateOffset;
    private int thickness;
    @ColorInt
    private int color = Color.BLUE;
    private int animDuration;
    private int animSteps;
    private boolean animationRunning = true;

    private List<CircularProgressViewListener> listeners;
    // Animation related stuff
    private float startAngle;
    private AnimatorSet indeterminateAnimator;
    private AnimatorSet completeAnimator;

    public CircularProgressView(Context context) {
        super(context);
        init(null, 0);
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CircularProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    protected void init(AttributeSet attrs, int defStyle) {
        listeners = new ArrayList<>();

        initAttributes(attrs, defStyle);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        updatePaint();

        bounds = new RectF();
    }

    private void initAttributes(AttributeSet attrs, int defStyle) {

        // Initialize attributes from styleable attributes
        thickness = (int) Utils.dpToPx(getContext(), 4);

        int accentColor = getContext().getResources().getIdentifier("colorAccent", "attr", getContext().getPackageName());

        color = Color.parseColor("#2196F3");
        // If using support library v7 accentColor
        if (accentColor != 0) {
            TypedValue t = new TypedValue();
            getContext().getTheme().resolveAttribute(accentColor, t, true);
            color = t.data;
        }
        // If using native accentColor (SDK >21)
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedArray t = getContext().obtainStyledAttributes(new int[]{android.R.attr.colorAccent});
            color = t.getColor(0, color);
        }

        animDuration = 4000;

        animSteps = 3;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int xPad = getPaddingLeft() + getPaddingRight();
        int yPad = getPaddingTop() + getPaddingBottom();
        int width = getMeasuredWidth() - xPad;
        int height = getMeasuredHeight() - yPad;
        size = (width < height) ? width : height;
        setMeasuredDimension(size + xPad, size + yPad);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        size = (w < h) ? w : h;
        updateBounds();
    }

    private void updateBounds() {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        bounds.set(paddingLeft + thickness, paddingTop + thickness, size - paddingLeft - thickness, size - paddingTop - thickness);
    }

    private void updatePaint() {
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(thickness);
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(color);
        // Draw the arc
        canvas.drawArc(bounds, startAngle + indeterminateRotateOffset, indeterminateSweep, false, paint);
    }

    /**
     * Get the thickness of the progress bar arc.
     *
     * @return the thickness of the progress bar arc
     */
    public int getThickness() {
        return thickness;
    }

    /**
     * Sets the thickness of the progress bar arc.
     *
     * @param thickness the thickness of the progress bar arc
     */
    public void setThickness(int thickness) {
        this.thickness = thickness;
        updatePaint();
        updateBounds();
        invalidate();
    }

    /**
     * @return the color of the progress bar
     */
    public int getColor() {
        return color;
    }

    /**
     * Sets the color of the progress bar.
     *
     * @param color the color of the progress bar
     */
    public void setColor(int color) {
        this.color = color;
        updatePaint();
        invalidate();
    }

    public void completeAnimation(@ColorInt int color) {
        animationRunning = false;
        stopAnimation();
        completeAnimator = createCompleteAnimator(color);
        completeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                for (CircularProgressViewListener listener : listeners) {
                    listener.onAnimationEnd();
                }
            }
        });
        completeAnimator.start();
    }

    /**
     * Register a CircularProgressViewListener with this View
     *
     * @param listener The listener to register
     */
    public void addListener(CircularProgressViewListener listener) {
        if (listener != null)
            listeners.add(listener);
    }

    /**
     * Unregister a CircularProgressViewListener with this View
     *
     * @param listener The listener to unregister
     */
    public void removeListener(CircularProgressViewListener listener) {
        listeners.remove(listener);
    }

    /**
     * Starts the progress bar animation.
     * (This is an alias of resetAnimation() so it does the same thing.)
     */
    public void startAnimation() {
        animationRunning = true;
        indeterminateRotateOffset = 0;
        startAngle = -90;
        resetAnimation();
    }

    /**
     * Resets the animation.
     */
    void resetAnimation() {

        if (getVisibility() != VISIBLE || !animationRunning) {
            return;
        }

        if (completeAnimator != null && completeAnimator.isRunning()) {
            completeAnimator.cancel();
        }

        // Cancel all the old animators
        if (indeterminateAnimator != null && indeterminateAnimator.isRunning()) {
            indeterminateAnimator.cancel();
        }

        indeterminateSweep = INDETERMINANT_MIN_SWEEP;
        // Build the whole AnimatorSet
        indeterminateAnimator = new AnimatorSet();
        AnimatorSet prevSet = null, nextSet;
        for (int k = 0; k < animSteps; k++) {
            nextSet = createIndeterminateAnimator(k);
            AnimatorSet.Builder builder = indeterminateAnimator.play(nextSet);
            if (prevSet != null) {
                builder.after(prevSet);
            }
            prevSet = nextSet;
        }

        // Listen to end of animation so we can infinitely loop
        indeterminateAnimator.addListener(new AnimatorListenerAdapter() {
            boolean wasCancelled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                wasCancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!wasCancelled) {
                    resetAnimation();
                }
            }
        });

        indeterminateAnimator.start();

        for (CircularProgressViewListener listener : listeners) {
            listener.onAnimationStart();
        }

    }

    /**
     * Stops the animation
     */
    public void stopAnimation() {
        if (indeterminateAnimator != null) {
            indeterminateAnimator.cancel();
            indeterminateAnimator = null;
        }
    }

    // Creates the animators for one step of the animation
    private AnimatorSet createIndeterminateAnimator(final float step) {
        final float maxSweep = 360f * (animSteps - 1) / animSteps + INDETERMINANT_MIN_SWEEP;
        final float start = -90f + step * (maxSweep - INDETERMINANT_MIN_SWEEP);

        // Extending the front of the arc
        ValueAnimator frontEndExtend = ValueAnimator.ofFloat(INDETERMINANT_MIN_SWEEP, maxSweep);
        frontEndExtend.setDuration(animDuration / animSteps / 2);
        frontEndExtend.setInterpolator(new DecelerateInterpolator(1));
        frontEndExtend.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                indeterminateSweep = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });

        // Overall rotation
        ValueAnimator rotateAnimator1 = ValueAnimator.ofFloat(step * 720f / animSteps, (step + .5f) * 720f / animSteps);
        rotateAnimator1.setDuration(animDuration / animSteps / 2);
        rotateAnimator1.setInterpolator(new LinearInterpolator());
        rotateAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                indeterminateRotateOffset = (Float) animation.getAnimatedValue();
            }
        });

        // Followed by...

        // Retracting the back end of the arc
        ValueAnimator backEndRetract = ValueAnimator.ofFloat(start, start + maxSweep - INDETERMINANT_MIN_SWEEP);
        backEndRetract.setDuration(animDuration / animSteps / 2);
        backEndRetract.setInterpolator(new DecelerateInterpolator(1));
        backEndRetract.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                startAngle = (Float) animation.getAnimatedValue();
                indeterminateSweep = maxSweep - startAngle + start;
                invalidate();
            }
        });

        // More overall rotation
        ValueAnimator rotateAnimator2 = ValueAnimator.ofFloat((step + .5f) * 720f / animSteps, (step + 1) * 720f / animSteps);
        rotateAnimator2.setDuration(animDuration / animSteps / 2);
        rotateAnimator2.setInterpolator(new LinearInterpolator());
        rotateAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                indeterminateRotateOffset = (Float) animation.getAnimatedValue();
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.play(frontEndExtend).with(rotateAnimator1);
        set.play(backEndRetract).with(rotateAnimator2).after(rotateAnimator1);
        return set;
    }

    // Creates the animators for one step of the animation
    private AnimatorSet createCompleteAnimator(@ColorInt final int color) {
        final float maxSweep = 360f;
        final float start = startAngle;

        int durationPerAngle = animDuration / 360;

        int duration = (int) ((360 - (indeterminateSweep % 360)) * durationPerAngle) / 2;

        // Extending the front of the arc
        ValueAnimator frontEndExtend = ValueAnimator.ofFloat(indeterminateSweep, maxSweep);
        frontEndExtend.setDuration(duration);
        frontEndExtend.setInterpolator(new DecelerateInterpolator(1));
        frontEndExtend.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                indeterminateSweep = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });

        // Followed by...

        // Retracting the back end of the arc
        ValueAnimator backEndRetract = ValueAnimator.ofFloat(start, start + maxSweep - INDETERMINANT_MIN_SWEEP);
        backEndRetract.setDuration(duration);
        backEndRetract.setInterpolator(new DecelerateInterpolator(1));
        backEndRetract.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                startAngle = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });

        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), this.color, color);
        colorAnimator.setDuration(duration);
        colorAnimator.setInterpolator(new DecelerateInterpolator(1));
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                CircularProgressView.this.color = (Integer) valueAnimator.getAnimatedValue();
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.play(frontEndExtend).with(backEndRetract).with(colorAnimator);
        return set;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    @Override
    public void setVisibility(int visibility) {
        int currentVisibility = getVisibility();
        super.setVisibility(visibility);
        if (visibility != currentVisibility) {
            if (visibility == View.VISIBLE) {
                resetAnimation();
            } else if (visibility == View.GONE || visibility == View.INVISIBLE) {
                stopAnimation();
            }
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            resetAnimation();
        } else if (visibility == View.GONE || visibility == View.INVISIBLE) {
            stopAnimation();
        }
    }
}

