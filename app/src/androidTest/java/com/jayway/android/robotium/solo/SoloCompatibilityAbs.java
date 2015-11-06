package com.jayway.android.robotium.solo;

import android.app.Activity;
import android.app.Instrumentation;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class SoloCompatibilityAbs extends Solo {

    protected ClickerCompatibilityAbs clickerCompatibilityAbs;

    public SoloCompatibilityAbs(Instrumentation instrumentation, Activity activity) {
        super(instrumentation, activity);
        clickerCompatibilityAbs = new ClickerCompatibilityAbs(activityUtils, sleeper, clicker);
    }

    /**
     * Clicks on an ActionBar item with a given resource id. Should be called only in apps that use
     * ActionBarSherlock and which are tested on pre ICS Android. Tests which run on 4.0 and higher
     * should use {@link Solo#clickOnActionBarItem(int)}.
     *
     * @param resourceId the R.id of the ActionBar item
     */
    public void clickOnVisibleActionbarItem(int resourceId) {
        waitForView(LinearLayout.class);
        LinearLayout linearLayout = getter.getView(LinearLayout.class, resourceId);
        clickOnView(linearLayout);
    }

    public ArrayList<Activity> getAllOpenedActivities() {
        return activityUtils.getAllOpenedActivities();
    }
}
