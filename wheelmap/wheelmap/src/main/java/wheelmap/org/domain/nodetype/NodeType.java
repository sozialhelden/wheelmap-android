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
package wheelmap.org.domain.nodetype;

import java.math.BigDecimal;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import wheelmap.org.domain.node.Category;

@JsonAutoDetect
public class NodeType {

	private BigDecimal id;
	private String identifier;
	@JsonProperty( value="icon" )
	private String iconUrl;
	@JsonIgnore
	private byte[] iconData;
	@JsonProperty( value="localized_name")
	private String localizedName;
	@JsonProperty( value="category_id")
	private BigDecimal categoryId;
	
	
	// This Category should be there according to the docs. But in fact its not there
	@JsonIgnore
	private Category category;
	
	public BigDecimal getId() {
		return id;
	}
	
	public void setId( BigDecimal id ) {
		this.id= id;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier( String identifier ) {
		this.identifier = identifier;
	}
	
	public String getIconUrl() {
		return iconUrl;
	}
	
	public void setIconUrl( String url ) {
		this.iconUrl = url;
	}
	
	public byte[] getIconData() {
		return iconData;
	}
	
	public void setIconData( byte[] data ) {
		this.iconData = data;
	}
	
	public String getLocalizedName() {
		return localizedName;
	}
	
	public void setLocalizedName( String localizedName ) {
		this.localizedName = localizedName;
	}
	
	public BigDecimal getCategoryId() {
		return categoryId;
	}
	
	public void setCategoryId( BigDecimal id ) {
		this.categoryId = id;
	}
	
	public Category getCategory() {
		return category;
	}
	
	public void setCategory( Category category ) {
		this.category = category;
	}
}
