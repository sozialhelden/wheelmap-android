package org.wheelmap.android.service;

import java.io.Serializable;

import org.wheelmap.android.R;

import android.os.Parcel;
import android.os.Parcelable;

public class SyncServiceException extends RuntimeException implements Parcelable, Serializable {

	private static final long serialVersionUID = -7198971477542767531L;
	private final int id;

	public final static int ERROR_NETWORK_FAILURE = 0x0;
	public final static int ERROR_INTERNAL_ERROR = 0x1;
	public final static int ERROR_AUTHORIZATION_ERROR = 0x2;
	
	private final int[] errorString = {
			R.string.error_network_failure,
			R.string.error_internal_error,
			R.string.error_authorization_error,
			
	};

	public SyncServiceException(int id, Throwable t ) {
		super( t );
		this.id = id;
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
