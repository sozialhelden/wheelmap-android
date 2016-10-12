package org.wheelmap.android.model.api;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class MeasurementImageUploadResponse {

    public abstract long id();

    public static TypeAdapter<MeasurementImageUploadResponse> typeAdapter(Gson gson) {
        return new AutoValue_MeasurementImageUploadResponse.GsonTypeAdapter(gson);
    }

}
