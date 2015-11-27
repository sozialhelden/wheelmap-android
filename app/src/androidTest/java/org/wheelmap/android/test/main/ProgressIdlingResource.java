package org.wheelmap.android.test.main;

import android.support.test.espresso.IdlingResource;

import org.wheelmap.android.activity.listeners.Progress;

public class ProgressIdlingResource implements IdlingResource {

    private ResourceCallback resourceCallback;

    private boolean isInProgress;
    public ProgressIdlingResource(Progress.Provider provider){
        provider.addProgressListener(new Progress.Listener() {
            @Override
            public void onProgressChanged(boolean isLoading) {
                if (resourceCallback == null){
                    return ;
                }
                isInProgress = isLoading;
                if (!isLoading) {
                    //Called when the resource goes from busy to idle.
                    resourceCallback.onTransitionToIdle();
                }
            }
        });
    }
    @Override
    public String getName() {
        return "ProgressListener";
    }

    @Override
    public boolean isIdleNow() {
        // the resource becomes idle when the progress has been dismissed
        return !isInProgress;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }
}