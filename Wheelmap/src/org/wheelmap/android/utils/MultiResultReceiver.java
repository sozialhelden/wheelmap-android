package org.wheelmap.android.utils;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * Its important, that this {@link ResultReceiver} is intended to live at a
 * {@link Service} and a receiver is just registering there. So multiple
 * activities {@link Activity} can get info about the state of a single service.
 */
public class MultiResultReceiver extends ResultReceiver {
	private static final String TAG = "ResultReceiver";
	private ArrayList<ResultReceiver> mReceivers;
	
	private int mResultCode;
	private Bundle mResultData;

	public MultiResultReceiver(Handler handler) {
		super(handler);
		mReceivers = new ArrayList<ResultReceiver>();
	}

	public void clearReceiver() {
		mReceivers.clear();
	}

	public void addReceiver(ResultReceiver receiver, boolean resentLast) {
		if ( receiver == null )
			return;
		if (!mReceivers.contains(receiver))
			mReceivers.add(receiver);
		
		if ( resentLast )
			receiver.send( mResultCode, mResultData );
	}

	public void removeReceiver(ResultReceiver receiver) {
		mReceivers.remove(receiver);
	}

	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData) {
		boolean sentOnce = false;
		
		mResultCode = resultCode;
		mResultData = resultData;

		for (ResultReceiver receiver : mReceivers) {
			receiver.send( resultCode, resultData );
			sentOnce = true;
		}

		if (!sentOnce)
			Log.w(TAG, "Dropping result on floor for code " + resultCode + ": "
					+ resultData.toString());
	}
}
