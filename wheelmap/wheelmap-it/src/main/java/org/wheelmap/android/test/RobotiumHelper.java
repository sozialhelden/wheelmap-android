package org.wheelmap.android.test;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.jayway.android.robotium.solo.Solo;
import com.jayway.android.robotium.solo.SoloCompatibilityAbs;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import org.wheelmap.android.activity.POIDetailEditableActivity;
import org.wheelmap.android.app.UserCredentials;
import org.wheelmap.android.fragment.*;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.UtilsMisc;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class RobotiumHelper {
	private final static String TAG = RobotiumHelper.class.getSimpleName();

	private final static int WAIT_IN_SECONDS_TO_FINISH = 60;

	static void clickOnActionbar( SoloCompatibilityAbs solo, int resId) {
		if ( UtilsMisc.hasHoneycomb())
			solo.clickOnActionBarItem(resId);
		else
			solo.clickOnVisibleActionbarItem(resId);
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
		solo.sleep(500);

	}

	static void  logout(Solo solo, String mainActivity) throws InterruptedException {
		UserCredentials c = new UserCredentials(solo.getCurrentActivity());
		if (!c.isLoggedIn())
			return;
		Log.d(TAG, "is logged in - logging out");

		solo.clickOnActionBarItem(R.id.menu_filter);
		solo.waitForActivity("NewSettingsActivity");
		solo.sleep(2000);

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
		String loginText = solo.getString(R.string.title_login);

		solo.waitForText(loginText);
		EditText emailText = solo.getEditText(0);
		EditText passwordText = solo.getEditText(1);
		solo.enterText(emailText, "rutton@web.de");
		solo.enterText(passwordText, "testtest");

		String loginButtonText = solo.getString(R.string.login_submit);
		solo.clickOnButton(loginButtonText);
		solo.waitForDialogToClose(1000);
	}

	static void selectWheelchairState( Solo solo) throws InterruptedException {

		View wheelchairStateButton = solo.getCurrentActivity().findViewById(R.id.wheelchair_state_layout);
		solo.clickOnView( wheelchairStateButton );
		Log.d(TAG, "wheelchairState Button " + wheelchairStateButton);

		Log.d( TAG, "Current activity " + solo.getCurrentActivity());
		solo.waitForView( RadioButton.class );
		RadioButton radioButton = (RadioButton) solo.getCurrentActivity().findViewById(R.id.radio_limited);
		Log.d(TAG, "radio button" + radioButton);
		solo.clickOnRadioButton(1);
		solo.waitForFragmentByTag(POIDetailEditableFragment.TAG );

	}

	static void selectCategoryState( Solo solo) throws InterruptedException {

		View categoryButton = solo.getCurrentActivity().findViewById(R.id.edit_nodetype );
		solo.clickOnView(categoryButton);
		solo.waitForFragmentByTag(NodetypeSelectFragment.TAG);
		solo.sleep(1000);
		solo.clickInList( 2 );
		solo.waitForFragmentByTag(POIDetailEditableFragment.TAG );

	}

	static void searchTestList( SoloCompatibilityAbs solo, String workerTag ) throws Exception {
		waitForListRefreshingDone( solo, workerTag );
		String searchString = solo.getString(R.string.title_search);

		clickOnActionbar(solo, R.id.menu_search);
		solo.waitForText(searchString);
		solo.clickOnButton(0);
		solo.waitForDialogToClose( 1000 );
		waitForListRefreshingDone( solo, workerTag );

		clickOnActionbar(solo, R.id.menu_search);

		solo.waitForText(searchString);
		String categoryString = solo.getString(R.string.search_no_selection );
		solo.clickOnText( categoryString );

		solo.scrollDownList(0);
		solo.scrollDownList(0);
		solo.scrollDownList(0);
		solo.clickInList(2);
		solo.sleep(500);
		solo.clickOnButton( 0 );
		solo.waitForDialogToClose( 1000 );
		waitForListRefreshingDone( solo, workerTag );

		clickOnActionbar(solo, R.id.menu_search);

		solo.waitForText(searchString);
		String distanceString = solo.getCurrentActivity().getResources().getStringArray(R.array.distance_array)[3];
		solo.clickOnText(distanceString);
		solo.clickInList(2);
		solo.sleep(500);
		solo.clickOnButton( 0 );
		solo.waitForDialogToClose( 1000 );
		waitForListRefreshingDone( solo, workerTag );

		clickOnActionbar(solo, R.id.menu_search);

		solo.waitForText(searchString);
		solo.enterText( 0, "Fernsehturm" );

		solo.clickOnText(distanceString);
		solo.scrollUpList(0);
		solo.clickInList(0);
		solo.sleep(500);
		solo.clickOnButton(0);
		solo.waitForDialogToClose( 1000 );
		waitForListRefreshingDone( solo, workerTag );

	}


}
