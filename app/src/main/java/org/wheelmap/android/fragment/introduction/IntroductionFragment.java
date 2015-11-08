package org.wheelmap.android.fragment.introduction;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.wheelmap.android.online.R;

/**
 * Created by uwe on 04.11.15.
 */
public class IntroductionFragment extends Fragment {

    private final static String LAYOUT_ID_KEY = "LAYOUT_ID_KEY";
    private final static String IMAGE_ID_KEY = "IMAGE_ID_KEY";
    private final static String LABEL_ID_KEY = "LABEL_ID_KEY";
    private final static String TEXT_ID_KEY = "TEXT_ID_KEY";

    public static IntroductionFragment newInstance(int layoutId, int imageId, int labelId, int textId) {
        Bundle b = new Bundle();

        b.putInt(LAYOUT_ID_KEY, layoutId);
        b.putInt(IMAGE_ID_KEY, imageId);
        b.putInt(LABEL_ID_KEY, labelId);
        b.putInt(TEXT_ID_KEY, textId);

        IntroductionFragment f = new IntroductionFragment();
        f.setArguments(b);

        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View layout = inflater.inflate(getArguments().getInt(LAYOUT_ID_KEY), container, false);

        ImageView imageView = (ImageView) layout.findViewById(R.id.introduction_image);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setImageDrawable(getActivity().getDrawable(getArguments().getInt(IMAGE_ID_KEY)));
        } else {
            imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), getArguments().getInt(IMAGE_ID_KEY)));
        }

        TextView labelTextView = (TextView) layout.findViewById(R.id.introduction_label);
        labelTextView.setText(getArguments().getInt(LABEL_ID_KEY));
        TextView textView = (TextView) layout.findViewById(R.id.introduction_text);
        textView.setText(getArguments().getInt(TEXT_ID_KEY));

        return layout;
    }
}