package org.wheelmap.android.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * Proxy {@link ResultReceiver} that offers a listener interface that can be
 * detached. Useful for when sending callbacks to a {@link Service} where a
 * listening {@link Activity} can be swapped out during configuration changes.
 */
public class DetachableResultReceiver extends ResultReceiver {
	private static final String TAG = "DetachableResultReceiver";

	private Receiver mReceiver;

	public DetachableResultReceiver(Handler handler) {
		super(handler);
	}

	public void clearReceiver() {
		mReceiver = null;
	}

	public void setReceiver(Receiver receiver) {
		mReceiver = receiver;
	}

	public interface Receiver {
		public void onReceiveResult(int resultCode, Bundle resultData);
	}

	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData) {
		if (mReceiver != null) {
			mReceiver.onReceiveResult(resultCode, resultData);
		} else {
			Log.w(TAG, "Dropping result on floor for code " + resultCode + ": "
					+ resultData.toString());
		}
	}
}