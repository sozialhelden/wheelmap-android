package org.wheelmap.android.net;

public interface IExecutor {
	public void prepareContent();

	public void execute() throws ExecutorException;

	public void prepareDatabase() throws ExecutorException;
}
