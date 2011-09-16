package wheelmap.org.request;


/**
 * Constructs the Uri of a <code>/api/nodes?bbox&wheelchair&page&per_page</code> request
 * @author p.lipp@web.de
 */
public class NodesRequestBuilder extends BaseNodesRequestBuilder {
	
	private static final String RESOURCE = "nodes";
	
	
	public NodesRequestBuilder(final String server, final String apiKey, final AcceptType acceptType) {
		super(server,apiKey, acceptType);
	}
		
	@Override
	protected  String resourcePath() {
		return RESOURCE;
	}
	
	@Override
	public int getRequestType() {
		return RequestBuilder.REQUEST_GET;
	}
}
