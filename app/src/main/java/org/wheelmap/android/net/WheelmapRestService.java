package org.wheelmap.android.net;

import org.wheelmap.android.model.api.ApiResponse;
import org.wheelmap.android.model.api.MeasurementMetaData;

import okhttp3.MultipartBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import rx.Observable;

interface WheelmapRestService {

    @Multipart
    @POST("api/nodes/{wmId}/photos")
    Observable<Response<ApiResponse>> uploadImage(@Path("wmId") long wmId, @Part MultipartBody.Part file);

    @Multipart
    @POST("api/nodes/{wmId}/measurements")
    Observable<Response<ApiResponse>> uploadMeasurementImage(@Path("wmId") long wmId, @Part MultipartBody.Part file);

    @POST("/api/nodes/{wmId}/measurements/{measurement_id}/metadata")
    Observable<Response<ApiResponse>> uploadMeasurementMetaData(@Path("wmId") long wmId, @Path("measurement_id") long measurementId, @Body MeasurementMetaData metaData);

}
