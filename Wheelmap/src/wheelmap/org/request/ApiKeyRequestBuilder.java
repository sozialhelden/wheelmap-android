package wheelmap.org.request;

public class ApiKeyRequestBuilder extends RequestBuilder {

	private static final String RESOURCE = "apikey";
	private String userName;
	private String password;

	public ApiKeyRequestBuilder(final String server, final AcceptType acceptType) {
		super(server, null, acceptType);
	}
	
	public void setCredentials( String userName, String password ) {
		this.userName = userName;
		this.password = password;
	}

	@Override
	public String buildRequestUri() {
		final StringBuilder requestAsStringBuffer = new StringBuilder(200);
		requestAsStringBuffer.append(String.format(baseUrl()));
		requestAsStringBuffer.append("username=");
		requestAsStringBuffer.append(userName);
		requestAsStringBuffer.append("&password=");
		requestAsStringBuffer.append(password);

		return requestAsStringBuffer.toString();
	}
	
	@Override
	protected String baseUrl() {
		return String.format("http://%s/api/%s.%s?",server,
				resourcePath(),acceptType.asRequestParameter());
	}

	@Override
	protected String resourcePath() {
		return RESOURCE;
	}
	
	@Override
	public int getRequestType() {
		return RequestBuilder.REQUEST_GET;
	}
}
