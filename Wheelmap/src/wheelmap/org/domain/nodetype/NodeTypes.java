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
