package org.wheelmap.android.model.api;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import org.wheelmap.android.net.ApiModule;
import org.wheelmap.android.tango.mode.Mode;

import java.util.Map;

@AutoValue
public abstract class MeasurementInfo implements Parcelable {

    private MetaData metaData;

    public abstract String type();

    public abstract String description();

    public abstract MeasurementInfo withDescription(String description);

    abstract Map<String, Object> data();

    public MetaData metaData() {
        if (metaData != null) {
            return metaData;
        }
        Gson gson =  ApiModule.getInstance().gson();
        String json = gson.toJson(data());
        switch (type()) {
            case "door":
                metaData = gson.fromJson(json, DoorMetaData.class);
                break;
        }
        return metaData;
    }

    public static MeasurementInfo create(Mode mode, String description, MetaData data) {
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
        Map<String, Object> metaData = gson.fromJson(gson.toJson(data), new TypeToken<Map<String, Object>>(){}.getType());
        return new AutoValue_MeasurementInfo(type, description, metaData);
    }

    public static TypeAdapter<MeasurementInfo> typeAdapter(Gson gson) {
        return new AutoValue_MeasurementInfo.GsonTypeAdapter(gson);
    }

    public static abstract class MetaData implements Parcelable {}

    @AutoValue
    public static abstract class DoorMetaData extends MetaData {
        public abstract double width();

        public static DoorMetaData create(double width) {
            return new AutoValue_MeasurementInfo_DoorMetaData(width);
        }
    }

    @AutoValue
    public static abstract class StairMetaData extends MetaData {
        public abstract double height();

        public static StairMetaData create(double height) {
            return new AutoValue_MeasurementInfo_StairMetaData(height);
        }
    }

    @AutoValue
    public static abstract class RampMetaData extends MetaData {
        public abstract double angle();

        public static RampMetaData create(double angle) {
            return new AutoValue_MeasurementInfo_RampMetaData(angle);
        }
    }


    @AutoValue
    public static abstract class ToiletMetaData extends MetaData {
        public abstract double space();

        public static ToiletMetaData create(double space) {
            return new AutoValue_MeasurementInfo_ToiletMetaData(space);
        }
    }
}
