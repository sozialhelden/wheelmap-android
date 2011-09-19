package wheelmap.org.domain.apikey;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect
public class ApiKey {
	@JsonProperty( value = "api_key" )
	protected String apiKey;
	
	public String getApiKey() {
		return apiKey;
	}
	
	public void setApiKey( String apiKey ) {
		this.apiKey = apiKey;
	}
	
}
