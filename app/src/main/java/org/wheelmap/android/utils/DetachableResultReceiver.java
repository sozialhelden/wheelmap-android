/*
 * #%L
 * Wheelmap - App
 * %%
 * Copyright (C) 2011 - 2012 Michal Harakal - Michael Kroez - Sozialhelden e.V.
 * %%
 * Wheelmap App based on the Wheelmap Service by Sozialhelden e.V.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wheelmap.android.utils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * Proxy {@link ResultReceiver} that offers a listener interface that can be detached. Useful for
 * when sending callbacks to a {@link Service} where a listening {@link Activity} can be swapped out
 * during configuration changes.
 */
public class DetachableResultReceiver extends ResultReceiver {

    private static final String TAG = "DetachableResultReceiver";

    private Receiver mReceiver;

    private int resultCode;

    private Bundle resultData;

    public DetachableResultReceiver(Handler handler) {
        super(handler);
    }

    public void clearReceiver() {
        mReceiver = null;
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    public void setReceiver(Receiver receiver, boolean resendLast) {
        mReceiver = receiver;
        if (resendLast) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }

    public interface Receiver {

        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        this.resultCode = resultCode;
        this.resultData = resultData;
        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}
