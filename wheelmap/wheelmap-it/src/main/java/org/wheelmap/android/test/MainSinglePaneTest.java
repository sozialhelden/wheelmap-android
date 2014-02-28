package org.wheelmap.android.test;

import com.jayway.android.robotium.solo.SoloCompatibilityAbs;

import org.holoeverywhere.widget.EditText;
import org.springframework.util.Assert;
import org.wheelmap.android.activity.MainSinglePaneActivity;
import org.wheelmap.android.activity.MyTabListener;
import org.wheelmap.android.activity.POIDetailActivity;
import org.wheelmap.android.activity.POIDetailEditableActivity;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.fragment.POIDetailEditableFragment;
import org.wheelmap.android.fragment.POIDetailFragment;
import org.wheelmap.android.fragment.POIsListFragment;
import org.wheelmap.android.fragment.POIsListWorkerFragment;
import org.wheelmap.android.fragment.POIsMapWorkerFragment;
import org.wheelmap.android.fragment.POIsOsmdroidFragment;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.UtilsMisc;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

public class MainSinglePaneTest extends
        ActivityInstrumentationTestCase2<MainSinglePaneActivity> {

    private static final String TAG = MainSinglePaneTest.class.getSimpleName();

    private final static int WAIT_IN_SECONDS_TO_FINISH = 60;

    private FragmentId[] ids;

    private SoloCompatibilityAbs solo;

    public MainSinglePaneTest() {
        super(MainSinglePaneActivity.class);
    }

    private void clickOnActionbar(int resId) {
        if (UtilsMisc.hasHoneycomb()) {
            solo.clickOnActionBarItem(resId);
        } else {
            solo.clickOnVisibleActionbarItem(resId);
        }
    }

    @Override
    public void setUp() throws Exception {
        ids = new FragmentId[]{
                new FragmentId(MyTabListener.TAB_LIST,
                        POIsListFragment.TAG, POIsListWorkerFragment.TAG),
                new FragmentId(MyTabListener.TAB_MAP,
                        POIsOsmdroidFragment.TAG,
                        POIsMapWorkerFragment.TAG)};
        solo = new SoloCompatibilityAbs(getInstrumentation(), getActivity());
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
        solo = null;
    }

    private void executeTabSelect(final int tabId) {
        final MainSinglePaneActivity activity = getActivity();
        //Instrumentation.ActivityMonitor monitor = getInstrumentation()
        //        .addMonitor(MainSinglePaneActivity.class.getName(), null, false);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getSupportActionBar().setSelectedNavigationItem(
                        ids[tabId].tab);
            }
        });

        Assert.isTrue(solo.waitForFragmentByTag(ids[tabId].displayTag));
        Assert.isTrue(solo.waitForFragmentByTag(ids[tabId].workerTag));
        Log.d(TAG, "tab with id = " + tabId + " selected");
    }

    public void testAAASetupApp() {
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                getInstrumentation().callApplicationOnCreate(WheelmapApp.getApp());
            }
        });
        // getInstrumentation().waitForIdleSync();
    }

    public void atestAFragmentsGettingStarted() {
        executeTabSelect(1);
        executeTabSelect(0);
        executeTabSelect(1);
    }

    public void atestBListAndDetailFragment()
            throws Exception {
        executeTabSelect(0);
        RobotiumHelper.waitForListRefreshingDone(solo, POIsListWorkerFragment.TAG);

        solo.clickInList(2);
        solo.waitForActivity("POIDetailActivity");
        solo.assertCurrentActivity("detail activity", POIDetailActivity.class);
        solo.waitForFragmentByTag(POIDetailFragment.TAG);

        solo.goBack();
        solo.waitForActivity("MainSinglePaneActivity");
        solo.assertCurrentActivity("main activity",
                MainSinglePaneActivity.class);
        solo.finishOpenedActivities();

    }

    public void testCMapListAndDetailFragment() throws Exception {
        executeTabSelect(0);
        executeTabSelect(1);
        solo.waitForFragmentByTag(POIsOsmdroidFragment.TAG);
        clickOnActionbar(R.id.map_btn_locate);
        executeTabSelect(0);
        RobotiumHelper.waitForListRefreshingDone(solo, POIsListWorkerFragment.TAG);
        solo.sleep(500);

        solo.clickInList(4);
        solo.waitForActivity("POIDetailActivity");
        solo.assertCurrentActivity("detail activity", POIDetailActivity.class);
        solo.waitForFragmentByTag(POIDetailFragment.TAG);

        RobotiumHelper.selectWheelchairState(solo, "POIDetailActivity");
        solo.waitForActivity("POIDetailActivity");
        solo.goBack();
        solo.waitForActivity("MainSinglePaneActivity");
        solo.assertCurrentActivity("main activity",
                MainSinglePaneActivity.class);
    }

    public void testDNewItem() throws Exception {
        getActivity();
        solo.waitForActivity("MainSinglePaneActivity");
        Log.d(TAG, "current activity 1 = " + solo.getCurrentActivity());
        executeTabSelect(0);
        RobotiumHelper.waitForListRefreshingDone(solo, POIsListWorkerFragment.TAG);

        clickOnActionbar(R.id.menu_new_poi);
        solo.waitForActivity("POIDetailEditableActivity");
        solo.assertCurrentActivity("want edit activity",
                POIDetailEditableActivity.class);

        RobotiumHelper.login(solo);
        solo.sleep(500);

        EditText et = (EditText)RobotiumHelper.findViewById( solo, R.id.name);
        solo.clearEditText(et);
        solo.enterText(et, "testtest");

        RobotiumHelper.selectWheelchairState(solo, "POIDetailEditableActivity");
        RobotiumHelper.selectCategoryState(solo);
        solo.waitForFragmentByTag(POIDetailEditableFragment.TAG);
        solo.clickOnActionBarItem(R.id.menu_save);
        solo.sleep(2000);
        String buttonOkay = getActivity().getString(R.string.btn_okay);
        solo.clickOnButton(buttonOkay);
        solo.waitForDialogToClose(1000);

        RobotiumHelper.logout(solo, "MainSinglePaneActivity");

    }

    public void testEEditItem() throws Exception {

        executeTabSelect(0);
        RobotiumHelper.waitForListRefreshingDone(solo, POIsListWorkerFragment.TAG);

        solo.clickInList(4);
        solo.waitForActivity("POIDetailActivity");
        solo.assertCurrentActivity("want detail activity",
                POIDetailActivity.class);
        solo.waitForFragmentByTag(POIDetailFragment.TAG);

        // clickOnActionbar(R.id.menu_edit);
        solo.sleep(500);
        RobotiumHelper.login(solo);

        solo.waitForActivity("POIDetailEditableActivity");
        solo.assertCurrentActivity("want edit activity",
                POIDetailEditableActivity.class);
        Log.d(TAG, "activity = " + solo.getCurrentActivity().toString());

        EditText et = (EditText)RobotiumHelper.findViewById( solo, R.id.name);
        solo.clearEditText(et);
        solo.enterText(et, "testtest");

        RobotiumHelper.selectWheelchairState(solo, "POIDetailEditableActivity");
        RobotiumHelper.selectCategoryState(solo);
        solo.waitForFragmentByTag(POIDetailEditableFragment.TAG);
        clickOnActionbar(R.id.menu_save);

        getInstrumentation().waitForIdleSync();

        // solo.waitForActivity("POIDetailActivity");
        // solo.assertCurrentActivity("Want detail activity",
        //		POIDetailActivity.class);
        solo.waitForText("testtest");

        solo.goBack();
        solo.waitForActivity("MainSinglePaneActivity");
        solo.assertCurrentActivity("Want main activity",
                MainSinglePaneActivity.class);

        RobotiumHelper.logout(solo, "MainSinglePaneActivity");

    }

    public void testFSearch() throws Exception {

        RobotiumHelper.searchTestList(solo, POIsListWorkerFragment.TAG);

    }

    private static class FragmentId {

        int tab;

        String displayTag;

        String workerTag;

        FragmentId(int tab, String displayTag, String workerTag) {
            this.tab = tab;
            this.displayTag = displayTag;
            this.workerTag = workerTag;
        }
    }
}
