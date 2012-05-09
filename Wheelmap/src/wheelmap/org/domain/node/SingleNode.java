package wheelmap.org.domain.node;

import org.codehaus.jackson.annotate.JsonAutoDetect;

@JsonAutoDetect
public class SingleNode {

	protected Node node;
	
	/**
     * Gets the value of the node property.
     * 
     * @return
     *     possible object is
     *     {@link Node }
     *     
     */
    public Node getNode() {
        return node;
    }

    /**
     * Sets the value of the node property.
     * 
     * @param value
     *     allowed object is
     *     {@link Node }
     *     
     */
    public void setNode(Node value) {
        this.node = value;
    }
	
}
