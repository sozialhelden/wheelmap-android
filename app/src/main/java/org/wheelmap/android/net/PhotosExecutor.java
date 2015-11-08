package org.wheelmap.android.net;


import org.wheelmap.android.mapping.node.Photos;
import org.wheelmap.android.model.DataOperationsPhotos;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.net.request.AcceptType;
import org.wheelmap.android.net.request.BasePhotosRequestBuilder;
import org.wheelmap.android.net.request.BoundingBox;
import org.wheelmap.android.net.request.Paging;
import org.wheelmap.android.net.request.PhotosRequestBuilder;
import org.wheelmap.android.service.RestServiceException;
import org.wheelmap.android.utils.GeoMath;
import org.wheelmap.android.utils.ParceableBoundingBox;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import de.akquinet.android.androlog.Log;

/**
 * Created by SMF on 10/03/14.
 */
public class PhotosExecutor extends MultiPageExecutor<Photos> implements
        IExecutor {

    private static final int MAX_PAGES_TO_RETRIEVE = 2;


    private BoundingBox mBoundingBox = null;

    public PhotosExecutor(Context context, Bundle bundle) {
        super(context, bundle, Photos.class);
    }

    @Override
    public void prepareContent() {
        if (getBundle().containsKey(Extra.BOUNDING_BOX)) {
            ParceableBoundingBox parcBoundingBox = (ParceableBoundingBox) getBundle()
                    .getSerializable(Extra.BOUNDING_BOX);
            mBoundingBox = parcBoundingBox.toBoundingBox();
            // Log.d(TAG,
            // "retrieving with bounding box: "
            // + parcBoundingBox.toString());
        } else if (getBundle().containsKey(Extra.LOCATION)) {
            float distance = getBundle().getFloat(Extra.DISTANCE_LIMIT);
            Location location = (Location) getBundle().getParcelable(
                    Extra.LOCATION);
            if(location == null){
                location = new Location("gps");
            }
            mBoundingBox = GeoMath.calculateBoundingBox(
                    new BoundingBox.Wgs84GeoCoordinates(location.getLongitude(), location
                            .getLatitude()), distance);
        }

    }

    @Override
    public void execute(long id) throws RestServiceException {
        BasePhotosRequestBuilder requestBuilder;

            requestBuilder = new PhotosRequestBuilder(getServer(), getApiKey(),
                    AcceptType.JSON, id);

        requestBuilder.paging(new Paging(DEFAULT_TEST_PAGE_SIZE)).boundingBox(
                mBoundingBox);
        clearTempStore();
        retrieveMaxNPages(requestBuilder, MAX_PAGES_TO_RETRIEVE);
    }

    @Override
    public void prepareDatabase() {
        Log.d(getTag(), "prepareDatabase");
        DataOperationsPhotos don = new DataOperationsPhotos(getResolver());
        don.insert(getTempStore());
        clearTempStore();
    }


}