package org.wheelmap.android.net;

import wheelmap.org.request.RequestProcessor;
import android.content.ContentResolver;
import android.os.Bundle;

public abstract class AbstractExecutor implements IExecutor {
	protected static final String TAG = "executor";
	
	protected ContentResolver mResolver;
	protected Bundle mBundle;

	protected static final String SERVER = "staging.wheelmap.org";
	protected static final String API_KEY = "9NryQWfDWgIebZIdqWiK";
	protected static RequestProcessor mRequestProcessor = new RequestProcessor();
	
	public AbstractExecutor( ContentResolver resolver, Bundle bundle ) {
		mResolver = resolver;
		mBundle = bundle;
	}
	
	public abstract void prepareContent();
	public abstract void execute() throws ExecutorException;
	public abstract void prepareDatabase();

}

