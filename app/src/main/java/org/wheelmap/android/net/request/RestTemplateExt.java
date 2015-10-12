package org.wheelmap.android.net.request;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

import de.akquinet.android.androlog.Log;

public class RestTemplateExt extends RestTemplate {

    private final static String TAG = RestTemplateExt.class.getSimpleName();

    private HttpHeaders requestHeaders = new HttpHeaders();

    private HttpHeaders responseHeaders;

    public HttpHeaders getRequestHttpHeaders() {
        return requestHeaders;
    }

    public HttpHeaders getResponseHttpHeaders() {
        return responseHeaders;
    }

    @Override
    protected <T> T doExecute(URI url, HttpMethod method,
            RequestCallback requestCallback,
            ResponseExtractor<T> responseExtractor) throws RestClientException {

        RequestCallbackDecorator requestCallbackDecorator = new RequestCallbackDecorator(
                requestCallback);
        ResponseExtractor<T> responseExtractorDecorator = new ResponseExtractorDecorator<T>(
                responseExtractor);

        return super.doExecute(url, method, requestCallbackDecorator,
                responseExtractorDecorator);
    }

    private class RequestCallbackDecorator implements RequestCallback {

        public RequestCallbackDecorator(RequestCallback targetRequestCallback) {
            this.targetRequestCallback = targetRequestCallback;
        }

        private RequestCallback targetRequestCallback;

        @Override
        public void doWithRequest(ClientHttpRequest request) throws IOException {

            request.getHeaders().putAll(requestHeaders);
            Log.d(TAG, "requestHeaders" + requestHeaders);
            if (null != targetRequestCallback) {
                targetRequestCallback.doWithRequest(request);
            }
        }
    }

    private class ResponseExtractorDecorator<T> implements ResponseExtractor<T> {

        private ResponseExtractor<T> targetResponseExtractor;

        public ResponseExtractorDecorator(
                ResponseExtractor<T> targetResponseExtractor) {
            this.targetResponseExtractor = targetResponseExtractor;
        }

        @Override
        public T extractData(ClientHttpResponse response) throws IOException {
            responseHeaders = response.getHeaders();
            Log.d(TAG, "responseHeaders: " + responseHeaders);
            if (targetResponseExtractor != null) {
                return targetResponseExtractor.extractData(response);
            }

            return null;
        }

    }

}
