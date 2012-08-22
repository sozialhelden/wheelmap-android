/*
 * #%L
 * Wheelmap - App
 * %%
 * Copyright (C) 2011 - 2012 Michal Harakal - Michael Kroez - Sozialhelden e.V.
 * %%
 * Wheelmap App based on the Wheelmap Service by Sozialhelden e.V.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wheelmap.android.net;

import org.wheelmap.android.app.AppProperties;
import org.wheelmap.android.app.IAppProperties;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Extra.What;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.request.IHttpUserAgent;

import wheelmap.org.request.RequestProcessor;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

public abstract class AbstractExecutor implements IExecutor {

	private final Context mContext;
	private final Bundle mBundle;
	private IAppProperties mAppProperties;

	
	protected final static int statusAuthRequired = 401;
	protected final static int statusRequestForbidden = 403;

	protected static final String API_KEY = "jWeAsb34CJq4yVAryjtc";
	protected static RequestProcessor mRequestProcessor = new RequestProcessor();

    public AbstractExecutor(Context context, Bundle bundle) {
		mContext = context;
		mBundle = bundle;
	}
	
	@Override
    public void setAppProperties(IAppProperties appProperties) {
		mAppProperties = appProperties;
	}
	
	@Override
	public void setUserAgent(String userAgent)
	{
       mRequestProcessor.setUserAgent(userAgent);		
	}


	public String getApiKey() {
		return API_KEY;
	}

	protected Context getContext() {
		return mContext;
	}

	protected ContentResolver getResolver() {
		return mContext.getContentResolver();
	}

	protected Bundle getBundle() {
		return mBundle;
	}

	public abstract void prepareContent();

	public abstract void execute() throws SyncServiceException;

	public abstract void prepareDatabase() throws SyncServiceException;

	public static IExecutor create(Context context, Bundle bundle, IAppProperties appProperties, IHttpUserAgent httpUserAgent) {
		if (bundle == null || !bundle.containsKey(Extra.WHAT))
			return null;

		int what = bundle.getInt(Extra.WHAT);
		IExecutor executor;
		switch (what) {
		case What.RETRIEVE_NODE:
			executor = new NodeExecutor(context, bundle);
			break;
		case What.RETRIEVE_NODES:
		case What.SEARCH_NODES:
		case What.SEARCH_NODES_IN_BOX:
			executor = new NodesExecutor(context, bundle);
			break;
		case What.RETRIEVE_LOCALES:
			executor = new LocalesExecutor(context, bundle);
			break;
		case What.RETRIEVE_CATEGORIES:
			executor = new CategoriesExecutor(context, bundle);
			break;
		case What.RETRIEVE_NODETYPES:
			executor = new NodeTypesExecutor(context, bundle);
			break;
		case What.UPDATE_SERVER:
			executor = new NodeUpdateOrNewExecutor(context);
			break;
		case What.RETRIEVE_APIKEY:
			executor = new ApiKeyExecutor(context, bundle);
			break;
		default:
			return null; // noop no instruction, no operation;
		}
		executor.setAppProperties(appProperties);
		executor.setUserAgent(httpUserAgent.getAppUserAgent());

		return executor;
	}
	
	@Override
	public String getServer()
	{
		return mAppProperties.get(AppProperties.KEY_WHEELMAP_URI);
	}

	protected String getTag() {
		return this.getClass().getSimpleName();
	}

}
