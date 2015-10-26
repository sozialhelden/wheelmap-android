package org.wheelmap.android.adapter;

import android.content.Context;

import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.model.WheelchairState;
import org.wheelmap.android.manager.SupportManager.WcAttributes;

import java.util.Map;

/**
 * Created by uwe on 14.10.15.
 */
public class WcSelectAdapter extends WheelchairStateSelectAdapter {

    public WcSelectAdapter(Context context) {
        super(context);

        mItems.clear();
        Map<WheelchairState, WcAttributes> attributes = SupportManager.wcAttributes;

        mItems.add(attributes.get(WheelchairState.YES));
        mItems.add(attributes.get(WheelchairState.NO));
        mItems.add(attributes.get(WheelchairState.UNKNOWN));
    }
}
