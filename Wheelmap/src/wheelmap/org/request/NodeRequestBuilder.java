package wheelmap.org.request;

/**
 * Constructs the Uri of a <code>/api/nodes/{node_id}</code> request
 * @author p.lipp@web.de
 */
public class NodeRequestBuilder extends RequestBuilder {

	private static final String RESOURCE = "nodes";
	private int id;

	public NodeRequestBuilder(final String server, final String apiKey, final AcceptType acceptType, int id) {
		super(server, apiKey, acceptType);
		this.id=id;
	}	

	@Override
	public String buildRequestUri() {
	    final StringBuilder requestAsStringBuffer = new StringBuilder(200);
	    requestAsStringBuffer.append(String.format(baseUrl()));  
	    
	    return requestAsStringBuffer.toString();
	}
	
	@Override
	protected  String resourcePath() {
		return RESOURCE+"/"+id;
	}
}
