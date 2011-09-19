package wheelmap.org.request;


/**
 * Constructs the Uri of a REST-request
 * @author p.lipp@web.de
 */
public abstract class RequestBuilder {
	
	public final static int REQUEST_GET = 0x1;
	public final static int REQUEST_POST = 0x2;
	public final static int REQUEST_PUT = 0x3;

	protected final String server;
	protected final String apiKey;
	protected final AcceptType acceptType;

	public RequestBuilder(final String server, final String apiKey, final AcceptType acceptType) {
		this.server = server;
		this.apiKey = apiKey;
		this.acceptType = acceptType;
	}

	public abstract String buildRequestUri() ;
	protected abstract String resourcePath();
	public abstract int getRequestType();
	
	protected String baseUrl() {
		return String.format("http://%s/api/%s.%s?api_key=%s",server,
				resourcePath(),acceptType.asRequestParameter(),
				apiKey);
	}
	
	
}