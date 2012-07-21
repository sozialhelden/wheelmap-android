package org.wheelmap.android.fragment;

import org.wheelmap.android.service.SyncServiceException;

public interface WorkerFragmentListener {
	public void onError(SyncServiceException e);

	public void onSearchModeChange(boolean isSearchMode);

}
