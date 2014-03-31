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
package org.wheelmap.android.actionbar;

import com.actionbarsherlock.widget.ShareActionProvider;

import org.wheelmap.android.online.R;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

public class DirectionsActionProvider extends ShareActionProvider {

    public DirectionsActionProvider(Context context) {
        super(context);
    }

    @Override
    public View onCreateActionView() {
        View view = super.onCreateActionView();
        ImageView image = (ImageView) view.findViewById(R.id.abs__image);
        image.setImageResource(R.drawable.ic_menu_directions_wheelmap);
        return view;
    }


}
