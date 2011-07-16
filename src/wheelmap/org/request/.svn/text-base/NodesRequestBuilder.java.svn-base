package wheelmap.org.request;

import wheelmap.org.BoundingBox;
import wheelmap.org.WheelchairState;

/**
 * Constructs the Uri of a <code>/api/nodes?bbox&wheelchair&page&per_page</code> request
 * @author p.lipp@web.de
 */
public class NodesRequestBuilder extends RequestBuilder {
	
	private static final String RESOURCE = "nodes";
	
	private Paging paging = Paging.DEFAULT_PAGING;
	private WheelchairState wheelchairState;
	private BoundingBox boundingBox;

	public NodesRequestBuilder(final String server, final String apiKey, final AcceptType acceptType) {
		super(server,apiKey, acceptType);
	}
	
	public NodesRequestBuilder paging (final Paging paging) {
		this.paging = paging;
		return this;		
	}
	
	public NodesRequestBuilder boundingBox (final BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
		return this;
	}
	
	public NodesRequestBuilder wheelchairState (final WheelchairState wheelchairState) {
		this.wheelchairState = wheelchairState;
		return this;		
	}
	
	public NodesRequestBuilder reset () {
		paging = Paging.DEFAULT_PAGING;
		wheelchairState=null;
		boundingBox=null;
		
		return this;
	}
	
	@Override	
	public String buildRequestUri() {
	    final StringBuilder requestAsStringBuffer = new StringBuilder(200);
	    requestAsStringBuffer.append(String.format(baseUrl() + "&page=%d&per_page=%d",
	      paging.pageNumber, paging.numberOfItemsPerPage));
	    
	    if (boundingBox!=null) {
	    	requestAsStringBuffer.append("&bbox=");
	    	requestAsStringBuffer.append(boundingBox.asRequestParameter());
	    }
	    
	    if (wheelchairState!=null) {
	    	requestAsStringBuffer.append("&wheelchair=");
	    	requestAsStringBuffer.append(wheelchairState.asRequestParameter());
	    }	    
	    
	    return requestAsStringBuffer.toString();
	  }
	
	@Override
	protected  String resourcePath() {
		return RESOURCE;
	}
}
