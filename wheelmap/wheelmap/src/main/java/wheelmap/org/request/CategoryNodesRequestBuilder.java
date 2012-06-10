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


/**
 * Constructs the Uri of a <code>/api/categories/1/nodes/search?api_key=&q=&bbox=&wheelchair=</code> request
 * @author p.lipp@web.de
 */
public class CategoryNodesRequestBuilder extends BaseNodesRequestBuilder {
	
	private static final String RESOURCE_SEARCH = "categories/%d/nodes/search";
	private static final String RESOURCE_CATEGORY_ONLY = "categories/%d/nodes";
	private String RESOURCE;
	
	private int category;
	private String searchTerm;

	public CategoryNodesRequestBuilder(final String server, final String apiKey, final AcceptType acceptType, int category, String searchTerm) {
		super(server,apiKey, acceptType);
		this.category = category;
		this.searchTerm = searchTerm;
		if ( searchTerm != null && searchTerm.length() > 0 )
			RESOURCE = RESOURCE_SEARCH;
		else
			RESOURCE = RESOURCE_CATEGORY_ONLY;
	}
	
	@Override
	public String buildRequestUri() {
		String request = super.buildRequestUri();
		if ( searchTerm != null && searchTerm.length() > 0)
			return String.format( "%s&q=%s", request, searchTerm );
		else
			return request;
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
