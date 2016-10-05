package org.wheelmap.android.net;

import android.net.Uri;
import android.util.Log;

import org.wheelmap.android.model.api.ApiResponse;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
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

    private long wmId;

    private Uri uploadFotoUri;
    private Observable<ApiResponse> imageUploadObservable;

    private BehaviorSubject<Status> uploadReadySubject = BehaviorSubject.create();

    public UploadTangoMeasurementExecutor(long wmId) {
        this.wmId = wmId;
    }

    public void uploadImage(Uri uri) {

        if (uploadFotoUri != null && uploadFotoUri != uri) {
            throw new IllegalStateException("UploadTangoMeasurementExecutor can only be used once");
        }

        uploadFotoUri = uri;
        if (imageUploadObservable == null) {
            imageUploadObservable = ApiModule.getInstance().api().uploadImage(wmId, uri)
                    .subscribeOn(Schedulers.io())
                    .flatMap(new Func1<ApiResponse, Observable<ApiResponse>>() {
                        @Override
                        public Observable<ApiResponse> call(ApiResponse apiResponse) {
                            Log.d(TAG, "Upload Response:" + apiResponse);
                            if (apiResponse.isOk()) {
                                return Observable.just(apiResponse);
                            }
                            return Observable.error(new Exception());
                        }
                    })
                    .retry(3)
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

    }

    public void uploadMetaData() {
        uploadImage(uploadFotoUri);

        if (imageUploadObservable == null) {
            throw new IllegalStateException("need to upload the image first");
        }

        imageUploadObservable.take(1)
                .flatMap(new Func1<Object, Observable<?>>() {
                    @Override
                    public Observable<Object> call(Object o) {
                        // TODO make real request
                        return Observable.just(null)
                                .delay(1, TimeUnit.SECONDS);
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
        uploadMetaData();
    }

}
