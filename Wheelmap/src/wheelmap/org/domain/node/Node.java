package wheelmap.org.domain.node;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;




@Root(name = "node")
public class Node {

    @Element(required = true)
    protected BigDecimal lat;
    @Element(name = "node-type", required = true)
    protected NodeType nodeType;
    @Element(required = false)
    protected String street;
    @Element(required = false)
    protected String website;
    @Element(required = false)
    protected String wheelchair;
    @Element(required = false)
    protected String housenumber;
    @Element(name = "wheelchair-description", required = false)
    protected String wheelchairDescription;
    @Element(required = false)
    protected String name;
    @Element(required = true)
    protected BigInteger id;
    @Element(required=true)
    protected Category category;
    @Element(required = false)
    protected String phone;
    @Element(required = true)
    protected BigDecimal lon;
    @Element(required = false)
    protected String city;
    @Element(required = false)
    protected String postcode;

    /**
     * Gets the value of the lat property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getLat() {
        return lat;
    }

    /**
     * Sets the value of the lat property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setLat(BigDecimal value) {
        this.lat = value;
    }

    /**
     * Gets the value of the nodeType property.
     * 
     * @return
     *     possible object is
     *     {@link NodeType }
     *     
     */
    public NodeType getNodeType() {
        return nodeType;
    }

    /**
     * Sets the value of the nodeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link NodeType }
     *     
     */
    public void setNodeType(NodeType value) {
        this.nodeType = value;
    }

    /**
     * Gets the value of the street property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStreet() {
        return street;
    }

    /**
     * Sets the value of the street property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStreet(String value) {
        this.street = value;
    }

    /**
     * Gets the value of the website property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWebsite() {
        return website;
    }

    /**
     * Sets the value of the website property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWebsite(String value) {
        this.website = value;
    }

    /**
     * Gets the value of the wheelchair property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWheelchair() {
        return wheelchair;
    }

    /**
     * Sets the value of the wheelchair property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWheelchair(String value) {
        this.wheelchair = value;
    }

    /**
     * Gets the value of the housenumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHousenumber() {
        return housenumber;
    }

    /**
     * Sets the value of the housenumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHousenumber(String value) {
        this.housenumber = value;
    }

    /**
     * Gets the value of the wheelchairDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWheelchairDescription() {
        return wheelchairDescription;
    }

    /**
     * Sets the value of the wheelchairDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWheelchairDescription(String value) {
        this.wheelchairDescription = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setId(BigInteger value) {
        this.id = value;
    }

    /**
     * Gets the value of the categoryId property.
     * 
     * @return
     *     possible object is
     *     {@link Category }
     *     
     */
    public Category getCategory() {
    	return category;
    }
    
    
    /**
     * Sets the value of the categoryId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Category }
     *     
     */
    public void setCategory( Category value ) {
    	this.category = value;
    }
    
    /**
     * Gets the value of the phone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the value of the phone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPhone(String value) {
        this.phone = value;
    }

    /**
     * Gets the value of the lon property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getLon() {
        return lon;
    }

    /**
     * Sets the value of the lon property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setLon(BigDecimal value) {
        this.lon = value;
    }

    /**
     * Gets the value of the city property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the value of the city property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
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

	@Override
	public String toString() {
		return "Node [lat=" + lat + ", nodeTypeId= {" + nodeType.toString() + "}, street="
				+ street + ", website=" + website + ", wheelchair="
				+ wheelchair + ", housenumber=" + housenumber
				+ ", wheelchairDescription=" + wheelchairDescription
				+ ", name=" + name + ", id=" + id + ", category= {"
				+ category.toString() + "}, phone=" + phone + ", lon=" + lon + ", city="
				+ city + ", postcode=" + postcode + "]";
	}

}
