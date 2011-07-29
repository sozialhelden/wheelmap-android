package wheelmap.org.request;

import java.net.URI;

import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Sends the {@link HttpUriRequest}s to the REST-Server (Testsystem, e.g. is http://staging.wheelmap.org/api/)
 * @see <a href="http://static.springsource.org/spring-android/docs/1.0.x/reference/html/rest-template.html">Spring android documentation</a>
 * @author p.lipp@web.de
 */
public class RequestProcessor {
	private final RestTemplate restTemplate;

	public RequestProcessor() {
		restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
	}

	public <T> T get (final URI uri, Class<T> clazz) {
		return restTemplate.getForObject(uri,clazz);
	}
	
	public <T> String post (final URI uri, final T postObject) {
		return restTemplate.postForObject(uri, postObject, String.class);
	}
}
