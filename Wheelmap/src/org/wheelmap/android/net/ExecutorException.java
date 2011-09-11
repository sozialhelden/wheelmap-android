package org.wheelmap.android.net;

public class ExecutorException extends RuntimeException {
	private static final long serialVersionUID = -3380111967461722513L;

	public ExecutorException() {
	}

	public ExecutorException(String detailMessage) {
		super(detailMessage);
	}

	public ExecutorException(Throwable throwable) {
		super(throwable);
	}

	public ExecutorException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
