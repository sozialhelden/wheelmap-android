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
