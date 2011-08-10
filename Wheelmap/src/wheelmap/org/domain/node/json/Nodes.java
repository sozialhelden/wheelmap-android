package wheelmap.org.domain.node.json;

import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;


@JsonAutoDetect
public class Nodes {

    protected Conditions conditions;
    protected Meta meta;
    // @JsonUnwrapped allows inlining, starting with jackson 1.9, so it could be possible to combine xml and json mapping.
    // as of now jackson 1.8.5 is current, so there is a need for an upgrade
    protected List<Node> nodes;

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
     * Gets the value of the meta property.
     * 
     * @return
     *     possible object is
     *     {@link Meta }
     *     
     */
    public Meta getMeta() {
        return meta;
    }

    /**
     * Sets the value of the meta property.
     * 
     * @param value
     *     allowed object is
     *     {@link Meta }
     *     
     */
    public void setMeta(Meta value) {
        this.meta = value;
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
