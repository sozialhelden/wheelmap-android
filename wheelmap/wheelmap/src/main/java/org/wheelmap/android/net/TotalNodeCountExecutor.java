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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.web.util.UriUtils;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.mapping.node.SingleNode;
import org.wheelmap.android.net.request.AcceptType;
import org.wheelmap.android.net.request.TotalNodeCountRequestBuilder;
import org.wheelmap.android.service.RestServiceException;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;

public class TotalNodeCountExecutor extends SinglePageExecutor<SingleNode> implements
        IExecutor {

    public TotalNodeCountExecutor(Context context, Bundle bundle) {
        super(context, bundle, SingleNode.class);
    }

    @Override
    public void prepareContent() {
    }

    @Override
    public void execute(long id) throws RestServiceException {
        TotalNodeCountRequestBuilder requestBuilder = null;

        requestBuilder = new TotalNodeCountRequestBuilder(getServer(), getApiKey(),
                AcceptType.JSON);

        try{
            String request= UriUtils.encodeQuery(requestBuilder.buildRequestUri(),
                    "utf-8");

            long countTotal = requestNodeCountForURL(request);
            long countUnknown=requestNodeCountForURL(request+"&wheelchair=unknown");

            if(countTotal != -1 && countUnknown != -1 && countTotal > countUnknown){
                long count = countTotal - countUnknown;
                WheelmapApp.getDefaultPrefs().edit().putLong("ItemCountTotal",count).commit();
            }

        }catch(Exception e){
            e.printStackTrace();
            processException(
                    RestServiceException.ERROR_NETWORK_FAILURE,
                    new NetworkErrorException(), true);
        }
    }

    private long requestNodeCountForURL(String request) throws Exception{
        org.apache.http.client.HttpClient client =  mRequestProcessor.getRequestFactory().getHttpClient();
        HttpGet get = new HttpGet(request);
        HttpResponse response = client.execute(get);
        if(response.getStatusLine().getStatusCode() == 200){
            String json = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = new JSONObject(json);
            long count = jsonObject.getJSONObject("meta").getLong("item_count_total");
            return count;
        }
        return -1;
    }

    @Override
    public void prepareDatabase() throws RestServiceException {
    }
}
