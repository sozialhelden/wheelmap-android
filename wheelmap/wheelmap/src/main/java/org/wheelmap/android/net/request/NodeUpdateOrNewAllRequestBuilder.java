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

import org.wheelmap.android.model.WheelchairState;

import android.text.TextUtils;

/**
 * Constructs the Uri of a <code>/api/nodes/{node_id}</code> update/put and <code>/api/nodes</code>
 * create/post request
 */
public class NodeUpdateOrNewAllRequestBuilder extends RequestBuilder {

    private static final String RESOURCE = "nodes";

    private String id;

    private String name;

    private String category;

    private String type;

    private double latitude;

    private double longitude;

    private WheelchairState state;

    private String description;

    private String street;

    private String housenumber;

    private String city;

    private String postcode;

    private String website;

    private String phone;

    private boolean update;

    public NodeUpdateOrNewAllRequestBuilder(final String server,
            final String apiKey, final AcceptType acceptType, String id,
            String name, String category, String type, double latitude,
            double longitude, WheelchairState state, String description,
            String street, String housenumber, String city, String postcode,
            String website, String phone) {
        super(server, apiKey, acceptType);
        this.id = id;
        this.name = name;
        this.category = category;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.state = state;
        this.description = description;
        this.street = street;
        this.housenumber = housenumber;
        this.city = city;
        this.postcode = postcode;
        this.website = website;
        this.phone = phone;
    }

    @Override
    public String buildRequestUri() {
        if (id != null) {
            update = true;
        }

        final StringBuilder requestAsStringBuffer = new StringBuilder(1024);
        requestAsStringBuffer.append(String.format(baseUrl()));
        if (!TextUtils.isEmpty(name)) {
            requestAsStringBuffer.append("&name=");
            requestAsStringBuffer.append(name);
        }

        if (!TextUtils.isEmpty(type)) {
            requestAsStringBuffer.append("&type=");
            requestAsStringBuffer.append(type);
        }

        if (latitude != 0) {
            requestAsStringBuffer.append("&lat=");
            requestAsStringBuffer.append(latitude);
        }

        if (longitude != 0) {
            requestAsStringBuffer.append("&lon=");
            requestAsStringBuffer.append(longitude);
        }

        if (state != null) {
            requestAsStringBuffer.append("&wheelchair=");
            requestAsStringBuffer.append(state.asRequestParameter());
        }

        if (!TextUtils.isEmpty(category)) {
            requestAsStringBuffer.append("&category=");
            requestAsStringBuffer.append(category);
        }

        if (!TextUtils.isEmpty(description)) {
            String tmpString = description.length() > 255 ? description
                    .substring(0, 254) : description;
            requestAsStringBuffer.append("&wheelchair_description=");
            requestAsStringBuffer.append(tmpString);
        }

        if (!TextUtils.isEmpty(street)) {
            requestAsStringBuffer.append("&street=");
            requestAsStringBuffer.append(street);
        }

        if (!TextUtils.isEmpty(housenumber)) {
            requestAsStringBuffer.append("&housenumber=");
            requestAsStringBuffer.append(housenumber);
        }

        if (!TextUtils.isEmpty(city)) {
            requestAsStringBuffer.append("&city=");
            requestAsStringBuffer.append(city);
        }

        if (!TextUtils.isEmpty(postcode)) {
            requestAsStringBuffer.append("&postcode=");
            requestAsStringBuffer.append(postcode);
        }

        if (!TextUtils.isEmpty(website)) {
            requestAsStringBuffer.append("&website=");
            requestAsStringBuffer.append(website);
        }

        if (!TextUtils.isEmpty(phone)) {
            requestAsStringBuffer.append("&phone=");
            requestAsStringBuffer.append(phone);
        }

        return requestAsStringBuffer.toString();
    }

    @Override
    protected String resourcePath() {
        if (update) {
            return RESOURCE + "/" + id;
        } else {
            return RESOURCE;
        }
    }

    @Override
    public int getRequestType() {
        if (update) {
            return RequestBuilder.REQUEST_PUT;
        } else {
            return RequestBuilder.REQUEST_POST;
        }
    }
}
