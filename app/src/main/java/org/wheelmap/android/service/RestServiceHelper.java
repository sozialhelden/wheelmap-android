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
package org.wheelmap.android.service;

import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Extra.What;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.math.BigInteger;

import de.akquinet.android.androlog.Log;

public class RestServiceHelper {

    private final static String TAG = RestServiceHelper.class.getSimpleName();

    public static void executeRequest(Context context, Bundle bundle) {
        Log.d(TAG, "executeRequest what = " + bundle.getInt(Extra.WHAT));
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, context,
                RestService.class);
        intent.putExtras(bundle);
        context.startService(intent);
    }

    public static void retrieveNode(Context context, String id,
            ResultReceiver receiver) {
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, context,
                RestService.class);
        intent.putExtra(Extra.WHAT, What.RETRIEVE_NODE);
        intent.putExtra(Extra.WM_ID, id);
        intent.putExtra(Extra.STATUS_RECEIVER, receiver);
        context.startService(intent);
    }

    public static void retrieveNodesByDistance(Context context,
            Location location, float distance, ResultReceiver receiver) {
        Log.d(TAG, "retrieveNodesByDistance");
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, context,
                RestService.class);
        intent.putExtra(Extra.WHAT, What.RETRIEVE_NODES);
        intent.putExtra(Extra.STATUS_RECEIVER, receiver);
        intent.putExtra(Extra.LOCATION, location);
        intent.putExtra(Extra.DISTANCE_LIMIT, distance);
        context.startService(intent);
    }

    public static void retrievePhotosById(Context context, long id,ResultReceiver receiver) {
        Log.d(TAG, "retrievePhotosByDistance");
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, context,
                RestService.class);
        intent.putExtra(Extra.STATUS_RECEIVER, receiver);
        intent.putExtra(Extra.WHAT, What.RETRIEVE_PHOTO);
        intent.putExtra(Extra.ID, id);
        context.startService(intent);
    }

    public static void executeUploadPhoto(Context context, long id,ResultReceiver receiver) {
        Log.d(TAG, "retrievePhotosByDistance");
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, context,
                RestService.class);
        intent.putExtra(Extra.STATUS_RECEIVER, receiver);
        intent.putExtra(Extra.WHAT, What.UPDATE_PHOTO);
        intent.putExtra(Extra.ID, id);
        context.startService(intent);
    }

    public static void executeUpdateServer(Context context,
            ResultReceiver receiver) {
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, context,
                RestService.class);
        intent.putExtra(Extra.WHAT, What.UPDATE_SERVER);
        intent.putExtra(Extra.STATUS_RECEIVER, receiver);
        context.startService(intent);
    }

    public static void executeRetrieveApiKey(Context context, String email,
            String password, ResultReceiver receiver) {

        Intent intent = new Intent(Intent.ACTION_SYNC, null, context,
                RestService.class);
        intent.putExtra(Extra.WHAT, What.RETRIEVE_APIKEY);
        intent.putExtra(Extra.STATUS_RECEIVER, receiver);
        intent.putExtra(Extra.EMAIL, email);
        intent.putExtra(Extra.PASSWORD, password);
        context.startService(intent);
    }

}
