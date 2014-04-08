package org.wheelmap.android.app;

import com.urbanairship.actions.ActionUtils;
import com.urbanairship.actions.DeepLinkAction;
import com.urbanairship.actions.LandingPageAction;
import com.urbanairship.actions.OpenExternalUrlAction;
import com.urbanairship.push.PushManager;

import org.wheelmap.android.activity.StartupActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class IntentReceiver extends BroadcastReceiver {
    // A set of actions that launch activities when a push is opened. Update
    // with any custom actions that also start activities when a push is opened.
    private static String[] ACTIVITY_ACTIONS = new String[] {
        DeepLinkAction.DEFAULT_REGISTRY_NAME,
        OpenExternalUrlAction.DEFAULT_REGISTRY_NAME,
        LandingPageAction.DEFAULT_REGISTRY_NAME
    };

    @Override
    public void onReceive(Context context, Intent intent) {

        if (PushManager.ACTION_PUSH_RECEIVED.equals(intent.getAction())) {
          // Push received
        } else if (PushManager.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {

            // Push opened
            // Only launch the main activity if the payload does not contain any
            // actions that might have already opened an activity
            if (!ActionUtils.containsRegisteredActions(intent.getExtras(), ACTIVITY_ACTIONS)) {
                Intent launch = new Intent(Intent.ACTION_MAIN);
                launch.setClass(context, StartupActivity.class);
                launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launch);
            }
        }
    }
}