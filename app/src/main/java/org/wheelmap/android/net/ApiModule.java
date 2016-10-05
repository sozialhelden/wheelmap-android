package org.wheelmap.android.net;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.model.AutoValueGson_AutoValueAdapterFactory;
import org.wheelmap.android.modules.UserCredentials;
import org.wheelmap.android.online.BuildConfig;
import org.wheelmap.android.utils.Constants;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiModule {

    private static ApiModule instance;

    public static ApiModule getInstance() {
        if (instance == null) {
            synchronized (ApiModule.class) {
                if (instance == null) {
                    instance = new ApiModule();
                }
            }
        }
        return instance;
    }

    private WheelmapApi api;
    private Gson gson;

    private ApiModule() {}

    public WheelmapApi api() {
        if (api == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .client(createOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create(gson()))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
            WheelmapRestService apiService = retrofit.create(WheelmapRestService.class);
            api = new WheelmapApi(apiService);
        }
        return api;
    }

    private OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        UserCredentials credentials = new UserCredentials(WheelmapApp.get());
                        Request originalRequest = chain.request();
                        HttpUrl url = originalRequest.url().newBuilder()
                                .addQueryParameter(Constants.Api.QUERY_PARAM_API_KEY, credentials.getApiKey())
                                .build();
                        originalRequest = originalRequest.newBuilder().url(url).build();
                        return chain.proceed(originalRequest);
                    }
                });

        if (BuildConfig.DEBUG) {
            builder.addNetworkInterceptor(new StethoInterceptor());
        }

        return builder.build();
    }

    public Gson gson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .registerTypeAdapterFactory(AutoValueGson_AutoValueAdapterFactory.create())
                    .create();
        }
        return gson;
    }

}
