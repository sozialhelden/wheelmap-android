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
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.mapping.Base;
import org.wheelmap.android.mapping.node.Nodes;
import org.wheelmap.android.mapping.node.Photo;
import org.wheelmap.android.mapping.node.Photos;
import org.wheelmap.android.mapping.node.SingleNode;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Extra.What;
import org.wheelmap.android.modules.IAppProperties;
import org.wheelmap.android.modules.ICredentials;
import org.wheelmap.android.modules.IHttpUserAgent;
import org.wheelmap.android.net.request.RequestBuilder;
import org.wheelmap.android.net.request.RequestProcessor;
import org.wheelmap.android.online.BuildConfig;
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

    protected final static RequestProcessor mRequestProcessor = new RequestProcessor();

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


    public AbstractExecutor(Context context, Bundle bundle, Class<T> clazz,
            int maxRetryCount) {
        mContext = context;
        mBundle = bundle;
        mClazz = clazz;
        fMaxRetryCount = maxRetryCount;
        RoboGuice.injectMembers(context, this);
    }

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
            case What.RETRIEVE_MARKER_ICONS:
                executor = new MarkerIconExecutor(context,bundle);
                break;
            case What.RETRIEVE_NODES:
            case What.SEARCH_NODES:
            case What.SEARCH_NODES_IN_BOX:
                executor = new NodesExecutor(context, bundle);
                break;
            case What.RETRIEVE_TOTAL_NODE_COUNT:
                executor = new TotalNodeCountExecutor(context,bundle);
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
            case What.RETRIEVE_PHOTO:
                executor = new PhotosExecutor(context, bundle);
                break;
            case What.UPDATE_PHOTO:
                executor = new PhotoExecutor(context, bundle);
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

    public abstract void execute(long id) throws RestServiceException;

    public abstract void prepareDatabase() throws RestServiceException;

    @Override
    public String getServer() {
        return BuildConfig.API_BASE_URL;
    }

    protected String getTag() {
        return getClass().getSimpleName();
    }

    protected String getApiKey() {
        return mCredentials.getApiKey();
    }

    protected T executeRequest(RequestBuilder requestBuilder)
            throws RestServiceException {
        T content = null;

        String request = null;
        try {
            if (requestBuilder.urlIsAlreadyUrlEncoded()) {
                request = requestBuilder.buildRequestUri();
            } else {
                request = UriUtils.encodeQuery(requestBuilder.buildRequestUri(),
                        "utf-8");
            }
        } catch (UnsupportedEncodingException e) {
            processException(
                    RestServiceException.ERROR_INTERNAL_ERROR, e, true);
        }

        Log.d("Executer",request);

        if (request == null) {
            // workaround for compiling not recognizing that request will be initialized
            return null;
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
                } else if(requestBuilder.getRequestType() == RequestBuilder.REQUEST_PUT_PHOTO){
                    Log.d(getTag(), "uploadPhoto = *" + request + "+");

                    content = (T) mRequestProcessor.post(new URI(request),null,mClazz);
                } else if(requestBuilder.getRequestType() == RequestBuilder.REQUEST_MAX_NODE_COUNT){
                    Log.d(getTag(), "getTotalNodeCount = *" + request + "+");

                    content = (T) mRequestProcessor.get(new URI(request),mClazz);
                }
                else {
                    Log.d(getTag(), "putRequest = *" + request + "*");
                    mRequestProcessor.put(new URI(request), null);
                }
                break;
            } catch (URISyntaxException e) {
                processException(
                        RestServiceException.ERROR_INTERNAL_ERROR, e, true);
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
                    processException(
                            RestServiceException.ERROR_NETWORK_FAILURE, e, true);
                }
            } catch (HttpClientErrorException e) {
                HttpStatus status = e.getStatusCode();

                checkApiCallClientErrors(e);

                if (status.value() == statusAuthRequired ||
                        (this instanceof ApiKeyExecutor &&
                                status.value() == statusBadRequest)) {
                    Log.e(getTag(), "authorization failed - apikey not valid");
                    processException(
                            RestServiceException.ERROR_AUTHORIZATION_FAILED, e, true);
                } else if (status.value() == statusRequestForbidden) {
                    Log.e(getTag(), "request forbidden");
                    processException(
                            RestServiceException.ERROR_REQUEST_FORBIDDEN, e, true);
                } else if ((status.value() == statusBadRequest)
                        || (status.value() == statusNotFound)
                        || (status.value() == statusNotAcceptable)) {
                    Log.e(getTag(), "request error");
                    processException(
                            RestServiceException.ERROR_CLIENT_FAILURE, e, true);
                } else {
                    processException(
                            RestServiceException.ERROR_CLIENT_FAILURE, e, true);
                }

            } catch (HttpServerErrorException e) {
                HttpStatus status = e.getStatusCode();
                if (status.value() == statusDownMaintenance) {
                    processException(
                            RestServiceException.ERROR_SERVER_DOWN, e, true);
                } else {
                    processException(
                            RestServiceException.ERROR_SERVER_FAILURE, e, true);
                }
            } catch (HttpMessageConversionException e) {
                processException(
                        RestServiceException.ERROR_NETWORK_FAILURE, e, false);
            } catch (RestClientException e) {
                processException(
                        RestServiceException.ERROR_NETWORK_UNKNOWN_FAILURE, e, true);
            }
        }
        Log.d(getTag(), "executeRequest successful");

        if(content != null){

            WheelmapApp app = (WheelmapApp) this.getContext().getApplicationContext();

            if(content.getClass().toString().equals("class org.wheelmap.android.mapping.node.Photos")){
                Log.d("Photos");
                try {

                    for(Photo p : ((Photos)content).getPhotos()){
                        p.getImages().remove(9);
                        p.getImages().remove(8);
                        p.getImages().remove(7);
                        p.getImages().remove(6);
                        p.getImages().remove(5);

                        p.getImages().remove(0);
                        p.getImages().remove(0);
                        p.getImages().remove(0);
                        p.getImages().remove(0);
                    }

                    app.setPhotos((Photos)content);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if(content.getClass().toString().equals("class org.wheelmap.android.mapping.node.SingleNode")){
                Log.d("Node");
                try{
                    app.setNode(((SingleNode)content).getNode());
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            } else if(content.getClass().toString().equals("class org.wheelmap.android.mapping.node.Nodes")){

                 int size = ((Nodes)content).getNodes().size();


                if(size == 0){

                    if(app.isSearching() == true)
                        app.setSearchSuccessfully(false);
                    else
                        app.setSearchSuccessfully(true);

                }
            }
        }



        return content;
    }

    protected void checkApiCallClientErrors(HttpClientErrorException e)
            throws RestServiceException {

    }

    protected void processException(int errorCode, Throwable t, boolean sendToBugsense)
            throws RestServiceException {
        throw new RestServiceException(errorCode, t);
    }
}
