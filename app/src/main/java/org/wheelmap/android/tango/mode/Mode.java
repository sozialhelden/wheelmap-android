package org.wheelmap.android.tango.mode;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import org.wheelmap.android.online.R;
import org.wheelmap.android.tango.renderer.WheelmapModeRenderer;

public enum Mode {

    DOOR,
    STAIR,
    RAMP,
    TOILET;

    @StringRes
    public int title() {
        switch (this) {
            case DOOR:
                return R.string.tango_mode_door;
            case STAIR:
                return R.string.tango_mode_stair;
            case RAMP:
                return R.string.tango_mode_ramp;
            case TOILET:
                return R.string.tango_mode_toilet;
            default:
                throw new IllegalStateException("unimplemented renderer");
        }
    }

    @DrawableRes
    public int icon() {
        switch (this) {
            case DOOR:
                return R.drawable.ic_door;
            case STAIR:
                return R.drawable.ic_stair;
            case RAMP:
                return R.drawable.ic_ramp;
            case TOILET:
                return R.drawable.ic_tango_wc;
            default:
                throw new IllegalStateException("unimplemented renderer");
        }
    }

    @StringRes
    public int tutorialTitle() {
        switch (this) {
            case DOOR:
                return R.string.tango_tutorial_door_title;
            case STAIR:
                return R.string.tango_tutorial_stair_title;
            case RAMP:
                return R.string.tango_tutorial_ramp_title;
            case TOILET:
                return R.string.tango_tutorial_wc_title;
            default:
                throw new IllegalStateException("unimplemented renderer");
        }
    }

    @StringRes
    public int tutorialText() {
        switch (this) {
            case DOOR:
                return R.string.tango_tutorial_door_text;
            case STAIR:
                return R.string.tango_tutorial_stair_text;
            case RAMP:
                return R.string.tango_tutorial_ramp_text;
            case TOILET:
                return R.string.tango_tutorial_wc_text;
            default:
                throw new IllegalStateException("unimplemented renderer");
        }
    }


    @DrawableRes
    public int tutorialImage() {
        switch (this) {
            case DOOR:
                return R.drawable.img_tango_door;
            case STAIR:
                return R.drawable.img_tango_stairs;
            case RAMP:
                return R.drawable.img_tango_ramp;
            case TOILET:
                return R.drawable.img_tango_wc;
            default:
                throw new IllegalStateException("unimplemented renderer");
        }
    }

    public WheelmapModeRenderer newRenderer() {
        switch (this) {
            case DOOR:
                return new MeasureDistanceModeRenderer();
            case STAIR:
                return new MeasureDistanceModeRenderer();
            case RAMP:
                return new MeasureSlopeAngleModeRenderer();
            case TOILET:
                return new MeasureDistanceModeRenderer();
            default:
                throw new IllegalStateException("unimplemented renderer");
        }
    }

}
