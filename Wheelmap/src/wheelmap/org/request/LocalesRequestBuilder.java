package wheelmap.org.request;

public class LocalesRequestBuilder extends RequestBuilder {

	private static final String RESOURCE = "locales";
	
	public LocalesRequestBuilder(final String server, final String apiKey, final AcceptType acceptType) {
		super(server,apiKey, acceptType);
	}
	
	@Override
	public String buildRequestUri() {
		return baseUrl();
	}

	@Override
	protected String resourcePath() {
		return RESOURCE;
	}

	@Override
	public int getRequestType() {
		return REQUEST_GET;
	}

}
