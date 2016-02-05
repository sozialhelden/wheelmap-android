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
package org.wheelmap.android.model;

import org.wheelmap.android.model.Wheelmap.POIs;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

public class POIHelper {

    public static String getWMId(Cursor c) {
        return c.getString(c.getColumnIndexOrThrow(POIs.WM_ID));
    }

    public static String getName(Cursor c) {
        String name = c.getString(c.getColumnIndexOrThrow(POIs.NAME));
        if (name == null) {
            return null;
        }

        name = name.replace("&#38;", "&");

        return name;
    }

    public static String getStreet(Cursor c) {
        return c.getString(c.getColumnIndexOrThrow(POIs.STREET));
    }

    public static String getPostcode(Cursor c) {
        return c.getString(c.getColumnIndexOrThrow(POIs.POSTCODE));
    }

    public static String getCity(Cursor c) {
        return c.getString(c.getColumnIndexOrThrow(POIs.CITY));
    }

    public static double getLatitude(Cursor c) {
        return c.getDouble(c.getColumnIndexOrThrow(POIs.LATITUDE));
    }

    public static double getLongitude(Cursor c) {
        return c.getDouble(c.getColumnIndexOrThrow(POIs.LONGITUDE));
    }

    public static String getAddress(Cursor c) {
        StringBuilder address = new StringBuilder();
        // street
        String street = getStreet(c);
        if (!TextUtils.isEmpty(street)) {
            address.append(street);
            address.append(' ');
        }
        // house number
        String nr = getHouseNumber(c);
        if (!TextUtils.isEmpty(nr)) {
            address.append(nr);
        }

        // post code & city
        String postcode = getPostcode(c);
        String city = getCity(c);

        if ((!TextUtils.isEmpty(street) || !TextUtils.isEmpty(nr))
                && (!TextUtils.isEmpty(postcode) || !TextUtils.isEmpty(city))) {
            address.append(", ");
        }

        if (!TextUtils.isEmpty(postcode)) {
            address.append(postcode);
            address.append(' ');
        }

        if (!TextUtils.isEmpty(city)) {
            address.append(city);
        }

        return address.toString();
    }

    public static WheelchairFilterState getWheelchair(Cursor c) {
        return WheelchairFilterState.valueOf(c.getInt(c
                .getColumnIndexOrThrow(POIs.WHEELCHAIR)));
    }

    public static WheelchairFilterState getWheelchairToilet(Cursor c) {
        return WheelchairFilterState.valueOf(c.getInt(c
                .getColumnIndexOrThrow(POIs.WHEELCHAIR_TOILET)));
    }

    // comment into DB
    public static String getComment(Cursor c) {
        return c.getString(c.getColumnIndexOrThrow(POIs.DESCRIPTION));
    }

    public static String getWebsite(Cursor c) {
        return c.getString(c.getColumnIndexOrThrow(POIs.WEBSITE));
    }

    public static String getPhone(Cursor c) {
        return c.getString(c.getColumnIndexOrThrow(POIs.PHONE));
    }

    public static long getId(Cursor c) {
        return c.getLong(c.getColumnIndexOrThrow(POIs._ID));
    }

    public static String getHouseNumber(Cursor c) {
        return c.getString(c.getColumnIndexOrThrow(POIs.HOUSE_NUM));
    }

    public static int getCategoryId(Cursor c) {
        return c.getInt(c.getColumnIndexOrThrow(POIs.CATEGORY_ID));
    }

    public static String getCategoryIdentifier(Cursor c) {
        return c.getString(c.getColumnIndexOrThrow(POIs.CATEGORY_IDENTIFIER));
    }

    public static int getNodeTypeId(Cursor c) {
        return c.getInt(c.getColumnIndexOrThrow(POIs.NODETYPE_ID));
    }

    public static String getNodeTypeIdentifier(Cursor c) {
        return c.getString(c.getColumnIndexOrThrow(POIs.NODETYPE_IDENTIFIER));
    }

    public static int getDirtyTag(Cursor c) {
        return c.getInt(c.getColumnIndexOrThrow(POIs.DIRTY));
    }

    public static void copyItemToValues(Cursor c, ContentValues values) {
        values.put(POIs.WM_ID, getWMId(c));
        values.put(POIs.NAME, getName(c));

        values.put(POIs.CATEGORY_ID, getCategoryId(c));
        values.put(POIs.CATEGORY_IDENTIFIER, getCategoryIdentifier(c));
        values.put(POIs.NODETYPE_ID, getNodeTypeId(c));
        values.put(POIs.NODETYPE_IDENTIFIER, getNodeTypeIdentifier(c));

        values.put(POIs.LATITUDE, getLatitude(c));
        values.put(POIs.LONGITUDE, getLongitude(c));

        values.put(POIs.STREET, getStreet(c));
        values.put(POIs.HOUSE_NUM, getHouseNumber(c));
        values.put(POIs.POSTCODE, getPostcode(c));
        values.put(POIs.CITY, getCity(c));

        values.put(POIs.WEBSITE, getWebsite(c));
        values.put(POIs.PHONE, getPhone(c));

        values.put(POIs.WHEELCHAIR, getWheelchair(c).getId());
        values.put(POIs.WHEELCHAIR_TOILET, getWheelchairToilet(c).getId());
        values.put(POIs.DESCRIPTION, getComment(c));

    }

    public static String getTakenOn(Cursor c) {
        return c.getString(c.getColumnIndexOrThrow(POIs.TAKEN_ON));
    }
    public static String getType(Cursor c){
        return c.getString(c.getColumnIndexOrThrow(POIs.TYPE));
    }
    public static String getWidth(Cursor c){
        return c.getString(c.getColumnIndexOrThrow(POIs.WIDTH));
    }
    public static String getHeight(Cursor c){
        return c.getString(c.getColumnIndexOrThrow(POIs.HEIGHT));
    }
    public static String getUrl(Cursor c){
        return c.getString(c.getColumnIndexOrThrow(POIs.URL));
    }

    public static String getPhotoID(Cursor c){
        return c.getString(c.getColumnIndexOrThrow(POIs.PHOTO_ID));
    }


}
