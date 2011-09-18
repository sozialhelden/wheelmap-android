package org.wheelmap.android.net;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.wheelmap.android.model.Support;
import org.wheelmap.android.model.Support.NodeTypesContent;
import org.wheelmap.android.service.SyncService;

import wheelmap.org.Locale;
import wheelmap.org.domain.nodetype.NodeType;
import wheelmap.org.domain.nodetype.NodeTypes;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.NodeTypesRequestBuilder;
import wheelmap.org.request.Paging;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

public class NodeTypesExecutor extends BaseRetrieveExecutor<NodeTypes> implements IExecutor {
	public static final String PREF_KEY_WHEELCHAIR_STATE = "wheelchairState";
	private Locale mLocale;

	public NodeTypesExecutor( ContentResolver resolver, Bundle bundle) {
		super(resolver, bundle, NodeTypes.class);
	}

	@Override
	public void prepareContent() {
		String locale = getBundle().getString( SyncService.EXTRA_LOCALE );
		if ( locale != null ) {
			mLocale = new Locale( locale );
		}
				
		getResolver().delete( NodeTypesContent.CONTENT_URI, null, null );
	}
	
	@Override
	public void execute() throws ExecutorException {
		final long startRemote = System.currentTimeMillis();
		NodeTypesRequestBuilder requestBuilder = new NodeTypesRequestBuilder( SERVER, API_KEY, AcceptType.JSON );
		requestBuilder.paging( new Paging( DEFAULT_TEST_PAGE_SIZE ));
		if ( mLocale != null )
			requestBuilder.locale( mLocale );

		clearTempStore();
		try {
			// retrieveAllPages( requestBuilder );
			retrieveSinglePage(requestBuilder);
		} catch ( Exception e ) {
			throw new ExecutorException( e );
		}
		Log.d(TAG, "remote sync took "
				+ (System.currentTimeMillis() - startRemote) + "ms");
	}

	@Override
	public void prepareDatabase() throws ExecutorException {
		long insertStart = System.currentTimeMillis();
		for( NodeTypes nodeTypes: getTempStore()) {			
			try {
				batchApply( nodeTypes );
			} catch (RemoteException e) {
				throw new ExecutorException( e );
			} catch (OperationApplicationException e) {
				throw new ExecutorException( e );
			} catch (IOException e) {
				throw new ExecutorException( e );
			}
		}
		long insertEnd = System.currentTimeMillis();
		Log.d(TAG, "insertTime = " + (insertEnd - insertStart) / 1000f);
		clearTempStore();
	}
	
	private void batchApply( NodeTypes nodeTypes ) throws RemoteException, OperationApplicationException, IOException {
		ContentValues values = new ContentValues();
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		for( NodeType nodeType: nodeTypes.getNodeTypes()) {
		 nodeType.setIconData( retrieveIconData( nodeType.getIconUrl() ));			
			copyCategoryToValues( nodeType, values );
			ContentProviderOperation operation = ContentProviderOperation
					.newInsert(NodeTypesContent.CONTENT_URI).withValues(values).build();
			operations.add( operation );
		}
		
		getResolver().applyBatch( Support.AUTHORITY, operations);
	}
	
	private byte[] retrieveIconData( String iconUrl ) throws IOException {
		URL url = new URL(String.format( "http://%s/images/icons/%s", SERVER, iconUrl));
		
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		InputStream is = null;
		try {
		  is = url.openStream();
		  Log.v(TAG,  "Icon found at " + url.toString());
		  byte[] byteChunk = new byte[2048]; // Or whatever size you want to read in at a time.
		  int n;

		  while ( (n = is.read(byteChunk)) > 0 ) {
		    bais.write(byteChunk, 0, n);
		  }
		}
		catch ( FileNotFoundException e) {
			Log.v(TAG, "Sorry - Icon not found at " + url.toString());
		}
		finally {
		  if (is != null) { is.close(); }
		}
		
		if ( bais.size() == 0 )
			return null;
		else
			return bais.toByteArray();
	}

	private void copyCategoryToValues(NodeType nodeType, ContentValues values) {
		values.clear();
		values.put( NodeTypesContent.NODETYPE_ID, nodeType.getId().intValue());
		values.put( NodeTypesContent.IDENTIFIER, nodeType.getIdentifier());
		values.put( NodeTypesContent.ICON_URL, nodeType.getIconUrl());
		values.put( NodeTypesContent.ICON_DATA, nodeType.getIconData());
		values.put( NodeTypesContent.LOCALIZED_NAME, nodeType.getLocalizedName());
		values.put( NodeTypesContent.CATEGORY_ID, nodeType.getCategoryId().intValue());
		// values.put( NodeTypesContent.CATEGORY_ID, nodeType.getCategory().getId().intValue());
		// values.put( NodeTypesContent.CATEGORY_IDENTIFIER, nodeType.getCategory().getIdentifier());	
	}
}
