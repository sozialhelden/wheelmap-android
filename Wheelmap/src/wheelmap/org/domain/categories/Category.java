package wheelmap.org.domain.categories;

import java.math.BigInteger;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect
public class Category {

    protected BigInteger id;
    @JsonProperty( value="localized_name" )
    protected String localizedName;
    protected String identifier;
	
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
     * Gets the value of the localized_name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalizedName() {
    	return localizedName;
    }
    
    /**
     * Sets the value of the localized_name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    
    public void setLocalizedName( String localized_name) {
    	this.localizedName = localized_name;
    }
    
    /**
     * Gets the value of the identifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentifier(String value) {
        this.identifier = value;
    }
    
    public String toString() {
    	return "id=" + id + ", identifier=" + identifier;
    }
}
