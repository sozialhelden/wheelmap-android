package org.wheelmap.android.fragment;

import android.database.Cursor;
import android.os.Bundle;

public interface WorkerFragment {
	public void registerDisplayFragment(DisplayFragment fragment);

	public void unregisterDisplayFragment(DisplayFragment fragment);

	public void requestUpdate(Bundle bundle);

	public void requestSearch(Bundle bundle);

	public Cursor getCursor();

	public boolean isRefreshing();

	public boolean isSearchMode();

	public void setSearchMode(boolean isSearchMode);

}
