package org.wheelmap.android.test.profile;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.web.webdriver.Locator;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wheelmap.android.activity.DashboardActivity;
import org.wheelmap.android.modules.UserCredentials;
import org.wheelmap.android.online.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webClick;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webKeys;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Test login and Logout
 *
 * Created by timfreiheit on 27.11.15.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class ProfileActivityTest {

    @Rule
    public ActivityTestRule<DashboardActivity> mActivityRule = new ActivityTestRule<>(DashboardActivity.class);

    @Before
    public void before(){
        Intents.init();
    }

    @After
    public void after(){
        Intents.release();
    }

    @Test
    public void testLogin() {
        ProfileUtils.logout();

        onView(withId(R.id.dashboard_login)).perform(click());
        login();

    }

    public static void login(){

        onView(withId(R.id.button_login)).perform(click());

        // enter credentials
        onWebView().withElement(findElement(Locator.ID, "username")).perform(webKeys(ProfileUtils.TEST_USER));
        onWebView().withElement(findElement(Locator.ID, "password")).perform(webKeys(ProfileUtils.TEST_USER_PASSWORD));

        // press login
        onWebView().withElement(findElement(Locator.NAME, "commit")).perform(webClick());

        // agree account access
        onWebView().withElement(findElement(Locator.NAME, "commit")).perform(webClick());

        try {
            // i don't know how to make it better`
            // wait for the login to finish
            Thread.sleep(2000);
        }catch ( Exception e){
            e.printStackTrace();
        }

        assertTrue(ProfileUtils.isLoggedIn());
    }

    @Test
    public void testLogout() {
        if (!ProfileUtils.isLoggedIn()) {
            ProfileUtils.fakeLogin();
        }
        onView(withId(R.id.dashboard_login)).perform(click());
        onView(withId(R.id.logout)).perform(click());

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertFalse(new UserCredentials(context).isLoggedIn());
    }

}
