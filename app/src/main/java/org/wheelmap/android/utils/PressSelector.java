package org.wheelmap.android.utils;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;

public class PressSelector implements OnTouchListener{

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_CANCEL 
				|| event.getAction() == MotionEvent.ACTION_UP){
			setAlphaForView(v, 1);
		}else{
			setAlphaForView(v, 0.5f);
			//return true;
		}
		return false;
	}


	public static void setAlphaForView(View alphaView, float alpha) {
        if(alphaView == null){
            return;
        }
		AlphaAnimation animation = new AlphaAnimation(alpha, alpha);
		animation.setDuration(0);
		animation.setFillAfter(true);
		alphaView.startAnimation(animation);
	}
}
