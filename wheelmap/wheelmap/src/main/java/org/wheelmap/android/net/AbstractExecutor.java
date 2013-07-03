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

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriUtils;
import org.wheelmap.android.mapping.Base;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Extra.What;
import org.wheelmap.android.modules.IAppProperties;
import org.wheelmap.android.modules.ICredentials;
import org.wheelmap.android.modules.IHttpUserAgent;
import org.wheelmap.android.net.request.RequestBuilder;
import org.wheelmap.android.net.request.RequestProcessor;
import org.wheelmap.android.service.RestServiceException;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import de.akquinet.android.androlog.Log;
import roboguice.RoboGuice;

public abstract class AbstractExecutor<T extends Base> implements IExecutor {

    private final static int statusBadRequest = 400;

    private final static int statusAuthRequired = 401;

    private final static int statusRequestForbidden = 403;

    private final static int statusNotFound = 404;

    private final static int statusNotAcceptable = 406;

    private final static int statusInternalServerError = 500;

    private final static int statusDownMaintenance = 503;

    private final int fMaxRetryCount;

    private final Context mContext;

    private final Bundle mBundle;

    private final Class<T> mClazz;

    protected IAppProperties mAppProperties;

    protected ICredentials mCredentials;

    protected final static RequestProcessor mRequestProcessor = new RequestProcessor();

    public AbstractExecutor(Context context, Bundle bundle, Class<T> clazz,
            int maxRetryCount) {
        mContext = context;
        mBundle = bundle;
        mClazz = clazz;
        fMaxRetryCount = maxRetryCount;
        RoboGuice.injectMembers(context, this);
    }

    @Override
    public void setAppProperties(IAppProperties appProperties) {
        mAppProperties = appProperties;
    }

    @Override
    public void setCredentials(ICredentials credentials) {
        mCredentials = credentials;
    }

    @Override
    public void setUserAgent(String userAgent) {
        mRequestProcessor.setUserAgent(userAgent);
    }

    protected String getEtag() {
        return mRequestProcessor.getEtag();
    }

    protected void setEtag(String etag) {
        mRequestProcessor.setEtag(etag);
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

    public abstract void execute() throws RestServiceException;

    public abstract void prepareDatabase() throws RestServiceException;

    public static IExecutor create(Context context, Bundle bundle, IAppProperties appProperties,
            ICredentials credentials, IHttpUserAgent httpUserAgent) {
        if (bundle == null || !bundle.containsKey(Extra.WHAT)) {
            return null;
        }

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
        executor.setCredentials(credentials);
        executor.setUserAgent(httpUserAgent.getAppUserAgent());

        return executor;
    }

    @Override
    public String getServer() {
        return mAppProperties.get(IAppProperties.KEY_WHEELMAP_URI);
    }

    protected String getTag() {
        return getClass().getSimpleName();
    }

    protected String getApiKey() {
        return mCredentials.getApiKey();
    }

    @SuppressWarnings("unchecked")
    protected T executeRequest(RequestBuilder requestBuilder)
            throws RestServiceException {
        T content = null;

        String request;
        try {
            request = UriUtils.encodeQuery(requestBuilder.buildRequestUri(),
                    "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RestServiceException(
                    RestServiceException.ERROR_INTERNAL_ERROR, e);
        }

        int retryCount = 0;
        while (retryCount < fMaxRetryCount) {
            try {
                if (requestBuilder.getRequestType() == RequestBuilder.REQUEST_GET) {
                    Log.d(getTag(), "getRequest = *" + request + "*");
                    content = mRequestProcessor.get(new URI(request), mClazz);
                } else if (requestBuilder.getRequestType() == RequestBuilder.REQUEST_POST) {
                    Log.d(getTag(), "postRequest = *" + request + "*");
                    content = (T) mRequestProcessor.post(new URI(request),
                            null, mClazz);
                } else {
                    Log.d(getTag(), "putRequest = *" + request + "*");
                    mRequestProcessor.put(new URI(request), null);
                }
                break;
            } catch (URISyntaxException e) {
                throw new RestServiceException(
                        RestServiceException.ERROR_INTERNAL_ERROR, e);
            } catch (ResourceAccessException e) {
                retryCount++;
                if (retryCount < fMaxRetryCount) {
                    Log.d(getTag(), "request timed out - retrying");
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e1) { // do nothing, just
                        // continue and try
                        // again
                    }
                    continue;
                } else {
                    throw new RestServiceException(
                            RestServiceException.ERROR_NETWORK_FAILURE, e);
                }
            } catch (HttpClientErrorException e) {
                HttpStatus status = e.getStatusCode();
                if (status.value() == statusAuthRequired) {
                    Log.e(getTag(), "authorization failed - apikey not valid");
                    throw new RestServiceException(
                            RestServiceException.ERROR_AUTHORIZATION_FAILED, e);
                } else if (status.value() == statusRequestForbidden) {
                    Log.e(getTag(), "request forbidden");
                    throw new RestServiceException(
                            RestServiceException.ERROR_REQUEST_FORBIDDEN, e);
                } else if ((status.value() == statusBadRequest)
                        || (status.value() == statusNotFound)
                        || (status.value() == statusNotAcceptable)) {
                    Log.e(getTag(), "request error");
                    throw new RestServiceException(
                            RestServiceException.ERROR_CLIENT_FAILURE, e);
                } else {
                    throw new RestServiceException(
                            RestServiceException.ERROR_CLIENT_FAILURE, e);
                }

            } catch (HttpServerErrorException e) {
                HttpStatus status = e.getStatusCode();
                if (status.value() == statusDownMaintenance) {
                    throw new RestServiceException(
                            RestServiceException.ERROR_SERVER_DOWN, e);
                } else {
                    throw new RestServiceException(
                            RestServiceException.ERROR_SERVER_FAILURE, e);
                }
            } catch (HttpMessageConversionException e) {
                throw new RestServiceException(
                        RestServiceException.ERROR_NETWORK_FAILURE, e);
            } catch (RestClientException e) {
                throw new RestServiceException(
                        RestServiceException.ERROR_NETWORK_UNKNOWN_FAILURE, e);
            }
        }
        Log.d(getTag(), "executeRequest successful");

        return content;
    }
}
