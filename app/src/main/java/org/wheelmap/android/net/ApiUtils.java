package org.wheelmap.android.net;

import retrofit2.Response;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

class ApiUtils {

    private static class ApiException extends Exception {
        ApiException(Response response) {
            super(response.code() + ": " +response.message());
        }
    }

    static <T> Observable.Transformer<Response<T>, T> failRequestAsError() {
        return new Observable.Transformer<Response<T>, T>() {
            @Override
            public Observable<T> call(Observable<Response<T>> observable) {
                return observable.flatMap(new Func1<Response<T>, Observable<T>>() {
                    @Override
                    public Observable<T> call(Response<T> tResponse) {
                        if (tResponse.isSuccessful()) {
                            return Observable.just(tResponse.body());
                        } else {
                            return Observable.error(new ApiException(tResponse));
                        }
                    }
                });
            }
        };
    }

    static <T> Single.Transformer<Response<T>, T> failRequestAsErrorSingle() {
        return new Single.Transformer<Response<T>, T>() {
            @Override
            public Single<T> call(Single<Response<T>> observable) {
                return observable.flatMap(new Func1<Response<T>, Single<T>>() {
                    @Override
                    public Single<T> call(Response<T> tResponse) {
                        if (tResponse.isSuccessful()) {
                            return Single.just(tResponse.body());
                        } else {
                            return Single.error(new ApiException(tResponse));
                        }
                    }
                });
            }
        };
    }

}
