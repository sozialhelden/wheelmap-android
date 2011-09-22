package org.wheelmap.android.net;

import org.wheelmap.android.model.Support.NodeTypesContent;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;

import wheelmap.org.Locale;
import wheelmap.org.domain.nodetype.NodeType;
import wheelmap.org.domain.nodetype.NodeTypes;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.NodeTypesRequestBuilder;
import wheelmap.org.request.Paging;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;

public class NodeTypesExecutor extends BaseRetrieveExecutor<NodeTypes>
		implements IExecutor {
	public static final String PREF_KEY_WHEELCHAIR_STATE = "wheelchairState";
	private Locale mLocale;

	public NodeTypesExecutor(ContentResolver resolver, Bundle bundle) {
		super(resolver, bundle, NodeTypes.class);
	}

	@Override
	public void prepareContent() {
		String locale = getBundle().getString(SyncService.EXTRA_LOCALE);
		if (locale != null && !locale.equals("de")) {
			mLocale = new Locale(locale);
		}

		getResolver().delete(NodeTypesContent.CONTENT_URI, null, null);
	}

	@Override
	public void execute() throws SyncServiceException {
		final long startRemote = System.currentTimeMillis();
		NodeTypesRequestBuilder requestBuilder = new NodeTypesRequestBuilder(
				SERVER, getApiKey(), AcceptType.JSON);
		requestBuilder.paging(new Paging(DEFAULT_TEST_PAGE_SIZE));
		if (mLocale != null) {
			requestBuilder.locale(mLocale);
		}
		clearTempStore();
		retrieveSinglePage(requestBuilder);
		Log.d(TAG, "remote sync took "
				+ (System.currentTimeMillis() - startRemote) + "ms");
	}

	@Override
	public void prepareDatabase() throws SyncServiceException {
		long insertStart = System.currentTimeMillis();
		for (NodeTypes nodeTypes : getTempStore()) {
			bulkInsert(nodeTypes);
		}
		long insertEnd = System.currentTimeMillis();
		Log.d(TAG, "insertTime = " + (insertEnd - insertStart) / 1000f);
		clearTempStore();
	}

	// private void batchApply(NodeTypes nodeTypes) throws RemoteException,
	// OperationApplicationException {
	// ContentValues values = new ContentValues();
	// ArrayList<ContentProviderOperation> operations = new
	// ArrayList<ContentProviderOperation>();
	// for (NodeType nodeType : nodeTypes.getNodeTypes()) {
	// // nodeType.setIconData( retrieveIconData( nodeType.getIconUrl() ));
	// copyCategoryToValues(nodeType, values);
	// ContentProviderOperation operation = ContentProviderOperation
	// .newInsert(NodeTypesContent.CONTENT_URI).withValues(values)
	// .build();
	// operations.add(operation);
	// }
	//
	// getResolver().applyBatch(Support.AUTHORITY, operations);
	// }

	private void bulkInsert(NodeTypes nodeTypes) {
		int size = nodeTypes.getNodeTypes().size();
		ContentValues[] contentValuesArray = new ContentValues[size];
		int i;
		for (i = 0; i < size; i++) {
			ContentValues values = new ContentValues();
			copyNodeTypeToValues(nodeTypes.getNodeTypes().get(i),
					values );
			contentValuesArray[i] = values;
		}

		getResolver().bulkInsert(NodeTypesContent.CONTENT_URI,
				contentValuesArray);
	}

	private void copyNodeTypeToValues(NodeType nodeType, ContentValues values) {
		values.clear();
		values.put(NodeTypesContent.NODETYPE_ID, nodeType.getId().intValue());
		values.put(NodeTypesContent.IDENTIFIER, nodeType.getIdentifier());
		values.put(NodeTypesContent.ICON_URL, nodeType.getIconUrl());
		values.put(NodeTypesContent.LOCALIZED_NAME, nodeType.getLocalizedName());
		values.put(NodeTypesContent.CATEGORY_ID, nodeType.getCategoryId()
				.intValue());
		// values.put( NodeTypesContent.CATEGORY_ID,
		// nodeType.getCategory().getId().intValue());
		// values.put( NodeTypesContent.CATEGORY_IDENTIFIER,
		// nodeType.getCategory().getIdentifier());
	}
}
