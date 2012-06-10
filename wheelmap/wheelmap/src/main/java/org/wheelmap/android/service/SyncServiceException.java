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

import java.io.Serializable;

import org.wheelmap.android.online.R;

import android.os.Parcel;
import android.os.Parcelable;

public class SyncServiceException extends RuntimeException implements Parcelable, Serializable {

	private static final long serialVersionUID = -7198971477542767531L;
	private final int id;

	public final static int ERROR_NETWORK_FAILURE = 0x0;
	public final static int ERROR_INTERNAL_ERROR = 0x1;
	public final static int ERROR_AUTHORIZATION_ERROR = 0x2;
	public final static int ERROR_NOT_OSM_CONNECTED = 0x3;
	public final static int ERROR_AUTHORIZATION_REQUIRED = 0x4;
	public final static int ERROR_REQUEST_FORBIDDEN = 0x5;
	
	private final int[] errorString = {
			R.string.error_network_failure,
			R.string.error_internal_error,
			R.string.error_authorization_error,
			R.string.error_not_osm_connected,
			R.string.error_authorization_required,
			R.string.error_request_forbidden,
	};

	public SyncServiceException(int id, Throwable t ) {
		super( t );
		this.id = id;
	}
	
	public int getErrorCode() {
		return this.id;
	}
	
	public int getRessourceString() {
		return errorString[id];
	}
	
	public static final Parcelable.Creator<SyncServiceException> CREATOR = new Parcelable.Creator<SyncServiceException>() {
		@Override
		public SyncServiceException createFromParcel(final Parcel in) {
			return readFromParcel(in);
		}

		@Override
		public SyncServiceException[] newArray(final int size) {
			return new SyncServiceException[size];
		}
	};
	
	@Override
	public void writeToParcel(final Parcel out, final int arg1) {
		out.writeInt( this.id );
		out.writeSerializable( getCause() );
	}

	private static SyncServiceException readFromParcel(final Parcel in) {
		final int errorId = in.readInt();
		final Throwable t = (Throwable) in.readSerializable();
		
		return new SyncServiceException( errorId, t );
	}

	@Override
	public int describeContents() {
		return 0;
	}
}
