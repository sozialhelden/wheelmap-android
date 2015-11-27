package org.wheelmap.android.activity.listeners;

/**
 * Created by timfreiheit on 27.11.15.
 */
public final class Progress {
    public interface Provider {
        void addProgressListener(Progress.Listener listener);
    }

    public interface Listener {
        void onProgressChanged(boolean isLoading);
    }
}
