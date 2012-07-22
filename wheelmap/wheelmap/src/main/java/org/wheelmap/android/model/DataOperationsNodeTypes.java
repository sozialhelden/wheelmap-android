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
