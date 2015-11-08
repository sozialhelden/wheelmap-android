package org.wheelmap.android.popup;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import org.wheelmap.android.activity.MapActivity;
import org.wheelmap.android.adapter.WheelchairToiletSelectAdapter;
import org.wheelmap.android.adapter.WheelchairStateSelectAdapter;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.MapActivityUtils;
import org.wheelmap.android.view.WheelchairFilterStateItemView;

public class FilterWindow extends PopupWindow {

    MapActivity context;
    View menuView;
    View menuItemView;
    private final int viewPadding = 20;

    int height = 0;

    public FilterWindow(MapActivity context, View menuView, View menuItemView) {
        super(context);
        this.context = context;
        this.menuItemView = menuItemView;
        this.menuView = menuView;
        init();
    }

    public void init() {
        setBackgroundDrawable(new BitmapDrawable());
        setFocusable(true);
        setOutsideTouchable(true);

        LinearLayout layoutContainer = new LinearLayout(context);
        layoutContainer.setOrientation(LinearLayout.VERTICAL);
        layoutContainer.setBackgroundColor(Color.WHITE);
        WheelchairStateSelectAdapter filterAdapter;
        if (menuItemView != null) {
            if (menuItemView.getId() == R.id.menu_filter) {
                filterAdapter = new WheelchairStateSelectAdapter(context);
            } else {
                filterAdapter = new WheelchairToiletSelectAdapter(context);
            }
            initFilterAdapterUsage(layoutContainer, filterAdapter);

            setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
            setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
            layoutContainer.measure(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            height = layoutContainer.getMeasuredHeight();
            layoutContainer.setPadding(viewPadding, viewPadding, viewPadding, viewPadding);
            setContentView(layoutContainer);
        }
    }

    private void initFilterAdapterUsage(LinearLayout layoutContainer, final WheelchairStateSelectAdapter adapter) {
        for (int i = 0; i < adapter.getCount(); i++) {
            final int pos = i;
            final WheelchairFilterStateItemView view = adapter.getView(i, null, null);
            view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SupportManager.AccessFilterAttributes item = adapter.getItem(pos);
                        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                        boolean isSet = mPrefs.getBoolean(item.prefsKey, true);
                        boolean toggleSet = !isSet;
                        mPrefs.edit().putBoolean(item.prefsKey, toggleSet).commit();
                        view.mWheelStateCheckBox.setChecked(toggleSet);
                        if (menuItemView.getId() == R.id.menu_filter) {
                            MapActivityUtils.setAccessFilterOptionDrawable(context, null, menuItemView);
                        } else {
                            MapActivityUtils.setWcFilterOptionsDrawable(context, null, menuItemView);
                        }
                    }
                }

            );
            layoutContainer.addView(view);
        }
    }

    @Override
    public void showAsDropDown(View anchor) {
        int[] coords = new int[2];
        anchor.getLocationOnScreen(coords);
        if (coords[1] > height) {
            super.showAsDropDown(anchor, 0, -(height + anchor.getHeight() + viewPadding * 2));
        } else {
            super.showAsDropDown(anchor);
        }
    }
}
