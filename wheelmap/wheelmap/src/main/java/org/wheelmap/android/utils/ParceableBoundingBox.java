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


import java.io.Serializable;

import org.mapsforge.android.maps.GeoPoint;

import wheelmap.org.BoundingBox;
import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;

import android.os.Parcel;
import android.os.Parcelable;

public class ParceableBoundingBox implements Parcelable, Serializable {

	private static final long serialVersionUID = 1566826330658318160L;
	
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
	
	public ParceableBoundingBox(final BoundingBox bb) {		
		this.mLatNorthE6 = (int)(bb.getEastNorth().latitude * 1E6);
		this.mLonEastE6 = (int)(bb.getEastNorth().longitude * 1E6);
		this.mLatSouthE6 = (int)(bb.getWestSouth().latitude * 1E6);
		this.mLonWestE6 = (int)(bb.getWestSouth().latitude * 1E6);
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
	
	public BoundingBox toBoundingBox() {
		return new BoundingBox(
        		new Wgs84GeoCoordinates(mLonWestE6 / 1E6, mLatSouthE6 / 1E6), 
        		new Wgs84GeoCoordinates(mLonEastE6 / 1E6, mLatNorthE6 / 1E6));
	}
}
