/*
Copyright (C) 2011 Michal Harakal and Michael Kroez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS-IS" BASIS
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
        
*/

package wheelmap.org.request;

public class ApiKeyRequestBuilder extends RequestBuilder {

	private static final String RESOURCE = "users/authenticate";
	private String email;
	private String password;

	public ApiKeyRequestBuilder(final String server, final AcceptType acceptType) {
		super(server, null, acceptType);
	}
	
	public void setCredentials( String email, String password ) {
		this.email = email;
		this.password = password;
	}

	@Override
	public String buildRequestUri() {
		final StringBuilder requestAsStringBuffer = new StringBuilder(200);
		requestAsStringBuffer.append(String.format(baseUrl()));
		requestAsStringBuffer.append("email=" );
		requestAsStringBuffer.append(email);
		requestAsStringBuffer.append("&");
		requestAsStringBuffer.append("password=" );
		requestAsStringBuffer.append(password);

		return requestAsStringBuffer.toString();
	}
	
	@Override
	protected String baseUrl() {
		return String.format("http://%s/api/%s.%s?",server,
				resourcePath(),acceptType.asRequestParameter());
	}

	@Override
	protected String resourcePath() {
		return RESOURCE;
	}
	
	@Override
	public int getRequestType() {
		return RequestBuilder.REQUEST_GET;
	}
}
