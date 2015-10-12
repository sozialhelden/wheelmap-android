package org.wheelmap.android.net.request;

/**
 * Created by SMF on 10/03/14.
 */
public class PhotoRequestBuilder extends BasePhotosRequestBuilder{

    private static final String RESOURCE = "nodes";

    private static final String PHOTOS = "photos";

    private long id = 927092067;

    public PhotoRequestBuilder(final String server, final String apiKey,
            final AcceptType acceptType, long id) {
        super(server, apiKey, acceptType);
        this.id = id;
    }

    @Override
    protected String resourcePath() {
        return String.format("%s/%s/%s", RESOURCE, id, PHOTOS);
    }

    @Override
    public int getRequestType() {
        return RequestBuilder.REQUEST_PUT_PHOTO;
    }
}
