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
import org.wheelmap.android.activity.MainSinglePaneActivity;
import org.wheelmap.android.activity.POIDetailActivity;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.online.R;
import org.wheelmap.android.test.groups.PhoneTest;
import org.wheelmap.android.test.profile.ProfileActivityTest;
import org.wheelmap.android.test.profile.ProfileUtils;
import org.wheelmap.android.utils.UtilsMisc;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;

/**
 * some tests for {@link MainSinglePaneActivity}
 * IMPORTANT: only for phones due to the use of the MainSinglePaneActivity
 *
 * Created by timfreiheit on 27.11.15.
 */
@RunWith(AndroidJUnit4.class)
@PhoneTest
public class MainSinglePaneActivityTest {

    @Rule
    public ActivityTestRule<MainSinglePaneActivity> mActivityRule = new ActivityTestRule<MainSinglePaneActivity>(MainSinglePaneActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            Context targetContext = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext();

            // clear preferences
            PreferenceManager.getDefaultSharedPreferences(targetContext);
            ProfileUtils.logout();

            Intent result = new Intent(targetContext, MainSinglePaneActivity.class);
            result.putExtra(Extra.SELECTED_TAB, 0);
            return result;
        }
    };

    @Before
    public void before() {
        Intents.init();

        assertFalse("This test is only for phones", UtilsMisc.isTablet(mActivityRule.getActivity()));

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

        onView(withId(R.id.search_keyword)).perform(typeText(searchText));
        onView(withId(R.id.button_search)).perform(click());

        //noinspection unchecked
        allOf(withText(searchText)).matches(isDisplayed());

        // click on first item
        onData(anything()).atPosition(0).perform(click());

        intended(hasComponent(POIDetailActivity.class.getName()));

        // make sure the correct detail activity was launched
        withText(searchText).matches(isDisplayed());
    }

    /**
     * test if the view can be switched between list and map view
     */
    @Test
    public void testSwitchView(){

        // list must be visible
        withId(android.R.id.list).matches(isDisplayed());
        withId(R.id.map).matches(not(isDisplayed()));

        onView(withId(R.id.switch_view)).perform(click());

        // map must be visible
        withId(android.R.id.list).matches(not(isDisplayed()));
        withId(R.id.map).matches(isDisplayed());

        onView(withId(R.id.switch_view)).perform(click());

        // list must be visible
        withId(android.R.id.list).matches(isDisplayed());
        withId(R.id.map).matches(not(isDisplayed()));
    }

    @Test
    public void testNewPoi(){
        onView(withId(R.id.menu_new_poi)).perform(click());
        if (!ProfileUtils.isLoggedIn()) {
            ProfileActivityTest.login();
        }
        // check if edit screen is shown
        withText(R.string.title_editor).matches(isDisplayed());
    }

}