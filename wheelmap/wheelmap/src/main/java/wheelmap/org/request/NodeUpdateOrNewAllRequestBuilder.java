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
 * Constructs the Uri of a <code>/api/nodes/{node_id}</code> update/put and
 * <code>/api/nodes</code> create/post request
 */
public class NodeUpdateOrNewAllRequestBuilder extends RequestBuilder {

	private static final String RESOURCE = "nodes";
	private String id;
	private String name;
	private String category;
	private String type;
	private double latitude;
	private double longitude;
	private WheelchairState state;
	private String wheelchair_desc;
	private String street;
	private String housenumber;
	private String city;
	private String postcode;
	private String website;
	private String phone;

	private boolean update;

	public NodeUpdateOrNewAllRequestBuilder(final String server,
			final String apiKey, final AcceptType acceptType, String id,
			String name, String category, String type, double latitude, double longitude,
			WheelchairState state, String wheelchair_desc, String street,
			String housenumber, String city, String postcode, String website,
			String phone, boolean update) {
		super(server, apiKey, acceptType);
		this.id = id;
		this.name = name;
		this.category = category;
		this.type = type;
		this.latitude = latitude;
		this.longitude = longitude;
		this.state = state;
		this.wheelchair_desc = wheelchair_desc;
		this.street = street;
		this.housenumber = housenumber;
		this.city = city;
		this.postcode = postcode;
		this.website = website;
		this.phone = phone;

		this.update = update;
	}

	@Override
	public String buildRequestUri() {
		final StringBuilder requestAsStringBuffer = new StringBuilder(1024);
		requestAsStringBuffer.append(String.format(baseUrl()));
		requestAsStringBuffer.append("&name=");
		requestAsStringBuffer.append(name);		
		requestAsStringBuffer.append("&type=");
		requestAsStringBuffer.append(type);
		requestAsStringBuffer.append("&lat=");
		requestAsStringBuffer.append(latitude);
		requestAsStringBuffer.append("&lon=");
		requestAsStringBuffer.append(longitude);
		requestAsStringBuffer.append("&wheelchair=");
		requestAsStringBuffer.append(state.asRequestParameter());

		if (category != null && !(category.length() == 0)) {
			requestAsStringBuffer.append("&category=");
			requestAsStringBuffer.append(category);
		}
		
		if (wheelchair_desc != null && !(wheelchair_desc.length() == 0)) {
			String tmpString = wheelchair_desc.length() > 255 ? wheelchair_desc.substring(0, 254) : wheelchair_desc;
			requestAsStringBuffer.append("&wheelchair_description=");
			requestAsStringBuffer.append(tmpString);
		}

		if (street != null && !(street.length() == 0)) {
			requestAsStringBuffer.append("&street=");
			requestAsStringBuffer.append(street);
		}

		if (housenumber != null && !(housenumber.length() == 0)) {
			requestAsStringBuffer.append("&housenumber=");
			requestAsStringBuffer.append(housenumber);
		}

		if (city != null && !(city.length() == 0)) {
			requestAsStringBuffer.append("&city=");
			requestAsStringBuffer.append(city);
		}

		if (postcode != null && !(postcode.length() == 0)) {
			requestAsStringBuffer.append("&postcode=");
			requestAsStringBuffer.append(postcode);
		}

		if (website != null && !(website.length() == 0)) {
			requestAsStringBuffer.append("&website=");
			requestAsStringBuffer.append(website);
		}

		if (phone != null && !(phone.length() == 0)) {
			requestAsStringBuffer.append("&phone=");
			requestAsStringBuffer.append(phone);
		}

		return requestAsStringBuffer.toString();
	}

	@Override
	protected String resourcePath() {
		if (update)
			return RESOURCE + "/" + id;
		else
			return RESOURCE;
	}
	
	@Override
	public int getRequestType() {
		if ( update )
			return RequestBuilder.REQUEST_PUT;
		else
			return RequestBuilder.REQUEST_POST;
	}
}
