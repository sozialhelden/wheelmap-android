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

import org.wheelmap.android.mapping.node.Node;
import org.wheelmap.android.mapping.node.Nodes;
import org.wheelmap.android.model.Wheelmap.POIs;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import java.math.BigDecimal;

public class DataOperationsNodes extends DataOperations<Nodes, Node> {

    public DataOperationsNodes(ContentResolver resolver) {
        super(resolver);
    }

    @Override
    protected Node getItem(Nodes items, int i) {
        return items.getNodes().get(i);
    }

    @Override
    public void copyToValues(Node node, ContentValues values) {
        values.clear();
        values.put(POIs.WM_ID, node.getId().longValue());
        values.put(POIs.NAME, node.getName());

        BigDecimal latitude = node.getLat();
        if (latitude != null) {
            values.put(POIs.LATITUDE, latitude.doubleValue());
        }
        BigDecimal longitude = node.getLon();
        if (longitude != null) {
            values.put(POIs.LONGITUDE, longitude.doubleValue());
        }
        values.put(POIs.STREET, node.getStreet());
        values.put(POIs.HOUSE_NUM, node.getHousenumber());
        values.put(POIs.POSTCODE, node.getPostcode());
        values.put(POIs.CITY, node.getCity());
        values.put(POIs.PHONE, node.getPhone());
        values.put(POIs.WEBSITE, node.getWebsite());
        values.put(POIs.WHEELCHAIR,
                WheelchairState.myValueOf(node.getWheelchair()).getId());
        values.put(POIs.DESCRIPTION, node.getWheelchairDescription());
        values.put(POIs.ICON, node.getIcon());

        org.wheelmap.android.mapping.node.Category cat = node.getCategory();
        if (cat != null) {
            values.put(POIs.CATEGORY_ID, cat.getId().intValue());
            values.put(POIs.CATEGORY_IDENTIFIER, cat.getIdentifier());
        }

        org.wheelmap.android.mapping.node.NodeType nodeType = node.getNodeType();
        if (nodeType != null) {
            values.put(POIs.NODETYPE_ID, nodeType.getId().intValue());
            values.put(POIs.NODETYPE_IDENTIFIER, nodeType.getIdentifier());
        }
        values.put(POIs.TAG, POIs.TAG_RETRIEVED);
    }

    @Override
    protected Uri getUri() {
        return POIs.createNoNotify(POIs.CONTENT_URI_RETRIEVED);
    }

}
