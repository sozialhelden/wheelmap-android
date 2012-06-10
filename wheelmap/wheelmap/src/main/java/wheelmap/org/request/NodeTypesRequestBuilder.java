/*
 * #%L
 * Wheelmap - App
 * %%
 * Copyright (C) 2011 - 2012 Michal Harakal - Michael Kroez - Sozialhelden e.V.
 * %%
 * Wheelmap App based on the Wheelmap Service by Sozialhelden e.V.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package wheelmap.org.request;

import wheelmap.org.Locale;

public class NodeTypesRequestBuilder extends RequestBuilder {

	private static final String RESOURCE = "node_types";
	private Paging paging = Paging.DEFAULT_PAGING;
	private Locale locale = null;
	
	public NodeTypesRequestBuilder(final String server, final String apiKey, final AcceptType acceptType) {
		super(server,apiKey, acceptType);
	}
	
	public NodeTypesRequestBuilder paging (final Paging paging) {
		this.paging = paging;
		return this;		
	}
	
	public NodeTypesRequestBuilder locale(final Locale locale ) {
		this.locale = locale;
		return this;
	}
	
	public NodeTypesRequestBuilder reset () {
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
