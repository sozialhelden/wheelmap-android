package org.wheelmap.android.net;

import java.util.ArrayList;

import org.wheelmap.android.model.Support;
import org.wheelmap.android.model.Support.LocalesContent;
import org.wheelmap.android.service.SyncServiceException;

import wheelmap.org.domain.locale.Locales;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.LocalesRequestBuilder;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

public class LocalesExecutor extends BaseRetrieveExecutor<Locales> implements IExecutor {
	public static final String PREF_KEY_WHEELCHAIR_STATE = "wheelchairState";
	

	public LocalesExecutor( ContentResolver resolver, Bundle bundle) {
		super(resolver, bundle, Locales.class);
	}

	@Override
	public void prepareContent() {
		getResolver().delete( LocalesContent.CONTENT_URI, null, null );
	}
	
	@Override
	public void execute() throws SyncServiceException {
		final long startRemote = System.currentTimeMillis();
		final LocalesRequestBuilder requestBuilder = new LocalesRequestBuilder( SERVER, getApiKey(), AcceptType.JSON);

		clearTempStore();
		retrieveSinglePage(requestBuilder);
		Log.d(TAG, "remote sync took "
				+ (System.currentTimeMillis() - startRemote) + "ms");
	}

	@Override
	public void prepareDatabase() throws SyncServiceException {
		long insertStart = System.currentTimeMillis();
		for( Locales locales: getTempStore()) {
			try {
				batchApply( locales );
			} catch (RemoteException e) {
				throw new SyncServiceException( SyncServiceException.ERROR_DATABASE_ERROR, e);
			} catch (OperationApplicationException e) {
				throw new SyncServiceException( SyncServiceException.ERROR_DATABASE_ERROR, e);
			}
		}
		long insertEnd = System.currentTimeMillis();
		Log.d(TAG, "insertTime = " + (insertEnd - insertStart) / 1000f);
		clearTempStore();
	}
	
	private void batchApply( Locales locales ) throws RemoteException, OperationApplicationException {
		ContentValues values = new ContentValues();
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		for ( String localeId: locales.getLocales().keySet()) {
			copyLocaleToValues( localeId, locales.getLocales().get( localeId ), values);
			ContentProviderOperation operation = ContentProviderOperation
					.newInsert(LocalesContent.CONTENT_URI).withValues(values).build();
			operations.add( operation );
		}
		
		getResolver().applyBatch( Support.AUTHORITY, operations);
	}

	private void copyLocaleToValues( String localeId, String localeName, ContentValues values) {
		values.clear();
		values.put( LocalesContent.LOCALE_ID, localeId);
		values.put( LocalesContent.LOCALIZED_NAME, localeName);
	}
}
