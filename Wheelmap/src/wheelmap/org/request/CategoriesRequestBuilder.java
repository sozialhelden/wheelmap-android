package wheelmap.org.request;

import wheelmap.org.Locale;

public class CategoriesRequestBuilder extends RequestBuilder {

	private static final String RESOURCE = "categories";
	private Paging paging = Paging.DEFAULT_PAGING;
	private Locale locale = null;
	
	public CategoriesRequestBuilder(final String server, final String apiKey, final AcceptType acceptType) {
		super(server,apiKey, acceptType);
	}
	
	public CategoriesRequestBuilder paging (final Paging paging) {
		this.paging = paging;
		return this;		
	}
	
	public CategoriesRequestBuilder locale(final Locale locale ) {
		this.locale = locale;
		return this;
	}
	
	public CategoriesRequestBuilder reset () {
		paging = Paging.DEFAULT_PAGING;
		locale = Locale.DEFAULT_LOCALE;
		
		return this;
	}
	
	@Override
	public String buildRequestUri() {
		final StringBuilder requestAsStringBuffer = new StringBuilder(200);
	    requestAsStringBuffer.append(String.format(baseUrl() + "&page=%d&per_page=%d",
	      paging.pageNumber, paging.numberOfItemsPerPage));
	    
	    if (locale != null ) {
	    	requestAsStringBuffer.append("&locale=");
	    	requestAsStringBuffer.append( locale.asRequestParameter());
	    }
	    
	    return requestAsStringBuffer.toString();
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
