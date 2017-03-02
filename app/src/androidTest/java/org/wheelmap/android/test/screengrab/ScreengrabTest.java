package org.wheelmap.android.test.screengrab;

/**
 * Created by waelgabsi on 02/03/17.
 */

import android.support.test.rule.ActivityTestRule;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.wheelmap.android.activity.StartupActivity;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;
import tools.fastlane.screengrab.locale.LocaleTestRule;

/**
 * Created by waelgabsi on 24/02/17.
 */
@RunWith(JUnit4.class)
public class ScreengrabTest {
    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<StartupActivity> activityRule = new ActivityTestRule<>(StartupActivity.class);

    @Test
    public void testTakeScreenshot() {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());

        Screengrab.screenshot("before_button_click");

        //  onView(withId(R.id.button_movable_resize)).perform(click());
    }
}
