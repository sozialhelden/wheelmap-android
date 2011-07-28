package wheelmap.org.request;


/**
 * Constructs the Uri of a REST-request
 * @author p.lipp@web.de
 */
public abstract class RequestBuilder {

	protected final String server;
	protected final String apiKey;
	private final AcceptType acceptType;

	public RequestBuilder(final String server, final String apiKey, final AcceptType acceptType) {
		this.server = server;
		this.apiKey = apiKey;
		this.acceptType = acceptType;
	}

	public abstract String buildRequestUri() ;
	protected abstract String resourcePath();	
	
	protected String baseUrl() {
		return String.format("http://%s/api/%s.%s?api_key=%s",server,
				resourcePath(),acceptType.asRequestParameter(),
				apiKey);
	}
	
	
}