package org.wheelmap.android.model.api;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class ApiResponse {

    public abstract String message();

    public boolean isOk() {
        return "OK".equals(message().toUpperCase());
    }

    public static TypeAdapter<ApiResponse> typeAdapter(Gson gson) {
        return new AutoValue_ApiResponse.GsonTypeAdapter(gson);
    }

}
