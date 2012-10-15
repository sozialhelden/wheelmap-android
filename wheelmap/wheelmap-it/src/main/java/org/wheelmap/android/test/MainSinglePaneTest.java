package org.wheelmap.android.test;

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import org.springframework.util.Assert;
import org.wheelmap.android.activity.MainSinglePaneActivity;
import org.wheelmap.android.activity.POIDetailActivity;
import org.wheelmap.android.activity.POIDetailEditableActivity;
import org.wheelmap.android.app.UserCredentials;
import org.wheelmap.android.fragment.POIsListFragment;
import org.wheelmap.android.fragment.POIsListWorkerFragment;
import org.wheelmap.android.fragment.POIsMapsforgeFragment;
import org.wheelmap.android.fragment.POIsMapsforgeWorkerFragment;
import org.wheelmap.android.online.R;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.EditText;

import com.jayway.android.robotium.solo.Solo;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class MainSinglePaneTest extends
		ActivityInstrumentationTestCase2<MainSinglePaneActivity> {

	private static final String TAG = "mainsinglepanetest";
	private static FragmentId[] ids;
	private final static int WAIT_IN_SECONDS_TO_FINISH = 60;

	private static class FragmentId {
		FragmentId(int tab, String displayTag, String workerTag) {
			this.tab = tab;
			this.displayTag = displayTag;
			this.workerTag = workerTag;
		}

		int tab;
		String displayTag;
		String workerTag;
	}

	static {
		ids = new FragmentId[] {
				new FragmentId(MainSinglePaneActivity.TAB_LIST,
						POIsListFragment.TAG, POIsListWorkerFragment.TAG),
				new FragmentId(MainSinglePaneActivity.TAB_MAP,
						POIsMapsforgeFragment.TAG,
						POIsMapsforgeWorkerFragment.TAG) };
	}

	private Solo solo;
	private Object mutex = new Object();

	public MainSinglePaneTest() {
		super(MainSinglePaneActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

	private void myWait(long microseconds) throws InterruptedException {
		synchronized (mutex) {
			mutex.wait(microseconds);
		}
	}

	private void executeTabSelect(final int tabId) {
		final MainSinglePaneActivity activity = (MainSinglePaneActivity) getActivity();
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

	private void waitForListRefreshingDone() throws Exception {
		Callable<Boolean> refreshingDoneCallable = new Callable<Boolean>() {
			final MainSinglePaneActivity activity = (MainSinglePaneActivity) getActivity();
			POIsListWorkerFragment f = (POIsListWorkerFragment) activity
					.getSupportFragmentManager().findFragmentByTag(
							POIsListWorkerFragment.TAG);

			@Override
			public Boolean call() throws Exception {
				return !f.isRefreshing();
			}
		};

		Awaitility
				.await()
				.atMost(new Duration(WAIT_IN_SECONDS_TO_FINISH,
						TimeUnit.SECONDS)).and().until(refreshingDoneCallable);

	}

	public void testAFragmentsGettingStarted() {
		executeTabSelect(1);
		executeTabSelect(0);
		executeTabSelect(1);
	}

	public void testBListAndDetailFragment()
			throws Exception {
		executeTabSelect(0);
		waitForListRefreshingDone();

		solo.clickInList(1);
		solo.waitForActivity("POIDetailActivity");
		solo.assertCurrentActivity("detail activity", POIDetailActivity.class);

		solo.goBack();
		solo.waitForActivity("MainSinglePaneActivity");
		solo.assertCurrentActivity("main activity",
				MainSinglePaneActivity.class);
		solo.finishOpenedActivities();

	}

	public void testCMapListAndDetailFragment() throws Exception {
		solo.waitForActivity("MainSinglePaneActivity" );
		// executeTabSelect(1);
		executeTabSelect(0);
		waitForListRefreshingDone();

		solo.clickInList(4);
		solo.waitForActivity("POIDetailActivity");
		solo.assertCurrentActivity("detail activity", POIDetailActivity.class);

		solo.goBack();
		solo.waitForActivity("MainSinglePaneActivity");
		solo.assertCurrentActivity("main activity",
		MainSinglePaneActivity.class);
	}

	private void logout() throws InterruptedException {
		UserCredentials c = new UserCredentials(getActivity());
		if (!c.isLoggedIn())
			return;
		Log.d(TAG, "is logged in - logging out");

		if (solo.getCurrentActivity() != getActivity()) {
			solo.goBackToActivity("MainSinglePaneActivity");
		}
		solo.clickOnActionBarItem(R.id.menu_filter);
		solo.waitForActivity("NewSettingsActivity");
		myWait(3000);

		// This is to click the last item in the list, which is logout
		while (solo.scrollDown())
			;
		solo.clickInList(7);
		solo.goBack();
		solo.waitForActivity("MainSinglePaneActivity");
	}

	private void login() {
		UserCredentials c = new UserCredentials(getActivity());
		if (c.isLoggedIn())
			return;

		Log.d(TAG, "is not logged in - logging in");

		solo.waitForText("Login");
		EditText emailText = solo.getEditText(0);
		EditText passwordText = solo.getEditText(1);
		solo.enterText(emailText, "rutton.r@gmail.com");
		solo.enterText(passwordText, "testtest");
		solo.clickOnButton("Login");
		solo.waitForDialogToClose(1000);
		// solo.clickOnView(solo.getView(android.R.id.button1));
	}

	public void testDNewItem() throws InterruptedException {
		executeTabSelect(1);

		solo.clickOnActionBarItem(R.id.menu_location);
		solo.clickOnActionBarItem(R.id.menu_new_poi);

		myWait(1000);
		login();

		solo.waitForActivity("POIDetailEditableActivity");
		solo.assertCurrentActivity("want edit activity",
				POIDetailEditableActivity.class);
		solo.clearEditText(0);
		solo.enterText(0, "testtest");
		solo.clickOnActionBarItem(R.id.menu_save);

		logout();

	}

	public void testEEditItem() throws Exception {

		executeTabSelect(0);
		waitForListRefreshingDone();

		solo.clickInList(4);
		solo.waitForActivity("POIDetailActivity");
		solo.assertCurrentActivity("want detail activity",
				POIDetailActivity.class);

		myWait(1000);
		solo.clickOnActionBarItem(R.id.menu_edit);
		myWait(1000);
		login();

		solo.waitForActivity("POIDetailEditableActivity");
		solo.assertCurrentActivity("want edit activity",
				POIDetailEditableActivity.class);
		Log.d(TAG, "activity = " + solo.getCurrentActivity().toString());
		solo.clearEditText(0);
		solo.enterText(0, "testtest");
		solo.clickOnActionBarItem(R.id.menu_save);

		solo.waitForActivity("POIDetailActivity");
		solo.assertCurrentActivity("Want detail activity",
				POIDetailActivity.class);
		solo.waitForText("testtest");

		solo.goBack();
		solo.waitForActivity("MainSinglePaneActivity");
		solo.assertCurrentActivity("Want main activity",
				MainSinglePaneActivity.class);

		logout();

	}
}
