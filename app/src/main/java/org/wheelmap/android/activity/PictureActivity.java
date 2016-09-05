package org.wheelmap.android.activity;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.wheelmap.android.activity.base.BaseActivity;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.UtilsMisc;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import de.akquinet.android.androlog.Log;


/**
 * Created by SMF on 18/03/14.
 */
public class PictureActivity extends BaseActivity {

    private static final String TAG = PictureActivity.class.getSimpleName();

    public static final String EXTRA_URL="extra_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.activity_picture);

        Bundle extras = getIntent().getExtras();

        ImageView image = (ImageView)findViewById(R.id.image_picture);

        String url = extras.getString(EXTRA_URL);

        Log.d(TAG,"load image: "+url);

        ImageLoader.getInstance().displayImage(url,image);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
