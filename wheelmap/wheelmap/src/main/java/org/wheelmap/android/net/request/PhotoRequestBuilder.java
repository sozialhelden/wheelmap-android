package org.wheelmap.android.net.request;

/**
 * Created by SMF on 10/03/14.
 */
public class PhotoRequestBuilder extends RequestBuilder{

    private static final String RESOURCE = "nodes";

    private static final String PHOTOS = "photos";

    private String id;

    public PhotoRequestBuilder(final String server, final String apiKey, final AcceptType acceptType, String id){
        super(server,apiKey,acceptType);
        this.id = id;
    }

    @Override
    public String buildRequestUri() {
        final StringBuilder requestAsStringBuffer = new StringBuilder(200);
        requestAsStringBuffer.append(String.format(baseUrl()));

        return requestAsStringBuffer.toString();
    }

    @Override
    protected String resourcePath() {
        return String.format("%s/%s/%s", RESOURCE, id, PHOTOS);
    }

    @Override
    public int getRequestType() {
        return RequestBuilder.REQUEST_GET;
    }
}
