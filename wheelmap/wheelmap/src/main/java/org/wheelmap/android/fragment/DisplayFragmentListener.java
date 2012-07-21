package org.wheelmap.android.fragment;

public interface DisplayFragmentListener {
	public void onShowDetail(long id, String wmId);

	public void onRefreshing(boolean isRefreshing);
}
