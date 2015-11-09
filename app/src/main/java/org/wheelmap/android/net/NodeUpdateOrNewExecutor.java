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

import com.google.inject.Inject;

import org.wheelmap.android.mapping.Message;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.PrepareDatabaseHelper;
import org.wheelmap.android.model.WheelchairFilterState;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.modules.ICredentials;
import org.wheelmap.android.net.request.AcceptType;
import org.wheelmap.android.net.request.NodeUpdateOrNewAllRequestBuilder;
import org.wheelmap.android.net.request.RequestBuilder;
import org.wheelmap.android.net.request.ToiletStateUpdateRequestBuilder;
import org.wheelmap.android.net.request.WheelchairUpdateRequestBuilder;
import org.wheelmap.android.service.RestServiceException;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import de.akquinet.android.androlog.Log;

public class NodeUpdateOrNewExecutor extends AbstractExecutor<Message> {

    private final static String TAG = NodeUpdateOrNewExecutor.class
            .getSimpleName();

    private static final int MAX_RETRY_COUNT = 3;

    private Cursor mCursor;

    @Inject
    private ICredentials mCredentials;

    public NodeUpdateOrNewExecutor(Context context) {
        super(context, null, Message.class, MAX_RETRY_COUNT);
    }

    public void prepareContent() {
        mCursor = PrepareDatabaseHelper.queryDirty(getResolver());

        if (mCursor == null) {
            return;
        }

        mCursor.moveToFirst();
    }

    public void execute(long id) throws RestServiceException {

        if (mCursor == null) {
            throw new RestServiceException(
                    RestServiceException.ERROR_INTERNAL_ERROR,
                    new NullPointerException("Cursor is null"));
        }

        boolean result = true;
        RestServiceException to_throw=null;
        RestServiceException errorException = null;
        cursorLoop: while (!mCursor.isAfterLast()) {

            String editApiKey = getApiKey();
            if (editApiKey.length() == 0) {
                throw new RestServiceException(
                        RestServiceException.ERROR_AUTHORIZATION_ERROR,
                        new RuntimeException("No apikey to edit available"));
            }

            List<RequestBuilder> requestBuilderList = new ArrayList<>();

            int dirtyTag = POIHelper.getDirtyTag(mCursor);
            switch (dirtyTag) {
                case POIs.DIRTY_STATE:
                    requestBuilderList.add(wheelchairUpdateRequestBuilder(editApiKey));
                    requestBuilderList.add(toiletStateUpdateRequestBuilder(editApiKey));
                    break;
                case POIs.DIRTY_ALL:
                    requestBuilderList.add(updateOrNewRequestBuilder(editApiKey));
                    break;
                default:
                    throw new RestServiceException(
                            RestServiceException.ERROR_INTERNAL_ERROR,
                            new RuntimeException(
                                    "Cant find matching RequestBuilder for update request"));
            }

            long idPOI = POIHelper.getId(mCursor);
            for (RequestBuilder requestBuilder : requestBuilderList) {
                try{
                    Message response = executeRequest(requestBuilder);
                    if(response != null){
                        if(!response.getMessage().equals("OK")){
                            PrepareDatabaseHelper.markDirtyAsClean(getResolver(), idPOI);
                            result = false;
                        }
                    }
                }catch(RestServiceException e) {
                    if(e.getMessage().contains("Bad Request")){
                        PrepareDatabaseHelper.markDirtyAsClean(getResolver(), idPOI);
                        errorException = e;
                        result = false;
                    }else{
                        to_throw = e;
                        mCursor.moveToNext();
                        continue cursorLoop;
                    }
                }
            }

            PrepareDatabaseHelper.markDirtyAsClean(getResolver(), idPOI);
            mCursor.moveToNext();
        }

        mCursor.close();

        if(to_throw != null){
           throw to_throw;
        }

        if(errorException != null){
            throw errorException;
        } else if(!result){
            throw new RestServiceException(
                    RestServiceException.ERROR_NETWORK_FAILURE,
                    new RuntimeException(
                            "REQUEST Failed"));
        }
    }

    public void prepareDatabase() {
        PrepareDatabaseHelper.replayChangedCopies(getResolver());
    }

    private RequestBuilder wheelchairUpdateRequestBuilder(String apiKey) {
        String id = POIHelper.getWMId(mCursor);

        WheelchairFilterState state = POIHelper.getWheelchair(mCursor);
        return new WheelchairUpdateRequestBuilder(getServer(), apiKey,
                AcceptType.JSON, id, state);
    }

    private RequestBuilder toiletStateUpdateRequestBuilder(String apiKey){
        String id = POIHelper.getWMId(mCursor);
        WheelchairFilterState state = POIHelper.getWheelchairToilet(mCursor);
        return new ToiletStateUpdateRequestBuilder(getServer(), apiKey,
                AcceptType.JSON, id, state);
    }

    private RequestBuilder updateOrNewRequestBuilder(String apiKey) {
        String id = POIHelper.getWMId(mCursor);
        if (id != null) {
            Log.d(TAG, "updateOrNewRequestBuilder: doing an update of id = "
                    + id);
        } else {
            Log.d(TAG, "updateOrNewRequestBuilder: creating a new poi");
        }

        String name = POIHelper.getName(mCursor);

        String categoryIdentifier = POIHelper.getCategoryIdentifier(mCursor);
        String nodeTypeIdentifier = POIHelper.getNodeTypeIdentifier(mCursor);

        double latitude = POIHelper.getLatitude(mCursor);
        double longitude = POIHelper.getLongitude(mCursor);

        WheelchairFilterState accessState = POIHelper.getWheelchair(mCursor);
        WheelchairFilterState toiletState = POIHelper.getWheelchairToilet(mCursor);
        String comment = POIHelper.getComment(mCursor);

        String street = POIHelper.getStreet(mCursor);
        String housenumber = POIHelper.getHouseNumber(mCursor);
        String city = POIHelper.getCity(mCursor);
        String postcode = POIHelper.getPostcode(mCursor);

        String website = POIHelper.getWebsite(mCursor);
        String phone = POIHelper.getPhone(mCursor);

        return new NodeUpdateOrNewAllRequestBuilder(getServer(), apiKey,
                AcceptType.JSON, id, name, categoryIdentifier,
                nodeTypeIdentifier, latitude, longitude, accessState, toiletState,
                comment, street, housenumber, city, postcode, website, phone);
    }

}
