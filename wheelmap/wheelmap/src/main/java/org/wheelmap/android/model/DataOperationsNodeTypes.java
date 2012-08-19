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
package org.wheelmap.android.model;

import org.wheelmap.android.model.Support.NodeTypesContent;

import wheelmap.org.domain.nodetype.NodeType;
import wheelmap.org.domain.nodetype.NodeTypes;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

public class DataOperationsNodeTypes extends
		DataOperations<NodeTypes, NodeType> {

	public DataOperationsNodeTypes(ContentResolver resolver) {
		super(resolver);
	}

	@Override
	protected NodeType getItem(NodeTypes type, int i) {
		return type.getNodeTypes().get(i);
	}

	@Override
	public void copyToValues(NodeType item, ContentValues values) {
		values.clear();
		values.put(NodeTypesContent.NODETYPE_ID, item.getId().intValue());
		values.put(NodeTypesContent.IDENTIFIER, item.getIdentifier());
		values.put(NodeTypesContent.ICON_URL, item.getIconUrl());
		values.put(NodeTypesContent.LOCALIZED_NAME, item.getLocalizedName());
		values.put(NodeTypesContent.CATEGORY_ID, item.getCategoryId()
				.intValue());
		// values.put( NodeTypesContent.CATEGORY_ID,
		// nodeType.getCategory().getId().intValue());
		// values.put( NodeTypesContent.CATEGORY_IDENTIFIER,
		// nodeType.getCategory().getIdentifier());
	}

	@Override
	public Uri getUri() {
		return NodeTypesContent.CONTENT_URI;
	}

}
