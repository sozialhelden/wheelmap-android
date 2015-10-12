package org.wheelmap.android.test;

import com.jayway.android.robotium.solo.SoloCompatibilityAbs;

import org.wheelmap.android.activity.MainMultiPaneActivity;
import org.wheelmap.android.activity.POIDetailEditableActivity;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.fragment.CombinedWorkerFragment;
import org.wheelmap.android.online.R;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.ImageView;

public class MainMultiPaneTest extends
        ActivityInstrumentationTestCase2<MainMultiPaneActivity> {

    private static final String TAG = MainMultiPaneTest.class.getSimpleName();

    private final static int WAIT_IN_SECONDS_TO_FINISH = 60;

    private SoloCompatibilityAbs solo;

    private Object mutex = new Object();

    public MainMultiPaneTest() {
        super(MainMultiPaneActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        solo = new SoloCompatibilityAbs(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
        solo = null;
    }

    public void testAAASetupApp() {
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                getInstrumentation().callApplicationOnCreate(
                        WheelmapApp.getApp());
            }
        });
        getInstrumentation().waitForIdleSync();
    }

    public void testAActivityGettingStarted() throws Exception {
        RobotiumHelper.waitForListRefreshingDone(solo,
                CombinedWorkerFragment.TAG);

        solo.clickInList(1);
        solo.sleep(2000);

        ImageView moveButton = (ImageView) getActivity().findViewById(
                R.id.button_movable_resize);
        solo.clickOnView(moveButton);
        solo.sleep(500);
        solo.clickOnView(moveButton);
        solo.sleep(500);
        solo.clickInList(4);
    }

    public void testBNewItem() throws Exception {
        RobotiumHelper.waitForListRefreshingDone(solo,
                CombinedWorkerFragment.TAG);

        solo.clickOnActionBarItem(R.id.map_btn_locate);
        solo.clickOnActionBarItem(R.id.menu_new_poi);

        solo.sleep(1000);
        RobotiumHelper.login(solo);

        solo.waitForActivity("POIDetailEditableActivity");
        solo.assertCurrentActivity("want edit activity",
                POIDetailEditableActivity.class);
        solo.clearEditText(0);
        solo.enterText(0, "testtest");
        RobotiumHelper.selectWheelchairState(solo, "POIDetailEditableActivity");
        RobotiumHelper.selectCategoryState(solo);
        //solo.clickOnActionBarItem(R.id.menu_save);
        solo.clickOnButton("Okay");

        RobotiumHelper.logout(solo, "MainMultiPaneActivity");

    }

    public void testCListAndEditorFragment() throws Exception {
        RobotiumHelper.waitForListRefreshingDone(solo,
                CombinedWorkerFragment.TAG);
        RobotiumHelper.login(solo);

        solo.clickInList(2);
        // solo.clickOnActionBarItem(R.id.menu_edit);

        solo.waitForActivity("POIDetailEditableActivity");
        solo.assertCurrentActivity("detail activity",
                POIDetailEditableActivity.class);
        solo.clearEditText(0);
        solo.enterText(0, "testtest");
        RobotiumHelper.selectWheelchairState(solo, "POIDetailEditableActivity");
        RobotiumHelper.selectCategoryState(solo);
        //solo.clickOnActionBarItem(R.id.menu_save);
        solo.waitForActivity("MainMultiPaneActivity");

        solo.clickInList(5);
        // solo.clickOnActionBarItem(R.id.menu_edit);

        solo.waitForActivity("POIDetailEditableActivity");
        solo.assertCurrentActivity("detail activity",
                POIDetailEditableActivity.class);
        solo.clearEditText(0);
        solo.enterText(0, "testtest");
        RobotiumHelper.selectWheelchairState(solo, "POIDetailEditableActivity");
        RobotiumHelper.selectCategoryState(solo);

        //solo.clickOnActionBarItem(R.id.menu_save);
        solo.waitForActivity("MainMultiPaneActivity");
        solo.assertCurrentActivity("main activity", MainMultiPaneActivity.class);

        RobotiumHelper.logout(solo, "MainMultiPaneActivity");

    }

    public void testFSearch() throws Exception {

        RobotiumHelper.searchTestList(solo, CombinedWorkerFragment.TAG);

    }
}
