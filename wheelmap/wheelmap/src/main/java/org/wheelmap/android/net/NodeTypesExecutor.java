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

import org.wheelmap.android.mapping.nodetype.NodeTypes;
import org.wheelmap.android.model.DataOperationsNodeTypes;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Support.LastUpdateContent;
import org.wheelmap.android.model.Support.NodeTypesContent;
import org.wheelmap.android.net.request.AcceptType;
import org.wheelmap.android.net.request.Locale;
import org.wheelmap.android.net.request.NodeTypesRequestBuilder;
import org.wheelmap.android.net.request.Paging;
import org.wheelmap.android.service.RestServiceException;

import android.content.Context;
import android.os.Bundle;

import de.akquinet.android.androlog.Log;

public class NodeTypesExecutor extends SinglePageExecutor<NodeTypes> implements
        IExecutor {

    private final static String TAG = NodeTypesExecutor.class.getSimpleName();

    private Locale mLocale;

    private String mEtag;

    private boolean mContentIsEqual;

    public NodeTypesExecutor(Context context, Bundle bundle) {
        super(context, bundle, NodeTypes.class);
    }

    @Override
    public void prepareContent() {
        String locale = getBundle().getString(Extra.LOCALE);
        if (locale != null && !locale.equals("de")) {
            mLocale = new Locale(locale);
        }

        mEtag = LastUpdateContent.queryEtag(getResolver(), LastUpdateContent.MODULE_NODETYPES);
    }

    @Override
    public void execute() throws RestServiceException {
        NodeTypesRequestBuilder requestBuilder = new NodeTypesRequestBuilder(
                getServer(), getApiKey(), AcceptType.JSON);
        requestBuilder.paging(new Paging(DEFAULT_TEST_PAGE_SIZE));
        if (mLocale != null) {
            requestBuilder.locale(mLocale);
        }
        clearTempStore();

        setEtag(mEtag);
        retrieveSinglePage(requestBuilder);
        if (mEtag != null && mEtag.equals(getEtag()) && getTempStore().isEmpty()) {
            mContentIsEqual = true;
        }

        LastUpdateContent.storeEtag(getResolver(), LastUpdateContent.MODULE_NODETYPES, getEtag());
        Log.d(TAG, "etag = " + getEtag());
    }

    @Override
    public void prepareDatabase() throws RestServiceException {
        if (mContentIsEqual) {
            Log.i(TAG, "content is equal according to etag - doing nothing");
            return;
        }

        getResolver().delete(NodeTypesContent.CONTENT_URI, null, null);
        DataOperationsNodeTypes don = new DataOperationsNodeTypes(getResolver());
        don.insert(getTempStore());
        clearTempStore();
    }
}
