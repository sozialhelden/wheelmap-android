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
