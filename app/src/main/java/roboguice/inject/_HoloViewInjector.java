package roboguice.inject;

import android.app.Activity;

/**
 * Created by timfreiheit on 12.10.15.
 */
public class _HoloViewInjector {
    public static void inject(Activity activity) {
        ViewListener.ViewMembersInjector.injectViews(activity);
    }
}
