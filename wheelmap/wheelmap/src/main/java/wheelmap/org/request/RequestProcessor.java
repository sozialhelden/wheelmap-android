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

import java.net.URI;

import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Sends the {@link HttpUriRequest}s to the REST-Server (Testsystem, e.g. is
 * http://staging.wheelmap.org/api/)
 * 
 * @see <a
 *      href="http://static.springsource.org/spring-android/docs/1.0.x/reference/html/rest-template.html">Spring
 *      android documentation</a>
 * @author p.lipp@web.de
 */
public class RequestProcessor {
	private final RestTemplate restTemplate;

	public RequestProcessor() {
		restTemplate = new RestTemplate();
		restTemplate
				.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		restTemplate.getMessageConverters().add(
				new MappingJacksonHttpMessageConverter());

	}

	public <T> T get(final URI uri, Class<T> clazz) {
		return restTemplate.getForObject(uri, clazz);
	}

	public <T> T post(final URI uri, final T postObject, Class<T> clazz) {
		return restTemplate.postForObject(uri, postObject, clazz);
	}

	public <T> void put(final URI uri, final T putObject) {
		restTemplate.put(uri, putObject);
	}
}
