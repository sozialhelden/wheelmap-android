package org.wheelmap.android.net;

import android.net.Uri;

import org.wheelmap.android.model.api.ApiResponse;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;

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

}
