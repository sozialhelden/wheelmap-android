package org.wheelmap.android.net;

import wheelmap.org.request.RequestProcessor;
import android.content.ContentResolver;
import android.os.Bundle;

public abstract class AbstractExecutor implements IExecutor {
	protected static final String TAG = "executor";
	
	private final ContentResolver mResolver;
	private final Bundle mBundle;

	protected static final String SERVER = "staging.wheelmap.org";
	protected static final String API_KEY = "jWeAsb34CJq4yVAryjtc";
	protected static RequestProcessor mRequestProcessor = new RequestProcessor();
	
	public AbstractExecutor( ContentResolver resolver, Bundle bundle ) {
		mResolver = resolver;
		mBundle = bundle;
	}
	
	public String getApiKey() {
		return "jWeAsb34CJq4yVAryjtc";
	}
	
	protected ContentResolver getResolver() {
		return mResolver;
	}
	
	protected Bundle getBundle() {
		return mBundle;
	}
	
	public abstract void prepareContent();
	public abstract void execute() throws ExecutorException;
	public abstract void prepareDatabase();

}

