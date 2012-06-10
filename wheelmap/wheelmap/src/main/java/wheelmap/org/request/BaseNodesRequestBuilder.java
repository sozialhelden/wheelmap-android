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

import wheelmap.org.BoundingBox;
import wheelmap.org.WheelchairState;

/**
 * Constructs the Uri of a <code>/api/nodes?bbox&wheelchair&page&per_page</code>
 * request
 * 
 * @author p.lipp@web.de
 */
public abstract class BaseNodesRequestBuilder extends RequestBuilder {

	private Paging paging = Paging.DEFAULT_PAGING;
	private WheelchairState wheelchairState;
	private BoundingBox boundingBox;

	public BaseNodesRequestBuilder(final String server, final String apiKey,
			final AcceptType acceptType) {
		super(server, apiKey, acceptType);
	}

	public BaseNodesRequestBuilder paging(final Paging paging) {
		this.paging = paging;
		return this;
	}

	public BaseNodesRequestBuilder boundingBox(final BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
		return this;
	}

	public BaseNodesRequestBuilder wheelchairState(
			final WheelchairState wheelchairState) {
		this.wheelchairState = wheelchairState;
		return this;
	}

	@Override
	public String buildRequestUri() {
		final StringBuilder requestAsStringBuffer = new StringBuilder(200);
		requestAsStringBuffer.append(String.format(baseUrl()
				+ "&page=%d&per_page=%d", paging.pageNumber,
				paging.numberOfItemsPerPage));

		if (boundingBox != null) {
			requestAsStringBuffer.append("&bbox=");
			requestAsStringBuffer.append(boundingBox.asRequestParameter());
		}

		if (wheelchairState != null
				&& wheelchairState != WheelchairState.NO_PREFERENCE) {
			requestAsStringBuffer.append("&wheelchair=");
			requestAsStringBuffer.append(wheelchairState.asRequestParameter());
		}

		return requestAsStringBuffer.toString();
	}

	public BaseNodesRequestBuilder reset() {
		paging = Paging.DEFAULT_PAGING;
		wheelchairState = null;
		boundingBox = null;

		return this;
	}

	@Override
	public int getRequestType() {
		return RequestBuilder.REQUEST_GET;
	}
}
