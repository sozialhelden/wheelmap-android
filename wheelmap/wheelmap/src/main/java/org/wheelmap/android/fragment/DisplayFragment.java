package org.wheelmap.android.fragment;

import org.mapsforge.android.maps.GeoPoint;

import android.location.Location;

public interface DisplayFragment {
	public void onUpdate(WorkerFragment fragment);

	public void setCurrentLocation(GeoPoint point, Location location);
}
