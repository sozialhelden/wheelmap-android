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
package wheelmap.org.domain.locale;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

import wheelmap.org.domain.BaseDomain;
import wheelmap.org.domain.categories.Conditions;

@JsonAutoDetect
public class Locales extends BaseDomain {

    protected Conditions conditions;
    @JsonProperty( value="locales" )
	protected Map<String, String> locales;

    /**
     * Gets the value of the conditions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Conditions }
     *     
     */
    public Conditions getConditions() {
        return conditions;
    }

    /**
     * Sets the value of the conditions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Conditions }
     *     
     */
    public void setConditions(Conditions value) {
        this.conditions = value;
    }
    
	/**
	 * Gets the values of the locales list property.
	 * 
	 * @return possible object is list of {@link Locale }
	 * 
	 */
	public Map<String, String> getLocales() {
		return locales;
	}

	/**
	 * Sets the values of the locales list property.
	 * 
	 * @param value
	 *            allowed object is list of {@link Locale }
	 * 
	 */
	public void setLocales(Map<String, String> value) {
		this.locales = value;
	}

}
