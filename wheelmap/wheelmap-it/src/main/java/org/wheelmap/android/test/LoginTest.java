/*
 * #%L
 * Wheelmap-it - Integration tests
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
package org.wheelmap.android.test;

import org.junit.Assert;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriUtils;
import org.wheelmap.android.mapping.apikey.AuthInfo;
import org.wheelmap.android.net.request.AcceptType;
import org.wheelmap.android.net.request.ApiKeyRequestBuilder;
import org.wheelmap.android.net.request.RequestProcessor;
import org.wheelmap.android.service.RestServiceException;

import android.test.AndroidTestCase;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

public class LoginTest extends AndroidTestCase {

    private final static String TAG = "logintest";

    private String server = "staging.wheelmap.org";

    private int statusAuthFailed = 400;

    private int statusOSMFailed = 403;

    public void testLoginTest() throws URISyntaxException {

        ApiKeyRequestBuilder rb = new ApiKeyRequestBuilder(server,
                AcceptType.JSON);
        rb.setCredentials("olfila@gmx.ne", "testtest"); // wrong
        int result = testRequest(rb);
        Assert.assertEquals(2, result);

        rb.setCredentials("rutton@web.de", "testtest"); // no osm connection
        result = testRequest(rb);
        Assert.assertEquals(0, result);

        rb.setCredentials("rutton.r@gmail.com", "testtest"); // correct
        result = testRequest(rb);
        Assert.assertEquals(2, result);

    }

    private int testRequest(ApiKeyRequestBuilder rb) {

        String request;
        try {
            request = UriUtils.encodeQuery(rb.buildRequestUri(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RestServiceException(
                    RestServiceException.ERROR_INTERNAL_ERROR, e);
        }

        Log.d(TAG, "Request = " + request);

        RequestProcessor rp = new RequestProcessor();
        AuthInfo authInfo = null;
        try {
            authInfo = rp.post(new URI(request), null, AuthInfo.class);
        } catch (HttpClientErrorException e) {
            HttpStatus status = e.getStatusCode();
            if (status.value() == statusAuthFailed) {
                Log.d(TAG, "wrong email or password");
                return 2;
            } else if (status.value() == statusOSMFailed) {
                Log.d(TAG, "not osm connected");
                return 1;
            }

        } catch (RestClientException e) {
            e.printStackTrace();
            Assert.fail("Login failed");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Assert.fail("Login failed");
        }

        if (authInfo != null) {
            Log.d(TAG, "User: olfila@gmx.net " + " api_key = "
                    + authInfo.getUser().getApiKey());
        } else {
            Assert.fail("Login failed");
        }

        return 0;

    }
}
