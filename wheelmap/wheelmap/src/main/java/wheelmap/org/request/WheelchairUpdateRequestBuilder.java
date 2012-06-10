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

import wheelmap.org.WheelchairState;

/**
 * Constructs the Uri of a <code>/api/nodes/{node_id}/update_wheelchair</code>
 * request
 */
public class WheelchairUpdateRequestBuilder extends RequestBuilder {

	private static final String RESOURCE = "nodes";
	private String id;
	private WheelchairState state;

	public WheelchairUpdateRequestBuilder(final String server,
			final String apiKey, final AcceptType acceptType, String id,
			WheelchairState state) {
		super(server, apiKey, acceptType);
		this.id = id;
		this.state = state;
	}

	@Override
	public String buildRequestUri() {
		final StringBuilder requestAsStringBuffer = new StringBuilder(200);
		requestAsStringBuffer.append(String.format(baseUrl()));
		requestAsStringBuffer.append("&wheelchair=");
		requestAsStringBuffer.append(state.asRequestParameter());

		return requestAsStringBuffer.toString();
	}

	@Override
	protected String resourcePath() {
		return RESOURCE + "/" + id + "/update_wheelchair";
	}
	
	@Override
	public int getRequestType() {
		return RequestBuilder.REQUEST_PUT;
	}
}
