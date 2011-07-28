package org.wheelmap.android.utils;


import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.maps.GeoPoint;

public class ParceableBoundingBox implements Parcelable, Serializable {
	
	static final long serialVersionUID = 2L;
	
	protected final int mLatNorthE6;
	protected final int mLatSouthE6;
	protected final int mLonEastE6;
	protected final int mLonWestE6;

	public ParceableBoundingBox(final int northE6, final int eastE6, final int southE6, final int westE6) {
		this.mLatNorthE6 = northE6;
		this.mLonEastE6 = eastE6;
		this.mLatSouthE6 = southE6;
		this.mLonWestE6 = westE6;
	}


	/**
	 * @return GeoPoint center of this BoundingBox
	 */
	public GeoPoint getCenter() {
		return new GeoPoint((this.mLatNorthE6 + this.mLatSouthE6) / 2,
				(this.mLonEastE6 + this.mLonWestE6) / 2);
	}


	public int getLatNorthE6() {
		return this.mLatNorthE6;
	}

	public int getLatSouthE6() {
		return this.mLatSouthE6;
	}

	public int getLonEastE6() {
		return this.mLonEastE6;
	}

	public int getLonWestE6() {
		return this.mLonWestE6;
	}
	
	public double getLatNorth() {
		return (this.mLatNorthE6 / 1E6);
	}

	public double getLatSouth() {
		return (this.mLatSouthE6 / 1E6);
	}

	public double getLonEast() {
		return (this.mLonEastE6 / 1E6);
	}

	public double getLonWest() {
		return (this.mLonWestE6 / 1E6);
	}
	

	public int getLatitudeSpanE6() {
		return Math.abs(this.mLatNorthE6 - this.mLatSouthE6);
	}

	public int getLongitudeSpanE6() {
		return Math.abs(this.mLonEastE6 - this.mLonWestE6);
	}

	@Override
	public String toString() {
		return new StringBuffer().append("N:").append(this.mLatNorthE6).append("; E:")
		.append(this.mLonEastE6).append("; S:").append(this.mLatSouthE6).append("; W:")
		.append(this.mLonWestE6).toString();
	}


	// ===========================================================
	// Parcelable
	// ===========================================================

	public static final Parcelable.Creator<ParceableBoundingBox> CREATOR = new Parcelable.Creator<ParceableBoundingBox>() {
		@Override
		public ParceableBoundingBox createFromParcel(final Parcel in) {
			return readFromParcel(in);
		}

		@Override
		public ParceableBoundingBox[] newArray(final int size) {
			return new ParceableBoundingBox[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel out, final int arg1) {
		out.writeInt(this.mLatNorthE6);
		out.writeInt(this.mLonEastE6);
		out.writeInt(this.mLatSouthE6);
		out.writeInt(this.mLonWestE6);
	}

	private static ParceableBoundingBox readFromParcel(final Parcel in) {
		final int latNorthE6 = in.readInt();
		final int lonEastE6 = in.readInt();
		final int latSouthE6 = in.readInt();
		final int lonWestE6 = in.readInt();
		return new ParceableBoundingBox(latNorthE6, lonEastE6, latSouthE6, lonWestE6);
	}
}
