package org.wheelmap.android.popup;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import com.actionbarsherlock.view.MenuItem;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.PopupWindow;
import org.holoeverywhere.widget.TextView;
import org.wheelmap.android.activity.MainMultiPaneActivity;
import org.wheelmap.android.activity.MainSinglePaneActivity;
import org.wheelmap.android.activity.MapActivity;
import org.wheelmap.android.adapter.WcSelectAdapter;
import org.wheelmap.android.adapter.WheelchairStateSelectAdapter;
import org.wheelmap.android.fragment.CombinedWorkerFragment;
import org.wheelmap.android.fragment.WorkerFragment;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.WheelchairState;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.MapActivityUtils;
import org.wheelmap.android.view.WheelchairStateItemView;

public class FilterWindow extends PopupWindow {

    MapActivity context;
    View menuView;
    MenuItem menuItem;

    int height = 0;

    public FilterWindow(MapActivity context,View menuView,MenuItem menuItem) {
        super(context);
        this.context = context;
        this.menuItem = menuItem;
        this.menuView = menuView;
        init();
    }

    public void init(){
        setBackgroundDrawable(new BitmapDrawable());
        setFocusable(true);
        setOutsideTouchable(true);

        LinearLayout l = new LinearLayout(context);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackgroundColor(Color.WHITE);
        WheelchairStateSelectAdapter selectedAdapter;
        if(menuItem != null && menuItem.getItemId() == R.id.menu_filter){
            selectedAdapter = new WheelchairStateSelectAdapter(context);
        } else {
            selectedAdapter = new WcSelectAdapter(context);
        }
        final WheelchairStateSelectAdapter adapter = selectedAdapter;
        for(int i=0;i<adapter.getCount();i++){
            final int pos = i;
            final WheelchairStateItemView view = adapter.getView(i,null,null);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SupportManager.WheelchairAttributes item = adapter.getItem(pos);
                    SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean isSet = mPrefs.getBoolean(item.prefsKey, true);
                    boolean toggleSet = !isSet;
                    mPrefs.edit().putBoolean(item.prefsKey, toggleSet).commit();
                    view.mWheelStateCheckBox.setChecked(toggleSet);
                    MapActivityUtils.setFilterDrawable(context,menuItem,menuView);
                }
            });
            l.addView(view);
        }
        setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        l.measure(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        height = l.getMeasuredHeight();
        setContentView(l);
    }

    @Override
    public void showAsDropDown(View anchor) {
        int[] coords = new int[2];
        anchor.getLocationOnScreen(coords);
        if(coords[1] > height){
            super.showAsDropDown(anchor,0,-(height+anchor.getHeight()));
        }else{
            super.showAsDropDown(anchor);
        }
    }
}
