package org.wheelmap.android.tango.mode;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import org.wheelmap.android.online.R;
import org.wheelmap.android.tango.renderer.WheelmapModeRenderer;

public enum Mode {
    DOOR(R.string.tango_mode_door, 0),
    STAIR(R.string.tango_mode_stair, 0),
    RAMP(R.string.tango_mode_ramp, 0),
    TOILET(R.string.tango_mode_toilet, 0)
    ;


    @StringRes
    public final int title;

    @DrawableRes
    public final int icon;

    Mode(int title, int icon) {
        this.title = title;
        this.icon = icon;
    }

    public WheelmapModeRenderer newRenderer() {
        switch (this) {
            case DOOR:
                return new MeasureDistanceModeRenderer();
            case STAIR:
                return new MeasureDistanceModeRenderer();
            case RAMP:
                return new MeasureDistanceModeRenderer();
            case TOILET:
                return new MeasureDistanceModeRenderer();
            default:
                throw new IllegalStateException("unimplemented renderer");
        }
    }
}
