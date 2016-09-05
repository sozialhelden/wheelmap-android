package org.wheelmap.android.activity;

import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.wheelmap.android.activity.base.BaseActivity;
import org.wheelmap.android.fragment.introduction.IntroductionFragment;
import org.wheelmap.android.online.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by uwe on 04.11.15.
 */
public class IntroductionActivity extends BaseActivity {

    private LinearLayout indicatorContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getResources().getBoolean(R.bool.rotation_support)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_introduction);

        indicatorContainer = (LinearLayout) findViewById(R.id.indicator_container);


        List<IntroductionData> introductionDataList = getIntroductionData();

        PagerAdapter adapder = new MyPageAdapter(getSupportFragmentManager(), introductionDataList);
        ViewPager paper = (ViewPager) findViewById(R.id.viewpager);

        paper.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                selectPageIndicator(position);
            }
        });
        paper.setAdapter(adapder);

        Button doneButton = (Button) findViewById(R.id.introduction_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        initIndicatorIcons(introductionDataList.size());
        selectPageIndicator(0);
    }

    private List getIntroductionData(){

        ArrayList<IntroductionData> introductionDataList = new ArrayList<>();

        TypedArray introductionNames = getResources().obtainTypedArray(R.array.introduction_names);
        for(int namesIndex = 0; namesIndex < introductionNames.length(); namesIndex++) {
            int arrayId = introductionNames.getResourceId(namesIndex, -1);
            if(arrayId > -1){
                TypedArray introductionEntryArray = getResources().obtainTypedArray(arrayId);

                //IMPORTENT - declaration-order needs to match xml-declaration-order -->
                int layoutId = introductionEntryArray.getResourceId(0, -1);
                int drawableId = introductionEntryArray.getResourceId(1, -1);
                int boldTextId = introductionEntryArray.getResourceId(2, -1);
                int defaultTextId = introductionEntryArray.getResourceId(3, -1);

                introductionDataList.add(new IntroductionData(layoutId, drawableId, boldTextId, defaultTextId));

                introductionEntryArray.recycle();
            }
        }

        introductionNames.recycle();


        return introductionDataList;
    }

    private void initIndicatorIcons(int indicatorNumber){
        if(indicatorContainer != null){
            for(int position = 0; position < indicatorNumber; position++){
                ImageView indicatorImageView = new ImageView(this);

                int oneDpInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        1, getResources().getDisplayMetrics());

                LinearLayout.LayoutParams layoutParameter = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParameter.setMargins(oneDpInPx * 2, 0, oneDpInPx * 2, 0);
                indicatorImageView.setLayoutParams(layoutParameter);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    indicatorImageView.setImageDrawable(getDrawable(R.drawable.circular_selection_indicator));
                } else {
                    indicatorImageView.setImageDrawable(
                            ContextCompat.getDrawable(this, R.drawable.circular_selection_indicator));
                }
                indicatorContainer.addView(indicatorImageView);
            }
        }
    }

    private void selectPageIndicator(int position){
        if(indicatorContainer != null){
            List<ImageView> indicatorImages = getIndicatorImages(indicatorContainer);
            for (ImageView indicatorImageView : indicatorImages){
                indicatorImageView.setActivated(false);
            }
            indicatorContainer.getChildAt(position).setActivated(true);
        }
    }

    private List<ImageView> getIndicatorImages(ViewGroup parent){
        ArrayList<ImageView> indicatorImageList = new ArrayList<>();
        if(parent != null){
            for(int childCount = 0; childCount < parent.getChildCount(); childCount++){
                View child = parent.getChildAt(childCount);
                if(child instanceof ViewGroup){
                    List<ImageView> deepElements = getIndicatorImages((ViewGroup) child);
                    if(deepElements.size() > 0){
                        indicatorImageList.addAll(deepElements);
                    }
                } else if (child instanceof ImageView){
                    indicatorImageList.add((ImageView) child);
                }
            }
        }
        return indicatorImageList;
    }

    private class MyPageAdapter extends FragmentPagerAdapter{

        private final List<IntroductionData> introductionDataList;

        public MyPageAdapter(FragmentManager supportFragmentManager, List introductionDataList){
            super(supportFragmentManager);
            this.introductionDataList = introductionDataList;
        }

        @Override
        public Fragment getItem(int position) {
            IntroductionData introductionData = introductionDataList.get(position);
            return IntroductionFragment.newInstance(
                    introductionData.layoutId,
                    introductionData.drawableId,
                    introductionData.boldTextId,
                    introductionData.defaultTextId);
        }

        @Override
        public int getCount() {
            return introductionDataList.size();
        }
    }

    public class IntroductionData{
        private final int layoutId;
        private final int drawableId;
        private final int boldTextId;
        private final int defaultTextId;

        public IntroductionData(int layoutId, int drawableId, int boldTextId, int defaultTextId){
            this.layoutId = layoutId;
            this.drawableId = drawableId;
            this.boldTextId = boldTextId;
            this.defaultTextId = defaultTextId;
        }
    }
}
