package wheelmap.org.request;

/**
 * Constructs the Uri of a <code>/api/nodes?bbox&wheelchair&page&per_page</code> request
 * @author p.lipp@web.de
 */
public class NodeTypeNodesRequestBuilder extends BaseNodesRequestBuilder {
	
	private static final String RESOURCE = "node_types/%d/nodes";
	
	private int nodeType;

	public NodeTypeNodesRequestBuilder(final String server, final String apiKey, final AcceptType acceptType, int nodeType) {
		super(server,apiKey, acceptType);
		this.nodeType = nodeType;
	}
	
	@Override
	protected  String resourcePath() {
		return String.format( RESOURCE, nodeType);
	}
	
	@Override
	public int getRequestType() {
		return RequestBuilder.REQUEST_GET;
	}
}
