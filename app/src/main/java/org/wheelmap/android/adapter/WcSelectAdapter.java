package org.wheelmap.android.adapter;

import android.content.Context;

import org.wheelmap.android.model.WheelchairState;

/**
 * Created by uwe on 14.10.15.
 */
public class WcSelectAdapter extends WheelchairStateSelectAdapter {

    public WcSelectAdapter(Context context) {
        super(context);

        if(mItems.lastIndexOf(WheelchairState.UNKNOWN) != -1) {
            mItems.remove(mItems.lastIndexOf(WheelchairState.UNKNOWN));
        }
    }
}
