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

import org.wheelmap.android.mapping.node.Nodes;
import org.wheelmap.android.model.DataOperationsNodes;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.PrepareDatabaseHelper;
import org.wheelmap.android.model.WheelchairState;
import org.wheelmap.android.net.request.AcceptType;
import org.wheelmap.android.net.request.BaseNodesRequestBuilder;
import org.wheelmap.android.net.request.BoundingBox;
import org.wheelmap.android.net.request.BoundingBox.Wgs84GeoCoordinates;
import org.wheelmap.android.net.request.CategoryNodesRequestBuilder;
import org.wheelmap.android.net.request.NodeTypeNodesRequestBuilder;
import org.wheelmap.android.net.request.NodesRequestBuilder;
import org.wheelmap.android.net.request.Paging;
import org.wheelmap.android.net.request.SearchNodesRequestBuilder;
import org.wheelmap.android.service.RestServiceException;
import org.wheelmap.android.utils.GeoMath;
import org.wheelmap.android.utils.ParceableBoundingBox;

import android.app.SearchManager;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import de.akquinet.android.androlog.Log;

public class NodesExecutor extends MultiPageExecutor<Nodes> implements
        IExecutor {

    private static final int MAX_PAGES_TO_RETRIEVE = 2;


    private BoundingBox mBoundingBox = null;

    private int mCategory = Extra.UNKNOWN;

    private int mNodeType = Extra.UNKNOWN;

    private String mSearchTerm = null;

    private WheelchairState mWheelchairState = null;

    public NodesExecutor(Context context, Bundle bundle) {
        super(context, bundle, Nodes.class);
    }

    @Override
    public void prepareContent() {
        if (getBundle().containsKey(Extra.BOUNDING_BOX)) {
            ParceableBoundingBox parcBoundingBox = (ParceableBoundingBox) getBundle()
                    .getSerializable(Extra.BOUNDING_BOX);
            mBoundingBox = parcBoundingBox.toBoundingBox();
            // Log.d(TAG,
            // "retrieving with bounding box: "
            // + parcBoundingBox.toString());
        } else if (getBundle().containsKey(Extra.LOCATION)) {
            float distance = getBundle().getFloat(Extra.DISTANCE_LIMIT);
            Location location = (Location) getBundle().getParcelable(
                    Extra.LOCATION);
            if(location == null){
                location = new Location("gps");
            }
            mBoundingBox = GeoMath.calculateBoundingBox(
                    new Wgs84GeoCoordinates(location.getLongitude(), location
                            .getLatitude()), distance);
            // Log.d(TAG,
            // "retrieving with current location = ("
            // + location.getLongitude() + ","
            // + location.getLatitude() + ") and distance = "
            // + distance);
        }

        if (getBundle().containsKey(Extra.CATEGORY)) {
            mCategory = getBundle().getInt(Extra.CATEGORY);
        } else if (getBundle().containsKey(Extra.NODETYPE)) {
            mNodeType = getBundle().getInt(Extra.NODETYPE);
        }

        if (getBundle().containsKey(SearchManager.QUERY)) {
            mSearchTerm = getBundle().getString(SearchManager.QUERY);
        }

        if (getBundle().containsKey(Extra.WHEELCHAIR_STATE)) {
            mWheelchairState = WheelchairState.valueOf(getBundle().getInt(
                    Extra.WHEELCHAIR_STATE));
        }
    }

    @Override
    public void execute() throws RestServiceException {
        BaseNodesRequestBuilder requestBuilder;
        if (mCategory != Extra.UNKNOWN) {
            requestBuilder = new CategoryNodesRequestBuilder(getServer(),
                    getApiKey(), AcceptType.JSON, mCategory, mSearchTerm);
        } else if (mNodeType != Extra.UNKNOWN) {
            requestBuilder = new NodeTypeNodesRequestBuilder(getServer(),
                    getApiKey(), AcceptType.JSON, mNodeType, mSearchTerm);
        } else if (mSearchTerm != null) {
            requestBuilder = new SearchNodesRequestBuilder(getServer(), getApiKey(),
                    AcceptType.JSON, mSearchTerm);
        } else {
            requestBuilder = new NodesRequestBuilder(getServer(), getApiKey(),
                    AcceptType.JSON);
        }

        requestBuilder.paging(new Paging(DEFAULT_TEST_PAGE_SIZE)).boundingBox(
                mBoundingBox);
        requestBuilder.wheelchairState(mWheelchairState);
        clearTempStore();
        retrieveMaxNPages(requestBuilder, MAX_PAGES_TO_RETRIEVE);
    }

    @Override
    public void prepareDatabase() {
        Log.d(getTag(), "prepareDatabase");
        PrepareDatabaseHelper.cleanupOldCopies(getResolver(), false);
        PrepareDatabaseHelper.deleteRetrievedData(getResolver());
        DataOperationsNodes don = new DataOperationsNodes(getResolver());
        don.insert(getTempStore());
        PrepareDatabaseHelper.replayChangedCopies(getResolver());
        clearTempStore();
    }


}
