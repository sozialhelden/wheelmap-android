package org.wheelmap.android.model.api;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class MeasurementInfoWrapper implements Parcelable {

    abstract MeasurementInfo metadata();

    public static MeasurementInfoWrapper create(MeasurementInfo info) {
        return new AutoValue_MeasurementInfoWrapper(info);
    }

    public static TypeAdapter<MeasurementInfoWrapper> typeAdapter(Gson gson) {
        return new AutoValue_MeasurementInfoWrapper.GsonTypeAdapter(gson);
    }

}
