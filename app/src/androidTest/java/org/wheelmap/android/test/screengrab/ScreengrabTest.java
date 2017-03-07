package org.wheelmap.android.test.screengrab;

/**
 * Created by waelgabsi on 02/03/17.
 */

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.wheelmap.android.activity.StartupActivity;
import org.wheelmap.android.online.R;

import java.io.File;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

/**
 * Created by waelgabsi on 24/02/17.
 */
@LargeTest
@RunWith(JUnit4.class)
public class ScreengrabTest {

    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<StartupActivity> mActivityTestRule = new ActivityTestRule<>(StartupActivity.class, false, false);

    @Before
    public void clearprefs() {
        File root = InstrumentationRegistry.getTargetContext().getFilesDir().getParentFile();
        String[] sharedPreferencesFileNames = new File(root, "shared_prefs").list();
        for (String fileName : sharedPreferencesFileNames) {
            InstrumentationRegistry.getTargetContext().getSharedPreferences(fileName.replace(".xml", ""), Context.MODE_PRIVATE).edit().clear().commit();
        }
    }

    @Test
    public void takeScreenshots() {

        mActivityTestRule.launchActivity(null);


        Screengrab.screenshot("SPLASHSCREEN");
        onView(isRoot()).perform(waitFor(4000));
        Screengrab.screenshot("INTRODUCTION");
        ViewInteraction appCompatButton = onView(withId(R.id.introduction_done));
        appCompatButton.perform(click());



        ViewInteraction appCompatButton2 = onView(
                allOf(withId(android.R.id.button2),
                        withParent(allOf(withClassName(is("com.android.internal.widget.ButtonBarLayout")),
                                withParent(withClassName(is("android.widget.LinearLayout"))))),
                        isDisplayed()));
        appCompatButton2.perform(click());
        Screengrab.screenshot("DASHBOARD");

        ViewInteraction squareTextView = onView(withId(R.id.dashboard_btn_in_der_naehe));
        squareTextView.check(matches(isDisplayed()));
        squareTextView.perform(scrollTo()).perform( click());
        Screengrab.screenshot("NEARBY");

        pressBack();

        ViewInteraction squareTextView2 = onView(
                allOf(withId(R.id.dashboard_btn_karte)));
        squareTextView2.perform( click());
        Screengrab.screenshot("MAP");

    }

    public static ViewAction waitFor(final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " milliseconds.";
            }

            @Override
            public void perform(UiController uiController, final View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }



}
