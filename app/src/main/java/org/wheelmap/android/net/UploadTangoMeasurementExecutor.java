package org.wheelmap.android.net;

import android.net.Uri;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

public class UploadTangoMeasurementExecutor {

    public enum Status {
        ERROR,
        SUCCESS
    }

    private static final String TAG = UploadTangoMeasurementExecutor.class.getSimpleName();

    private Uri uploadFotoUri;
    private Observable<Object> imageUploadObservable;

    private BehaviorSubject<Status> uploadReadySubject = BehaviorSubject.create();

    public void uploadImage(Uri uri) {

        if (uploadFotoUri != null && uploadFotoUri != uri) {
            throw new IllegalStateException("UploadTangoMeasurementExecutor can only be used once");
        }

        uploadFotoUri = uri;
        if (imageUploadObservable == null) {
            // TODO make real request
            imageUploadObservable = Observable.just(null)
                    .delay(5, TimeUnit.SECONDS)
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
                                .delay(2, TimeUnit.SECONDS)
                                .flatMap(new Func1<Object, Observable<Object>>() {
                                    @Override
                                    public Observable<Object> call(Object o) {
                                        // test error state
                                        return Observable.error(new Exception());
                                    }
                                });
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
