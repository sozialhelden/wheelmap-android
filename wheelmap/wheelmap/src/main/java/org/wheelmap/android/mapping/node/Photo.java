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

import java.math.BigDecimal;
import java.math.BigInteger;

@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class Photo {

    protected BigInteger id;

    protected BigInteger takenOn;

    protected String name;

    protected String type;

    protected int width;

    protected int height;

    @JsonProperty(value = "url")
    protected String url;

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
     * @return possible object is {@link java.math.BigInteger }
     */
    public BigInteger getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link java.math.BigInteger }
     */
    public void setId(BigInteger value) {
        this.id = value;
    }

    public BigInteger getTakenOn() {
        return takenOn;
    }

    public void setTakenOn(BigInteger takenOn) {
        this.takenOn = takenOn;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Photo [id=" + id + ", takenOn=" + takenOn + ", name="
                + name + ", url=" + url + ", with="
                + width + ", height=" + height
                + ", type=" + type + "]";
    }

}
