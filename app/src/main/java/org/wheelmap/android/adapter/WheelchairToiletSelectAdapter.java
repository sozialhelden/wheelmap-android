package org.wheelmap.android.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.model.WheelchairFilterState;
import org.wheelmap.android.manager.SupportManager.WheelchairToiletAttributes;
import org.wheelmap.android.view.WheelchairFilterStateItemView;

import java.util.Map;

/**
 * Created by uwe on 14.10.15.
 */
public class WheelchairToiletSelectAdapter extends WheelchairStateSelectAdapter {

    public WheelchairToiletSelectAdapter(Context context) {
        super(context);

        mItems.clear();
        Map<WheelchairFilterState, WheelchairToiletAttributes> attributes = SupportManager.wheelchairToiletAttributes;

        mItems.add(attributes.get(WheelchairFilterState.TOILET_YES));
        mItems.add(attributes.get(WheelchairFilterState.TOILET_NO));
        mItems.add(attributes.get(WheelchairFilterState.TOILET_UNKNOWN));
    }

    @Override
    public WheelchairFilterStateItemView getView(int position, View convertView, ViewGroup parent) {
        WheelchairFilterStateItemView itemView;
        if (convertView != null) {
            itemView = (WheelchairFilterStateItemView) convertView;
        } else {
            itemView = new WheelchairFilterStateItemView(mContext);
        }

        SupportManager.AccessFilterAttributes attributes = mItems.get(position);

        itemView.setIcon(attributes.drawableId);
        itemView.setText(mContext.getString(attributes.settingsStringId));
        itemView.setTextColor(mContext.getResources().getColor(
                attributes.colorId));
        itemView.setCheckboxChecked(mPrefs
                .getBoolean(attributes.prefsKey, true));

        return itemView;

    }
}
