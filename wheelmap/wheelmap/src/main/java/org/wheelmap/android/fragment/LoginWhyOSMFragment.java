package org.wheelmap.android.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.TextView;
import org.wheelmap.android.online.R;

/**
 * Created by dschmidt on 26.03.15.
 */
public class LoginWhyOSMFragment extends Fragment {

    public final static String TAG = LoginWhyOSMFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_login_why_osm, container, false);

        Button understood = (Button)v.findViewById(R.id.fragment_login_why_osm_btn_ok);

        final FragmentManager fm = getActivity().getSupportFragmentManager();

        understood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //remove fragment
                fm.beginTransaction().remove(LoginWhyOSMFragment.this).commit();
            }
        });

        return v;
    }
}
