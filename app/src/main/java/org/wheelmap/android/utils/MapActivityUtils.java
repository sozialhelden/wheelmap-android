package org.wheelmap.android.utils;

import com.actionbarsherlock.view.MenuItem;

import org.wheelmap.android.activity.MapActivity;
import org.wheelmap.android.adapter.WheelchairStateSelectAdapter;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.model.WheelchairState;
import org.wheelmap.android.online.R;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MapActivityUtils {
    public static void setFilterDrawable(MapActivity context, MenuItem item,View v) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Map<WheelchairState, SupportManager.WheelchairAttributes> attributes = SupportManager.wsAttributes;
        Resources r = context.getResources();
        List<Drawable> layers = new LinkedList<Drawable>();
        layers.add(r.getDrawable(R.drawable.map_navbar_btn_filter));
        if (mPrefs.getBoolean(attributes.get(WheelchairState.YES).prefsKey, true)) {
            layers.add(r.getDrawable(R.drawable.map_navbar_btn_filter_green));
        }
        if(mPrefs.getBoolean(attributes.get(WheelchairState.LIMITED).prefsKey, true)){
            layers.add(r.getDrawable(R.drawable.map_navbar_btn_filter_orange));
        }
        if (mPrefs.getBoolean(attributes.get(WheelchairState.NO).prefsKey, true)) {
            layers.add(r.getDrawable(R.drawable.map_navbar_btn_filter_red));
        }
        if (mPrefs.getBoolean(attributes.get(WheelchairState.UNKNOWN).prefsKey, true)) {
            layers.add(r.getDrawable(R.drawable.map_navbar_btn_filter_grey));
        }
        LayerDrawable layerDrawable = new LayerDrawable(layers.toArray(new Drawable[layers.size()]));

        setOptionsItemLayerDrawable(context, item, layerDrawable);

        if(v != null && v instanceof ImageView){
            ((ImageView)v).setImageDrawable(layerDrawable);
        }
    }

    //TODO - rewrite for wc-filter-entries (images & co)
    public static void setWcFilterOptionsDrawable(MapActivity context, MenuItem item,View v) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Map<WheelchairState, SupportManager.WheelchairAttributes> attributes = SupportManager.wsAttributes;
        Resources r = context.getResources();
        List<Drawable> layers = new LinkedList<Drawable>();
        layers.add(r.getDrawable(R.drawable.ic_wc));
        if (mPrefs.getBoolean(attributes.get(WheelchairState.YES).prefsKey, true)) {
            layers.add(r.getDrawable(R.drawable.map_navbar_btn_filter_green));
        }
//        if(mPrefs.getBoolean(attributes.get(WheelchairState.LIMITED).prefsKey, true)){
//            layers.add(r.getDrawable(R.drawable.map_navbar_btn_filter_orange));
//        }
        if (mPrefs.getBoolean(attributes.get(WheelchairState.NO).prefsKey, true)) {
            layers.add(r.getDrawable(R.drawable.map_navbar_btn_filter_red));
        }
        if (mPrefs.getBoolean(attributes.get(WheelchairState.UNKNOWN).prefsKey, true)) {
            layers.add(r.getDrawable(R.drawable.map_navbar_btn_filter_grey));
        }
        LayerDrawable layerDrawable = new LayerDrawable(layers.toArray(new Drawable[layers.size()]));

        setOptionsItemLayerDrawable(context, item, layerDrawable);

        if(v != null && v instanceof ImageView){
            ((ImageView)v).setImageDrawable(layerDrawable);
        }
    }

    private static void setOptionsItemLayerDrawable(final MapActivity context, final MenuItem item, LayerDrawable layerDrawable){

        if(item != null){

            ImageView image;
            if(item.getActionView() != null){
                image = (ImageView) item.getActionView();
                image.setImageDrawable(layerDrawable);
            }else{
                image = new ImageView(context);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                image.setLayoutParams(params);
                image.setPadding(5, 5, 5, 5);
                image.setImageDrawable(layerDrawable);
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                         context.onOptionsItemSelected(item);
                    }
                });
                item.setActionView(image);
            }
        }
    }

    public static void setWheelchairFilterToEngageMode(final MapActivity context) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Map<WheelchairState, SupportManager.WheelchairAttributes> attributes = SupportManager.wsAttributes;

        mPrefs.getBoolean(attributes.get(WheelchairState.YES).prefsKey, true);
        SharedPreferences.Editor editor = mPrefs.edit();

        editor.putBoolean(attributes.get(WheelchairState.YES).prefsKey, false);
        editor.putBoolean(attributes.get(WheelchairState.NO).prefsKey, false);
        editor.putBoolean(attributes.get(WheelchairState.LIMITED).prefsKey, false);
        editor.putBoolean(attributes.get(WheelchairState.UNKNOWN).prefsKey, true);

        editor.commit();
    }

    public static void resetWheelchairFilter(final MapActivity context) {

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mPrefs.edit();

        final WheelchairStateSelectAdapter adapter = new WheelchairStateSelectAdapter(context);

        for(int i=0;i<adapter.getCount();i++){
            final int pos = i;
            SupportManager.WheelchairAttributes item = adapter.getItem(pos);
            editor.putBoolean(item.prefsKey, true);
        }

        editor.commit();
    }
}
