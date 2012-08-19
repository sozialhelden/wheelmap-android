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
package org.wheelmap.android.view;

import org.wheelmap.android.online.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;

public class NodeTypeItemView extends FrameLayout implements TypeItemView {
	private CheckedTextView mText;

	public NodeTypeItemView(Context context) {
		super(context);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.item_search_nodetype, this, true);
		mText = (CheckedTextView) findViewById(R.id.text);
	}

	public void setText(String text) {
		mText.setText(text);
	}
}
