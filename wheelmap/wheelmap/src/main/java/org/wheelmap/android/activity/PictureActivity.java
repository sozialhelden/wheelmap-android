package org.wheelmap.android.activity;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.holoeverywhere.app.Activity;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.UtilsMisc;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


/**
 * Created by SMF on 18/03/14.
 */
public class PictureActivity extends Activity{

    public static final String EXTRA_URL="extra_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Bundle extras = getIntent().getExtras();

        ImageView image = new ImageView(this);
        image.setBackgroundColor(Color.TRANSPARENT);
        image.setAdjustViewBounds(true);
        ImageLoader.getInstance().displayImage(extras.getString(EXTRA_URL),image);

        setContentView(image);

        if(UtilsMisc.isTablet(getApplicationContext())){
            View v = image;
            while(v != null && v instanceof ViewGroup){
                v.setBackgroundColor(Color.TRANSPARENT);
                if(v.getParent() instanceof View){
                    v = (View) v.getParent();
                }else{
                    break;
                }
            }
        }
    }
}
