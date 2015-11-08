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
package org.wheelmap.android.mapping.node;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.wheelmap.android.mapping.BaseDomain;

import java.math.BigDecimal;
import java.math.BigInteger;

@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class Node extends BaseDomain {

    protected BigInteger id;

    protected BigDecimal lat;

    protected BigDecimal lon;

    protected String name;

    protected Category category;

    @JsonProperty(value = "node_type")
    protected NodeType nodeType;

    protected String wheelchair;

    @JsonProperty(value = "wheelchair_toilet")
    protected String wheelchair_toilet;

    @JsonProperty(value = "wheelchair_description")
    protected String wheelchairDescription;

    protected String street;

    protected String housenumber;

    protected String city;

    protected String postcode;

    protected String phone;

    protected String website;

    @JsonProperty()
    protected String icon;

    /**
     * Gets the value of the lat property.
     *
     * @return possible object is {@link BigDecimal }
     */
    public BigDecimal getLat() {
        return lat;
    }

    /**
     * Sets the value of the lat property.
     *
     * @param value allowed object is {@link BigDecimal }
     */
    public void setLat(BigDecimal value) {
        this.lat = value;
    }

    /**
     * Gets the value of the nodeType property.
     *
     * @return possible object is {@link NodeType }
     */
    public NodeType getNodeType() {
        return nodeType;
    }

    /**
     * Sets the value of the nodeType property.
     *
     * @param value allowed object is {@link NodeType }
     */
    public void setNodeType(NodeType value) {
        this.nodeType = value;
    }

    /**
     * Gets the value of the street property.
     *
     * @return possible object is {@link String }
     */
    public String getStreet() {
        return street;
    }

    /**
     * Sets the value of the street property.
     *
     * @param value allowed object is {@link String }
     */
    public void setStreet(String value) {
        this.street = value;
    }

    /**
     * Gets the value of the website property.
     *
     * @return possible object is {@link String }
     */
    public String getWebsite() {
        return website;
    }

    /**
     * Sets the value of the website property.
     *
     * @param value allowed object is {@link String }
     */
    public void setWebsite(String value) {
        this.website = value;
    }

    /**
     * Gets the value of the wheelchair property.
     *
     * @return possible object is {@link String }
     */
    public String getWheelchair() {
        return wheelchair;
    }

    /**
     * Sets the value of the wheelchair property.
     *
     * @param value allowed object is {@link String }
     */
    public void setWheelchair(String value) {
        this.wheelchair = value;
    }

    /**
     * Gets the value of the wheelchair_toilet property.
     *
     * @return possible object is {@link String }
     */
    public String getWheelchairToilet() {
        return wheelchair_toilet;
    }

    /**
     * Sets the value of the wheelchair_toilet property.
     *
     * @param value allowed object is {@link String }
     */
    public void setWheelchairToilet(String value) {
        this.wheelchair_toilet = value;
    }

    /**
     * Gets the value of the housenumber property.
     *
     * @return possible object is {@link String }
     */
    public String getHousenumber() {
        return housenumber;
    }

    /**
     * Sets the value of the housenumber property.
     *
     * @param value allowed object is {@link String }
     */
    public void setHousenumber(String value) {
        this.housenumber = value;
    }

    /**
     * Gets the value of the wheelchairDescription property.
     *
     * @return possible object is {@link String }
     */
    public String getWheelchairDescription() {
        return wheelchairDescription;
    }

    /**
     * Sets the value of the wheelchairDescription property.
     *
     * @param value allowed object is {@link String }
     */
    public void setWheelchairDescription(String value) {
        this.wheelchairDescription = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is {@link BigInteger }
     */
    public BigInteger getId() {

        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link BigInteger }
     */
    public void setId(BigInteger value) {
        this.id = value;
    }

    /**
     * Gets the value of the categoryId property.
     *
     * @return possible object is {@link Category }
     */
    public Category getCategory() {
        return category;
    }


    /**
     * Sets the value of the categoryId property.
     *
     * @param value allowed object is {@link Category }
     */
    public void setCategory(Category value) {
        this.category = value;
    }

    /**
     * Gets the value of the phone property.
     *
     * @return possible object is {@link String }
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the value of the phone property.
     *
     * @param value allowed object is {@link String }
     */
    public void setPhone(String value) {
        this.phone = value;
    }

    /**
     * Gets the value of the lon property.
     *
     * @return possible object is {@link BigDecimal }
     */
    public BigDecimal getLon() {
        return lon;
    }

    /**
     * Sets the value of the lon property.
     *
     * @param value allowed object is {@link BigDecimal }
     */
    public void setLon(BigDecimal value) {
        this.lon = value;
    }

    /**
     * Gets the value of the city property.
     *
     * @return possible object is {@link String }
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the value of the city property.
     *
     * @param value allowed object is {@link String }
     */
    public void setCity(String value) {
        this.city = value;
    }


    public String getPostcode() {
        return postcode;
    }


    public void setPostcode(String value) {
        this.postcode = value;
    }

    public String getIcon() { return postcode; }

    public void setIcon(String value) { this.icon = value; };

    @Override
    public String toString() {
        return "Node [lat=" + lat + ", nodeTypeId= {" + nodeType.toString()
                + "}, street=" + street
                + ", website=" + website
                + ", wheelchair=" + wheelchair
                + ", wheelchair_toilet=" + wheelchair_toilet
                + ", housenumber=" + housenumber
                + ", wheelchairDescription=" + wheelchairDescription
                + ", name=" + name
                + ", id=" + id
                + ", category= {" + category.toString() + "}"
                + ", phone=" + phone
                + ", lon=" + lon
                + ", city=" + city
                + ", postcode=" + postcode + "]";
    }

}
