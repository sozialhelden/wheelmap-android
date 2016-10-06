package org.wheelmap.android.model.api;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;

import org.wheelmap.android.net.ApiModule;
import org.wheelmap.android.tango.mode.Mode;

@AutoValue
public abstract class MeasurementMetaData {

    private MetaData metaData;

    public abstract String type();

    public abstract String description();

    abstract JsonObject data();

    public MetaData metaData() {
        if (metaData != null) {
            return metaData;
        }
        switch (type()) {
            case "door":
                metaData = ApiModule.getInstance().gson().fromJson(data(), DoorMetaData.class);
                break;
        }
        return metaData;
    }

    public static MeasurementMetaData create(Mode mode, String description, MetaData data) {
        String type = "";
        switch (mode) {
            case DOOR:
                type = "door";
                if (!(data instanceof DoorMetaData)) {
                    throw new IllegalArgumentException();
                }
                break;
            case RAMP:
                break;
            case STAIR:
                if (!(data instanceof StairMetaData)) {
                    throw new IllegalArgumentException();
                }
                break;
            case TOILET:
                break;
        }

        Gson gson = ApiModule.getInstance().gson();
        JsonParser parser = new JsonParser();
        JsonObject metaData = parser.parse(gson.toJson(data)).getAsJsonObject();
        return new AutoValue_MeasurementMetaData(type, description, metaData);
    }

    public static TypeAdapter<MeasurementMetaData> typeAdapter(Gson gson) {
        return new AutoValue_MeasurementMetaData.GsonTypeAdapter(gson);
    }

    public static abstract class MetaData {}

    @AutoValue
    public static abstract class DoorMetaData extends MetaData {
        public abstract double width();
    }

    @AutoValue
    public static abstract class StairMetaData extends MetaData {
        public abstract double height();
    }

}
