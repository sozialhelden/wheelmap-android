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
package org.wheelmap.android.net.request;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.params.CoreProtocolPNames;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestClientException;

import java.net.URI;

import de.akquinet.android.androlog.Log;

/**
 * Sends the {@link HttpUriRequest}s to the REST-Server (Testsystem, e.g. is
 * http://staging.wheelmap.org/api/)
 *
 * @author p.lipp@web.de
 * @see <a href="http://static.springsource.org/spring-android/docs/1.0.x/reference/html/rest-template.html">Spring
 *      android documentation</a>
 */
public class RequestProcessor {

    private final static String TAG = RequestProcessor.class.getSimpleName();

    private static final int CONNECT_TIMEOUT = 5 * 1000;

    private static final int READ_TIMEOUT = 5 * 1000;

    private final RestTemplateExt restTemplate;

    private HttpComponentsClientHttpRequestFactory mRequestFactory;

    public RequestProcessor() {
        restTemplate = new RestTemplateExt();
        mRequestFactory = new HttpComponentsClientHttpRequestFactory();
        mRequestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        mRequestFactory.setReadTimeout(READ_TIMEOUT);

        restTemplate.setRequestFactory(mRequestFactory);
        restTemplate.getMessageConverters().add(
                new MappingJacksonHttpMessageConverter());
    }

    public HttpComponentsClientHttpRequestFactory getRequestFactory(){
        return mRequestFactory;
    }

    public void setUserAgent(String userAgent) {
        mRequestFactory.getHttpClient().getParams()
                .setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
    }

    public void setEtag(String etag) {
        restTemplate.getRequestHttpHeaders().setIfNoneMatch(etag);
    }

    public String getEtag() {
        return restTemplate.getResponseHttpHeaders().getETag();
    }

    public <T> T get(final URI uri, Class<T> clazz) throws RestClientException {
        Log.d(TAG, uri.getQuery());
        return restTemplate.getForObject(uri, clazz);
    }

    public <T> T post(final URI uri, final T postObject, Class<T> clazz)
            throws RestClientException {
        Log.d(TAG, uri.getQuery());
        return restTemplate.postForObject(uri, postObject, clazz);
    }

    public <T> void put(final URI uri, final T putObject)
            throws RestClientException {
        Log.d(TAG, uri.getQuery());
        restTemplate.put(uri, putObject);
    }
}
