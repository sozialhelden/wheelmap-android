package org.wheelmap.android.fragment;

import android.location.Location;

public interface DisplayFragment {
	public void onUpdate(WorkerFragment fragment);

	public void setCurrentLocation(Location location);
}
