package org.wheelmap.android.test.dashboard;

import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wheelmap.android.activity.DashboardActivity;
import org.wheelmap.android.activity.InfoActivity;
import org.wheelmap.android.activity.MainSinglePaneActivity;
import org.wheelmap.android.activity.profile.ProfileActivity;
import org.wheelmap.android.online.R;
import org.wheelmap.android.test.groups.PhoneTest;
import org.wheelmap.android.utils.UtilsMisc;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertFalse;

/**
 *
 * Created by timfreiheit on 27.11.15.
 */
@RunWith(AndroidJUnit4.class)
@PhoneTest
public class DashboardActivityTest {

    @Rule
    public ActivityTestRule<DashboardActivity> mActivityRule = new ActivityTestRule<>(DashboardActivity.class);

    @Before
    public void before(){
        Intents.init();
        assertFalse("This test is only for phones", UtilsMisc.isTablet(mActivityRule.getActivity()));
    }

    @After
    public void after(){
        Intents.release();
    }

    @Test
    public void searchText(){
        onView(withId(R.id.dashboard_search_edit)).perform(typeText("test"));
        onView(withId(R.id.dashboard_search)).perform(click());
        intended(hasComponent(MainSinglePaneActivity.class.getName()));
    }

    @Test
    public void openLogin(){
        onView(withId(R.id.dashboard_login)).perform(click());
        intended(hasComponent(ProfileActivity.class.getName()));
    }

    @Test
    public void openInfo(){
        onView(withId(R.id.dashboard_info)).perform(click());
        intended(hasComponent(InfoActivity.class.getName()));
    }


}
