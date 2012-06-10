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

import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

import wheelmap.org.domain.BaseDomain;
import wheelmap.org.domain.categories.Category;
import wheelmap.org.domain.categories.Conditions;

@JsonAutoDetect
public class NodeTypes extends BaseDomain {
	protected Conditions conditions;
	@JsonProperty( value="node_types" )
	protected List<NodeType> node_types;

	/**
	 * Gets the value of the conditions property.
	 * 
	 * @return possible object is {@link Conditions }
	 * 
	 */
	public Conditions getConditions() {
		return conditions;
	}

	/**
	 * Sets the value of the conditions property.
	 * 
	 * @param value
	 *            allowed object is {@link Conditions }
	 * 
	 */
	public void setConditions(Conditions value) {
		this.conditions = value;
	}

	/**
	 * Gets the values of the category property.
	 * 
	 * @return possible object is list of {@link Category }
	 * 
	 */
	public List<NodeType> getNodeTypes() {
		return node_types;
	}

	/**
	 * Sets the values of the categories property.
	 * 
	 * @param value
	 *            allowed object is list of {@link Category }
	 * 
	 */
	public void setNodeTypes(List<NodeType> value) {
		this.node_types = value;
	}

}
