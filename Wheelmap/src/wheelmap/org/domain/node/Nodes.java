package wheelmap.org.domain.node;

import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;

import wheelmap.org.domain.BaseDomain;

@JsonAutoDetect
public class Nodes extends BaseDomain {

    protected Conditions conditions;
    protected List<Node> nodes;

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
     *     allowed object is
     *     {@link Conditions }
     *     
     */
    public void setConditions(Conditions value) {
        this.conditions = value;
    }

    /**
     * Gets the value of the nodes property.
     * 
     * @return
     *     possible object is
     *     {@link Nodes }
     *     
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * Sets the value of the nodes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Nodes }
     *     
     */
    public void setNodes(List<Node> value) {
        this.nodes = value;
    }

}
