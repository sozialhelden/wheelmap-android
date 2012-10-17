package org.wheelmap.android.test;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.ImageView;
import com.jayway.android.robotium.solo.Solo;
import org.wheelmap.android.activity.MainMultiPaneActivity;
import org.wheelmap.android.activity.POIDetailEditableActivity;
import org.wheelmap.android.fragment.*;
import org.wheelmap.android.online.R;

public class MainMultiPaneTest extends
		ActivityInstrumentationTestCase2<MainMultiPaneActivity> {

	private static final String TAG = MainMultiPaneTest.class.getSimpleName();
	private final static int WAIT_IN_SECONDS_TO_FINISH = 60;

	private Solo solo;
	private Object mutex = new Object();

	public MainMultiPaneTest() {
		super(MainMultiPaneActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
		super.tearDown();
		solo = null;
	}

	private void myWait(long microseconds) throws InterruptedException {
		synchronized (mutex) {
			mutex.wait(microseconds);
		}
	}


	public void testAActivityGettingStarted() throws Exception {
		RobotiumHelper.waitForListRefreshingDone( solo, CombinedWorkerFragment.TAG );

		solo.clickInList(1);
		myWait(2000);

		ImageView moveButton = (ImageView)getActivity().findViewById(R.id.button_movable_resize);
		solo.clickOnView(moveButton);
		myWait(500);
		solo.clickOnView(moveButton);
		myWait(500);
		solo.clickInList( 4 );
	}


	public void testBNewItem() throws Exception {
		RobotiumHelper.waitForListRefreshingDone(solo, CombinedWorkerFragment.TAG);

		solo.clickOnActionBarItem(R.id.menu_location);
		solo.clickOnActionBarItem(R.id.menu_new_poi);

		myWait(1000);
		RobotiumHelper.login( solo );

		solo.waitForActivity("POIDetailEditableActivity");
		solo.assertCurrentActivity("want edit activity",
				POIDetailEditableActivity.class);
		solo.clearEditText(0);
		solo.enterText(0, "testtest");
		RobotiumHelper.selectWheelchairState(solo);
		RobotiumHelper.selectCategoryState(solo);
		solo.clickOnActionBarItem(R.id.menu_save);
		solo.clickOnButton( "Okay" );

		RobotiumHelper.logout(solo, "MainMultiPaneActivity");

	}

	public void testCListAndEditorFragment()
			throws Exception {
		RobotiumHelper.waitForListRefreshingDone( solo, CombinedWorkerFragment.TAG);
		RobotiumHelper.login(solo);

		solo.clickInList(2);
		solo.clickOnActionBarItem(R.id.menu_edit );

		solo.waitForActivity("POIDetailEditableActivity");
		solo.assertCurrentActivity("detail activity", POIDetailEditableActivity.class);
		solo.clearEditText(0);
		solo.enterText(0, "testtest");
		RobotiumHelper.selectWheelchairState(solo);
		RobotiumHelper.selectCategoryState(solo);

		solo.clickOnActionBarItem(R.id.menu_save );
		solo.waitForActivity("MainMultiPaneActivity" );

		solo.clickInList(5);
		solo.clickOnActionBarItem(R.id.menu_edit );

		solo.waitForActivity("POIDetailEditableActivity");
		solo.assertCurrentActivity("detail activity", POIDetailEditableActivity.class);
		solo.clearEditText(0);
		solo.enterText(0, "testtest");
		RobotiumHelper.selectCategoryState(solo);
		RobotiumHelper.selectCategoryState(solo);

		solo.clickOnActionBarItem(R.id.menu_save );
		solo.waitForActivity("MainMultiPaneActivity");
		solo.assertCurrentActivity("main activity",
				MainMultiPaneActivity.class);

		RobotiumHelper.logout(solo, "MainMultiPaneActivity");

	}
}
