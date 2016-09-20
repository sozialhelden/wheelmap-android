package org.wheelmap.android.fragment.profile;

import org.wheelmap.android.analytics.AnalyticsTrackingManager;
import org.wheelmap.android.modules.ICredentials;
import org.wheelmap.android.modules.UserCredentials;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.UtilsMisc;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LogoutFragment extends Fragment{

    ICredentials mCredentials;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCredentials = new UserCredentials(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_logout,container,false);

        TextView user = (TextView) v.findViewById(R.id.logout_user);
        if (TextUtils.isEmpty(mCredentials.getUserName())) {
            user.setText(R.string.signed_in);
        } else {
            user.setText(String.format(user.getText().toString(), mCredentials.getUserName()));
        }

        v.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCredentials.logout();
                AnalyticsTrackingManager.trackScreen(AnalyticsTrackingManager.TrackableScreensName.OSMLOGOUTSCREEN);
                getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
            }
        });

        if(!UtilsMisc.isTablet(getActivity().getApplicationContext())){
            View parent = v.findViewById(R.id.parent);
            ViewGroup.LayoutParams params = parent.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            parent.setLayoutParams(params);
        }

        return v;
    }
}
