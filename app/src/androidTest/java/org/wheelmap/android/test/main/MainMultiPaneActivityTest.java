package org.wheelmap.android.test.main;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wheelmap.android.activity.MainMultiPaneActivity;
import org.wheelmap.android.activity.MainSinglePaneActivity;
import org.wheelmap.android.online.R;
import org.wheelmap.android.test.profile.ProfileActivityTest;
import org.wheelmap.android.test.profile.ProfileUtils;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;

/**
 * some tests for {@link MainMultiPaneActivity}
 * Created by timfreiheit on 30.11.15.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainMultiPaneActivityTest {
    @Rule
    public ActivityTestRule<MainMultiPaneActivity> mActivityRule = new ActivityTestRule<MainMultiPaneActivity>(MainMultiPaneActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            Context targetContext = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext();

            // clear preferences
            PreferenceManager.getDefaultSharedPreferences(targetContext);
            ProfileUtils.logout();

            Intent result = new Intent(targetContext, MainMultiPaneActivity.class);
            result.putExtra("", "");
            return result;
        }
    };

    @Before
    public void before() {
        Intents.init();

        // wait for network requests
        Espresso.registerIdlingResources(new ProgressIdlingResource(mActivityRule.getActivity()));

    }

    @After
    public void after() {
        Intents.release();
    }

    @Test
    public void testSearch(){

        String searchText = "S+U Brandenburger Tor";
        onView(withId(R.id.menu_search)).perform(click());

        try {
            // because of slow emulator sometimes
            Thread.sleep(500);
        }catch (Exception e){
            e.printStackTrace();
        }

        onView(withId(R.id.search_keyword)).perform(typeText(searchText));
        onView(withId(R.id.button_search)).perform(click());

        //noinspection unchecked
        allOf(withText(searchText)).matches(isDisplayed());

        // click on first item
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(0).perform(click());

        // make sure the detail fragment is displayed
        withId(R.id.titlebar_backbutton).matches(isDisplayed());

    }

    @Test
    public void testNewPoi(){
        onView(withId(R.id.menu_new_poi)).perform(click());

        try {
            // because of slow emulator sometimes
            Thread.sleep(500);
        }catch (Exception e){
            e.printStackTrace();
        }

        if (!ProfileUtils.isLoggedIn()) {
            ProfileActivityTest.login();
        }
        // check if edit screen is shown
        withText(R.string.title_editor).matches(isDisplayed());
    }

}
