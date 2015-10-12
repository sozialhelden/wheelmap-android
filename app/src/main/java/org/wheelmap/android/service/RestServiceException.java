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

import org.wheelmap.android.online.R;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class RestServiceException extends RuntimeException implements
        Parcelable, Serializable {

    private static final long serialVersionUID = -7198971477542767531L;

    private final int id;

    public static final int ERROR_NETWORK_FAILURE = 0x0;

    public static final int ERROR_INTERNAL_ERROR = 0x1;

    public static final int ERROR_AUTHORIZATION_ERROR = 0x2;

    public static final int ERROR_NOT_OSM_CONNECTED = 0x3;

    public static final int ERROR_AUTHORIZATION_REQUIRED = 0x4;

    public static final int ERROR_AUTHORIZATION_FAILED = 0x5;

    public static final int ERROR_REQUEST_FORBIDDEN = 0x6;

    public static final int ERROR_CLIENT_FAILURE = 0x7;

    public static final int ERROR_SERVER_FAILURE = 0x8;

    public static final int ERROR_SERVER_DOWN = 0x9;

    public static final int ERROR_NETWORK_UNKNOWN_FAILURE = 0x10;

    private final int[] errorString = {R.string.error_network_failure,
            R.string.error_internal_error, R.string.error_authorization_error,
            R.string.error_authorization_not_osm_connected,
            R.string.error_authorization_required,
            R.string.error_authorization_request_failed,
            R.string.error_authorization_request_forbidden,
            R.string.error_network_client_failure,
            R.string.error_network_server_failure,
            R.string.error_network_server_maintenance,
            R.string.error_network_unknown_failure};

    public RestServiceException(int id, Throwable t) {
        super(t);
        this.id = id;
    }

    public int getErrorCode() {
        return this.id;
    }

    public int getRessourceString() {
        if(id >= errorString.length){
          return errorString[ERROR_INTERNAL_ERROR];
        }
        return errorString[id];
    }

    public static final Parcelable.Creator<RestServiceException> CREATOR
            = new Parcelable.Creator<RestServiceException>() {
        @Override
        public RestServiceException createFromParcel(final Parcel in) {
            return readFromParcel(in);
        }

        @Override
        public RestServiceException[] newArray(final int size) {
            return new RestServiceException[size];
        }
    };

    @Override
    public void writeToParcel(final Parcel out, final int arg1) {
        out.writeInt(this.id);
        try{
            out.writeSerializable(getCause());
        }catch(Exception e){
            out.writeSerializable("");
        }
    }

    private static RestServiceException readFromParcel(final Parcel in) {
        final int errorId = in.readInt();
        Serializable s = in.readSerializable();
        if(s != null && s.equals("")){
            return new RestServiceException(errorId,null);
        } else if(s != null){
            final Throwable t = (Throwable) in.readSerializable();
            return new RestServiceException(errorId, t);
        }
        return new RestServiceException(errorId,null);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isNetworkError() {
        return id == ERROR_NETWORK_FAILURE || id == ERROR_SERVER_FAILURE
                || id == ERROR_NETWORK_UNKNOWN_FAILURE;
    }
}
