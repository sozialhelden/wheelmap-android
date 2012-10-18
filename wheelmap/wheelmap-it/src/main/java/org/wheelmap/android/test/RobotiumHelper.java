package org.wheelmap.android.test;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import com.jayway.android.robotium.solo.Solo;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import org.wheelmap.android.activity.POIDetailEditableActivity;
import org.wheelmap.android.app.UserCredentials;
import org.wheelmap.android.fragment.NodetypeSelectFragment;
import org.wheelmap.android.fragment.POIDetailEditableFragment;
import org.wheelmap.android.fragment.WorkerFragment;
import org.wheelmap.android.online.R;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class RobotiumHelper {
	private final static String TAG = RobotiumHelper.class.getSimpleName();

	private final static int WAIT_IN_SECONDS_TO_FINISH = 60;
	private static Object mutex = new Object();

	private static void myWait(long microseconds) throws InterruptedException {
		synchronized (mutex) {
			mutex.wait(microseconds);
		}
	}

	static void waitForListRefreshingDone(final Solo solo,
			final String FRAGMENTTAG) throws Exception {
		final FragmentActivity a = (FragmentActivity) solo.getCurrentActivity();
		Callable<Boolean> refreshingDoneCallable = new Callable<Boolean>() {
			WorkerFragment f = (WorkerFragment) a.getSupportFragmentManager()
					.findFragmentByTag(FRAGMENTTAG);

			@Override
			public Boolean call() throws Exception {
				return !f.isRefreshing();
			}
		};

		Awaitility
				.await()
				.atMost(new Duration(WAIT_IN_SECONDS_TO_FINISH,
						TimeUnit.SECONDS)).and().until(refreshingDoneCallable);
		myWait( 500 );

	}

	static void  logout(Solo solo, String mainActivity) throws InterruptedException {
		UserCredentials c = new UserCredentials(solo.getCurrentActivity());
		if (!c.isLoggedIn())
			return;
		Log.d(TAG, "is logged in - logging out");

		solo.clickOnActionBarItem(R.id.menu_filter);
		solo.waitForActivity("NewSettingsActivity");
		myWait(2000);

		// This is to click the last item in the list, which is logout
		while (solo.scrollDown())
			;
		solo.clickInList(7);
		solo.goBack();
		solo.waitForActivity(mainActivity);
	}

	static void  login(Solo solo) {
		UserCredentials c = new UserCredentials(solo.getCurrentActivity());
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
	}

	static void selectWheelchairState( Solo solo) throws InterruptedException {

		View wheelchairStateButton = solo.getCurrentActivity().findViewById(R.id.wheelchair_state_layout );
		solo.clickOnView( wheelchairStateButton );
		Log.d( TAG, "wheelchairState Button " + wheelchairStateButton);

		Log.d( TAG, "Current activity " + solo.getCurrentActivity());
		solo.waitForView( RadioButton.class );
		RadioButton radioButton = (RadioButton) solo.getCurrentActivity().findViewById(R.id.radio_limited );
		Log.d( TAG, "radio button" + radioButton );
		solo.clickOnRadioButton(1);
		solo.waitForFragmentByTag(POIDetailEditableFragment.TAG );

	}

	static void selectCategoryState( Solo solo) throws InterruptedException {

		View categoryButton = solo.getCurrentActivity().findViewById(R.id.edit_nodetype );
		solo.clickOnView( categoryButton );
		solo.waitForFragmentByTag( NodetypeSelectFragment.TAG );
		myWait(1000);
		solo.clickInList( 2 );
		solo.waitForFragmentByTag(POIDetailEditableFragment.TAG );

	}

}
