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

import org.wheelmap.android.model.WheelchairFilterState;

import android.text.TextUtils;

import java.net.URLEncoder;
import java.util.*;
import java.util.Locale;

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

    private WheelchairFilterState accessState;
    private WheelchairFilterState toiletState;

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
            double longitude, WheelchairFilterState accessState, WheelchairFilterState toiletState,
            String description, String street, String housenumber, String city, String postcode,
            String website, String phone) {
        super(server, apiKey, acceptType);
        this.id = id;
        this.name = name;
        this.category = category;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accessState = accessState;
        this.toiletState = toiletState;
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
            requestAsStringBuffer.append(urlencode(name));
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

        if (accessState != null) {
            requestAsStringBuffer.append("&wheelchair=");
            requestAsStringBuffer.append(urlencode(accessState.asRequestParameter()));
        }

        if (toiletState != null) {
            requestAsStringBuffer.append("&wheelchair_toilet=");
            requestAsStringBuffer.append(urlencode(toiletState.asRequestParameter()));
        }

        if (!TextUtils.isEmpty(category)) {
            requestAsStringBuffer.append("&category=");
            requestAsStringBuffer.append(urlencode(category));
        }

        if (!TextUtils.isEmpty(description)) {
            String tmpString = description.length() > 255 ? description
                    .substring(0, 254) : description;
            requestAsStringBuffer.append("&wheelchair_description=");
            requestAsStringBuffer.append(urlencode(tmpString));
        }

        if (!TextUtils.isEmpty(street)) {
            requestAsStringBuffer.append("&street=");
            requestAsStringBuffer.append(urlencode(street));
        }

        if (!TextUtils.isEmpty(housenumber)) {
            requestAsStringBuffer.append("&housenumber=");
            requestAsStringBuffer.append(urlencode(housenumber));
        }

        if (!TextUtils.isEmpty(city)) {
            requestAsStringBuffer.append("&city=");
            requestAsStringBuffer.append(urlencode(city));
        }

        if (!TextUtils.isEmpty(postcode)) {
            requestAsStringBuffer.append("&postcode=");
            requestAsStringBuffer.append(urlencode(postcode));
        }

        if (!TextUtils.isEmpty(website)) {
            requestAsStringBuffer.append("&website=");
            requestAsStringBuffer.append(urlencode(website));
        }

        if (!TextUtils.isEmpty(phone)) {
            requestAsStringBuffer.append("&phone=");
            requestAsStringBuffer.append(urlencode(phone));
        }

        requestAsStringBuffer.append("&locale=");
        requestAsStringBuffer.append(Locale.getDefault().getLanguage());

        return requestAsStringBuffer.toString();
    }

    private static String urlencode(String s){
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (Exception e){
            return s;
        }
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

    @Override
    public boolean urlIsAlreadyUrlEncoded() {
        return true;
    }
}
