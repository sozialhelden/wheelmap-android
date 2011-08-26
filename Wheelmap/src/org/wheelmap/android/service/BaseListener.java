package org.wheelmap.android.service;

public interface BaseListener {
	public void setListener( BaseListener listener );
	public void onRunning();
	public void onFinished();
}
