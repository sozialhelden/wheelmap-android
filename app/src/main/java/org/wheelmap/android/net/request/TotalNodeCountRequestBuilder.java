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
/**
 * Constructs the Uri of a <code>/api/nodes/{node_id}</code> request
 * @author p.lipp@web.de
 */
package org.wheelmap.android.net.request;

import java.util.Locale;

public class TotalNodeCountRequestBuilder extends RequestBuilder {

    private static final String RESOURCE = "nodes";

    public TotalNodeCountRequestBuilder(final String server, final String apiKey,
            final AcceptType acceptType) {
        super(server, apiKey, acceptType);
    }

    @Override
    public String buildRequestUri() {
        final StringBuilder requestAsStringBuffer = new StringBuilder(200);
        requestAsStringBuffer.append(String.format(baseUrl()));

        return requestAsStringBuffer.toString()+"&per_page=1";
    }

    @Override
    protected String resourcePath() {
        return String.format(Locale.US, "%s", RESOURCE);
    }

    @Override
    public int getRequestType() {
        return RequestBuilder.REQUEST_MAX_NODE_COUNT;
    }
}
