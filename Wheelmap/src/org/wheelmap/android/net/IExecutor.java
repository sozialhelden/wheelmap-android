package org.wheelmap.android.net;

import org.wheelmap.android.service.SyncServiceException;

public interface IExecutor {
	public void prepareContent();

	public void execute() throws SyncServiceException;
	public void prepareDatabase() throws SyncServiceException;
}
