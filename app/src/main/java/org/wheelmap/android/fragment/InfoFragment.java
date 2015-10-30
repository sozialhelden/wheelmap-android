/*
 * #%L
 * Wheelmap - App
 * %%
 * Copyright (C) 2011 - 2012 Michal Harakal - Michael Kroez - Sozialhelden e.V.
 * %%
 * Wheelmap App based on the Wheelmap Service by Sozialhelden e.V.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wheelmap.android.fragment;

import org.wheelmap.android.online.R;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class InfoFragment extends Fragment {

    private OnInfoListener mListener;

    public interface OnInfoListener {

        public void onNextView(String view);

        public void onViewUri(Uri uri);

        public void dismissInfoView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnInfoListener) {
            mListener = (OnInfoListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_credit_screen, container, false);
        TextView openStreetMapLink = (TextView)v.findViewById(R.id.openStreetMapLink);
        TextView openMapBoxLink = (TextView)v.findViewById(R.id.openMapBox);
        TextView openNicolasLink = (TextView)v.findViewById(R.id.linkNicolasMollet);

        String linkOpenStreetMap = "https://www.openstreetmap.org/copyright";
        String linkOpenMapBox = "http://mapbox.com/about/maps/";
        String linkOpenNicolas = "http://mapicons.nicolasmollet.com";


        openStreetMapLink.setClickable(true);
        openMapBoxLink.setClickable(true);

        String text = "<a href=" + linkOpenStreetMap + ">" + openStreetMapLink.getText() + "</a>";
        openStreetMapLink.setText(Html.fromHtml(text));

        String text1 = "<a href=" + linkOpenMapBox + ">" + openMapBoxLink.getText() + "</a>";
        openMapBoxLink.setText(Html.fromHtml(text1));

        String text2 = "<a href=" + linkOpenNicolas + ">" + openNicolasLink.getText() + "</a>";
        openNicolasLink.setText(Html.fromHtml(text2));

        openStreetMapLink.setMovementMethod(LinkMovementMethod.getInstance());
        openMapBoxLink.setMovementMethod(LinkMovementMethod.getInstance());
        openNicolasLink.setMovementMethod(LinkMovementMethod.getInstance());

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private String createVersionString() {
        PackageInfo packageInfo;
        String version;
        try {
            packageInfo = getActivity().getPackageManager().getPackageInfo(
                    getActivity().getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            version = "Unknown";
        }
        return String.format("%s: %s",
                getResources().getString(R.string.info_wheelmap_android_two),
                version);
    }
}
