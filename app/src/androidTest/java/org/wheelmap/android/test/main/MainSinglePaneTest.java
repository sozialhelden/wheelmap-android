package org.wheelmap.android.test.main;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wheelmap.android.activity.MainSinglePaneActivity;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.online.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/**
 * some tests for {@link MainSinglePaneActivity}
 * Created by timfreiheit on 27.11.15.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class MainSinglePaneTest {

    @Rule
    public ActivityTestRule<MainSinglePaneActivity> mActivityRule = new ActivityTestRule<MainSinglePaneActivity>(MainSinglePaneActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            Context targetContext = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext();
            Intent result = new Intent(targetContext, MainSinglePaneActivity.class);
            result.putExtra(Extra.MAP_MODE_ENGAGE, true);
            result.putExtra(Extra.SELECTED_TAB, 0);
            return result;
        }
    };

    @Before
    public void before() {
        Intents.init();
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

        allOf(withText(searchText)).matches(isDisplayed());

    }

}