package org.wheelmap.android.test.rules;

import android.app.Activity;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 *
 * Created by timfreiheit on 27.11.15.
 */
public class BeforeActivityTestRule<T extends Activity> extends ActivityTestRule<T> {

    public interface OnBeforeActivityLaunchedListener {
        void beforeActivityLaunched();
    }

    private OnBeforeActivityLaunchedListener mListener;

    public BeforeActivityTestRule(Class<T> activityClass, OnBeforeActivityLaunchedListener listener) {
        super(activityClass);
        mListener = listener;
    }

    public BeforeActivityTestRule(Class<T> activityClass, boolean initialTouchMode, OnBeforeActivityLaunchedListener listener) {
        super(activityClass, initialTouchMode);
        mListener = listener;
    }

    public BeforeActivityTestRule(Class<T> activityClass, boolean initialTouchMode, boolean launchActivity, OnBeforeActivityLaunchedListener listener) {
        super(activityClass, initialTouchMode, launchActivity);
        mListener = listener;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        if (mListener != null) {
            mListener.beforeActivityLaunched();
        }
        return super.apply(base, description);
    }

}
