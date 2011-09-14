package wheelmap.org.domain.locale;

import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;

import wheelmap.org.domain.BaseDomain;
import wheelmap.org.domain.categories.Conditions;

@JsonAutoDetect
public class Locales extends BaseDomain {

    protected Conditions conditions;
	protected List<Locale> locales;

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
	public List<Locale> getLocales() {
		return locales;
	}

	/**
	 * Sets the values of the locales list property.
	 * 
	 * @param value
	 *            allowed object is list of {@link Locale }
	 * 
	 */
	public void setLocales(List<Locale> value) {
		this.locales = value;
	}

}
