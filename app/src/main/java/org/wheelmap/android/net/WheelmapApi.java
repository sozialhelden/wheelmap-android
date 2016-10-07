package org.wheelmap.android.net;

import android.net.Uri;

import org.wheelmap.android.model.api.ApiResponse;
import org.wheelmap.android.model.api.MeasurementImageUploadResponse;
import org.wheelmap.android.model.api.MeasurementInfo;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;
import rx.Observable;
import rx.functions.Func1;

public class WheelmapApi {

    private WheelmapRestService apiService;

    WheelmapApi(WheelmapRestService apiService) {
        this.apiService = apiService;
    }

    public Observable<ApiResponse> uploadImage(long wmId, Uri uri) {
        File file = new File(uri.getPath());
        RequestBody fbody = RequestBody.create(MediaType.parse("application/octet-stream"), file);

        MultipartBody.Part part = MultipartBody.Part.createFormData("photo", file.getName(), fbody);
        return apiService.uploadImage(wmId, part)
                .compose(ApiUtils.<ApiResponse>failRequestAsError());
    }

    public Observable<MeasurementImageUploadResponse> uploadMeasurementImage(long wmId, Uri uri) {
        File file = new File(uri.getPath());
        RequestBody fbody = RequestBody.create(MediaType.parse("application/octet-stream"), file);

        MultipartBody.Part part = MultipartBody.Part.createFormData("photo", file.getName(), fbody);
        return apiService.uploadMeasurementImage(wmId, part)
                .compose(ApiUtils.<MeasurementImageUploadResponse>failRequestAsError());
    }

    public Observable<Void> uploadMeasurementMetaData(long wmId, MeasurementImageUploadResponse image, MeasurementInfo measurementInfo) {
        return apiService.uploadMeasurementMetaData(wmId, image.id(), measurementInfo)
                .flatMap(new Func1<Response<Void>, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(Response<Void> response) {
                        if (response.isSuccessful()) {
                            return Observable.just(null);
                        }
                        return Observable.error(new Exception("Status code" + response.code()));
                    }
                });
    }

}
