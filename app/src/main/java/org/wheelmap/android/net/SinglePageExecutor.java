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

import org.wheelmap.android.mapping.Base;
import org.wheelmap.android.net.request.RequestBuilder;
import org.wheelmap.android.service.RestServiceException;

import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import de.akquinet.android.androlog.Log;

public abstract class SinglePageExecutor<T extends Base> extends
        AbstractExecutor<T> implements IExecutor {

    protected static final int DEFAULT_TEST_PAGE_SIZE = 250;

    private static final int MAX_RETRY_COUNT = 5;

    private List<T> mTempStore = new ArrayList<T>();

    public SinglePageExecutor(Context context, Bundle bundle, Class<T> clazz) {
        super(context, bundle, clazz, MAX_RETRY_COUNT);
    }

    protected void clearTempStore() {
        mTempStore.clear();
    }

    protected List<T> getTempStore() {
        return mTempStore;
    }

    protected void retrieveSinglePage(RequestBuilder requestBuilder)
            throws RestServiceException {
        final long startRemote = System.currentTimeMillis();
        int result = executeSingleRequest(requestBuilder);
        Log.i(getTag(), "remote sync took "
                + (System.currentTimeMillis() - startRemote) + "ms");
    }

    protected int executeSingleRequest(RequestBuilder requestBuilder)
            throws RestServiceException {

        T items = executeRequest(requestBuilder);
        if (items == null) {
            Log.w(getTag(), "retrieved no items - tempstore is empty");
            return 0;
        }

        mTempStore.add(items);

        return 1;
    }
}
