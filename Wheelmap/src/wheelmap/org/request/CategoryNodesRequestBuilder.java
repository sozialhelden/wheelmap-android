package wheelmap.org.request;


/**
 * Constructs the Uri of a <code>/api/nodes?bbox&wheelchair&page&per_page</code> request
 * @author p.lipp@web.de
 */
public class CategoryNodesRequestBuilder extends BaseNodesRequestBuilder {
	
	private static final String RESOURCE = "categories/%d/nodes";
	
	private int category;

	public CategoryNodesRequestBuilder(final String server, final String apiKey, final AcceptType acceptType, int category) {
		super(server,apiKey, acceptType);
		this.category = category;
	}
	
	@Override
	protected  String resourcePath() {
		return String.format( RESOURCE, category);
	}
	
	@Override
	public int getRequestType() {
		return RequestBuilder.REQUEST_GET;
	}
}
