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

import java.util.Locale;

/**
 * Constructs the Uri of a <code>/api/nodes?bbox&wheelchair&page&per_page</code> request
 *
 * @author p.lipp@web.de
 */
public abstract class BasePhotosRequestBuilder extends RequestBuilder {

    private Paging paging = Paging.DEFAULT_PAGING;

    private BoundingBox boundingBox;

    public BasePhotosRequestBuilder(final String server, final String apiKey,
            final AcceptType acceptType) {
        super(server, apiKey, acceptType);
    }

    public BasePhotosRequestBuilder paging(final Paging paging) {
        this.paging = paging;
        return this;
    }

    public BasePhotosRequestBuilder boundingBox(final BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        return this;
    }

    @Override
    public String buildRequestUri() {
        final StringBuilder requestAsStringBuffer = new StringBuilder(200);
        requestAsStringBuffer.append(String.format(Locale.US, baseUrl()
                + "&page=%d&per_page=%d", paging.pageNumber,
                paging.numberOfItemsPerPage));

        if (boundingBox != null) {
            requestAsStringBuffer.append("&bbox=");
            requestAsStringBuffer.append(boundingBox.asRequestParameter());
        }

        return requestAsStringBuffer.toString();
    }

    public BasePhotosRequestBuilder reset() {
        paging = Paging.DEFAULT_PAGING;
        boundingBox = null;

        return this;
    }

    @Override
    public int getRequestType() {
        return RequestBuilder.REQUEST_GET;
    }
}
