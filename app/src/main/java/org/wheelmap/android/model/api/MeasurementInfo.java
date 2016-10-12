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

    private static final String TYPE_DOOR = "door";
    private static final String TYPE_RAMP = "ramp";
    private static final String TYPE_STAIR = "stair";
    private static final String TYPE_TOILET = "toilet";

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
            case TYPE_DOOR:
                metaData = gson.fromJson(json, DoorMetaData.class);
                break;
            case TYPE_RAMP:
                metaData = gson.fromJson(json, RampMetaData.class);
                break;
            case TYPE_STAIR:
                metaData = gson.fromJson(json, StairMetaData.class);
                break;
            case TYPE_TOILET:
                metaData = gson.fromJson(json, ToiletMetaData.class);
                break;
        }
        return metaData;
    }

    public static MeasurementInfo create(Mode mode, String description, MetaData data) {
        String type = "";
        switch (mode) {
            case DOOR:
                type = TYPE_DOOR;
                if (!(data instanceof DoorMetaData)) {
                    throw new IllegalArgumentException();
                }
                break;
            case RAMP:
                type = TYPE_RAMP;
                if (!(data instanceof RampMetaData)) {
                    throw new IllegalArgumentException();
                }
                break;
            case STAIR:
                type = TYPE_STAIR;
                if (!(data instanceof StairMetaData)) {
                    throw new IllegalArgumentException();
                }
                break;
            case TOILET:
                type = TYPE_TOILET;
                if (!(data instanceof ToiletMetaData)) {
                    throw new IllegalArgumentException();
                }
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
