package org.wheelmap.android.net;

import android.net.Uri;
import android.util.Log;

import org.wheelmap.android.model.api.ApiResponse;
import org.wheelmap.android.model.api.MeasurementImageUploadResponse;
import org.wheelmap.android.model.api.MeasurementInfo;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class UploadTangoMeasurementExecutor {

    public enum Status {
        ERROR,
        SUCCESS
    }

    private static final String TAG = UploadTangoMeasurementExecutor.class.getSimpleName();

    /**
     * temporary upload measurements over the photos api
     */
    private static boolean USE_PHOTO_API = true;

    private long wmId;
    private MeasurementInfo measurementInfo;

    private Uri uploadPhotoUri;
    private Observable<MeasurementImageUploadResponse> imageUploadObservable;

    private BehaviorSubject<Status> uploadReadySubject = BehaviorSubject.create();

    public UploadTangoMeasurementExecutor(long wmId) {
        this.wmId = wmId;
    }

    public Observable<MeasurementImageUploadResponse> uploadImage(Uri uri) {

        if (uploadPhotoUri != null && uploadPhotoUri != uri) {
            throw new IllegalStateException("UploadTangoMeasurementExecutor can only be used once");
        }

        uploadPhotoUri = uri;
        if (imageUploadObservable == null) {
            if (USE_PHOTO_API) {
                imageUploadObservable = ApiModule.getInstance().api().uploadImage(wmId, uri)
                        .flatMap(new Func1<ApiResponse, Observable<ApiResponse>>() {
                            @Override
                            public Observable<ApiResponse> call(ApiResponse apiResponse) {
                                Log.d(TAG, "Upload Response:" + apiResponse);
                                if (apiResponse.isOk()) {
                                    return Observable.just(apiResponse);
                                }
                                return Observable.error(new Exception());
                            }
                        }).map(new Func1<ApiResponse, MeasurementImageUploadResponse>() {
                            @Override
                            public MeasurementImageUploadResponse call(ApiResponse apiResponse) {
                                return null;
                            }
                        });
            } else {
                imageUploadObservable = ApiModule.getInstance().api().uploadMeasurementImage(wmId, uri);
            }
            imageUploadObservable = imageUploadObservable
                    .subscribeOn(Schedulers.io())
                    .retry(2)
                    .replay(1)
                    .autoConnect();
            imageUploadObservable.subscribe(new Action1<Object>() {
                @Override
                public void call(Object o) {
                    Log.d(TAG, "" + o);
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    imageUploadObservable = null;
                    throwable.printStackTrace();
                }
            });
        }

        return imageUploadObservable;
    }

    public void uploadMetaData(final MeasurementInfo measurementInfo) {
        this.measurementInfo = measurementInfo;
        uploadImage(uploadPhotoUri).take(1)
                .flatMap(new Func1<MeasurementImageUploadResponse, Observable<?>>() {
                    @Override
                    public Observable<Void> call(MeasurementImageUploadResponse o) {
                        if (USE_PHOTO_API) {
                            return Observable.<Void>just(null)
                                    .delay(1, TimeUnit.SECONDS);
                        } else {
                            return ApiModule.getInstance().api().uploadMeasurementMetaData(wmId, o, measurementInfo)
                                    .subscribeOn(Schedulers.io())
                                    .retry(2);
                        }
                    }
                })
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        uploadReadySubject.onNext(Status.SUCCESS);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        uploadReadySubject.onNext(Status.ERROR);
                    }
                });
    }

    public Observable<Status> uploadReady() {
        return uploadReadySubject.observeOn(AndroidSchedulers.mainThread());
    }

    public void retry() {
        uploadMetaData(measurementInfo);
    }

}
