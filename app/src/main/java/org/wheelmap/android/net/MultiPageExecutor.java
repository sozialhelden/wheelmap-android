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

import org.wheelmap.android.mapping.BaseDomain;
import org.wheelmap.android.net.request.BaseNodesRequestBuilder;
import org.wheelmap.android.net.request.Paging;
import org.wheelmap.android.net.request.RequestBuilder;
import org.wheelmap.android.service.RestServiceException;

import android.content.Context;
import android.os.Bundle;

import de.akquinet.android.androlog.Log;

public abstract class MultiPageExecutor<T extends BaseDomain> extends
        SinglePageExecutor<T> implements IExecutor {

    public MultiPageExecutor(Context context, Bundle bundle, Class<T> clazz) {
        super(context, bundle, clazz);
    }

    @Override
    protected int executeSingleRequest(RequestBuilder requestBuilder) {
        super.executeSingleRequest(requestBuilder);
        int pages = getTempStore().get(0).getMeta().getNumPages().intValue();

        Log.d(getTag(), "pages available " + pages);
        return pages;
    }

    protected void retrieveMaxNPages(RequestBuilder requestBuilder, int n)
            throws RestServiceException {
        final long startRemote = System.currentTimeMillis();

        // Server seems to count from 1...
        Paging page = new Paging(DEFAULT_TEST_PAGE_SIZE, 1);
        if (requestBuilder instanceof BaseNodesRequestBuilder) {
            ((BaseNodesRequestBuilder) requestBuilder).paging(page);
        }

        int pages = executeSingleRequest(requestBuilder);
        if (pages == 0) {
            return;
        }

        int pagesToRetrieve = n < pages ? n : pages;
        int crrPage;
        for (crrPage = 2; crrPage <= pagesToRetrieve; crrPage++) {
            page.setPage(crrPage);
            executeSingleRequest(requestBuilder);
        }

        Log.i(getTag(), "remote sync took "
                + (System.currentTimeMillis() - startRemote) + "ms");
    }
}
