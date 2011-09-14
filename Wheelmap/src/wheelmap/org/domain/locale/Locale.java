package wheelmap.org.domain.locale;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect
public class Locale {


	private String id;
	@JsonProperty( value="localized_name" )
	private String localizedName;
	
	public String getId() {
		return id;
	}
	
	public void setId( String id ) {
		this.id = id;
	}
	
	public String getLocalizedName() {
		return localizedName;
	}
	
	public void setLocalizedName( String localizedName ) {
		this.localizedName = localizedName;
	}
	
}
