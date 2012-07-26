package org.wheelmap.android.service;

import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Extra.What;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;

public class SyncServiceHelper {

	public static void executeRequest(Context context, Bundle bundle) {
		final Intent intent = new Intent(Intent.ACTION_SYNC, null, context,
				SyncService.class);
		intent.putExtras(bundle);
		context.startService(intent);
	}

	public static void retrieveNode(Context context, String id,
			ResultReceiver receiver) {
		final Intent intent = new Intent(Intent.ACTION_SYNC, null, context,
				SyncService.class);
		intent.putExtra(Extra.WHAT, What.RETRIEVE_NODE);
		intent.putExtra(Extra.WM_ID, id);
		intent.putExtra(Extra.STATUS_RECEIVER, receiver);
		context.startService(intent);
	}

	public static void retrieveNodesByDistance(Context context,
			Location location, float distance, ResultReceiver receiver) {
		final Intent intent = new Intent(Intent.ACTION_SYNC, null, context,
				SyncService.class);
		intent.putExtra(Extra.WHAT, What.RETRIEVE_NODES);
		intent.putExtra(Extra.STATUS_RECEIVER, receiver);
		intent.putExtra(Extra.LOCATION, location);
		intent.putExtra(Extra.DISTANCE_LIMIT, distance);
		context.startService(intent);
	}

	public static void executeUpdateServer(Context context,
			ResultReceiver receiver) {
		final Intent intent = new Intent(Intent.ACTION_SYNC, null, context,
				SyncService.class);
		intent.putExtra(Extra.WHAT, What.UPDATE_SERVER);
		context.startService(intent);
	}

	public static void executeRetrieveApiKey(Context context, String email,
			String password, ResultReceiver receiver) {

		Intent intent = new Intent(Intent.ACTION_SYNC, null, context,
				SyncService.class);
		intent.putExtra(Extra.WHAT, What.RETRIEVE_APIKEY);
		intent.putExtra(Extra.STATUS_RECEIVER, receiver);
		intent.putExtra(Extra.EMAIL, email);
		intent.putExtra(Extra.PASSWORD, password);
		context.startService(intent);
	}

}
