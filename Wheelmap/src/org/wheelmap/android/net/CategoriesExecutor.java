package org.wheelmap.android.net;

import java.util.ArrayList;

import org.wheelmap.android.model.Support;
import org.wheelmap.android.model.Support.CategoriesContent;
import org.wheelmap.android.service.SyncService;

import wheelmap.org.Locale;
import wheelmap.org.domain.categories.Categories;
import wheelmap.org.domain.categories.Category;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.CategoriesRequestBuilder;
import wheelmap.org.request.Paging;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

public class CategoriesExecutor extends BaseRetrieveExecutor<Categories> implements IExecutor {
	public static final String PREF_KEY_WHEELCHAIR_STATE = "wheelchairState";
	private Locale mLocale;

	public CategoriesExecutor( ContentResolver resolver, Bundle bundle) {
		super(resolver, bundle, Categories.class);
	}

	@Override
	public void prepareContent() {
		String locale = getBundle().getString( SyncService.EXTRA_LOCALE );
		if ( locale != null ) {
			mLocale = new Locale( locale );
		}
		
		getResolver().delete( CategoriesContent.CONTENT_URI, null, null );
	}
	
	@Override
	public void execute() throws ExecutorException {
		final long startRemote = System.currentTimeMillis();
		CategoriesRequestBuilder requestBuilder = new CategoriesRequestBuilder( SERVER, API_KEY, AcceptType.JSON );
//		requestBuilder.paging( new Paging( DEFAULT_TEST_PAGE_SIZE ));
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
		for( Categories categories: getTempStore() ) {
			try {
				batchApply( categories );
			} catch (RemoteException e) {
				throw new ExecutorException( e );
			} catch (OperationApplicationException e) {
				throw new ExecutorException( e );
			}
		}
		long insertEnd = System.currentTimeMillis();
		Log.d(TAG, "insertTime = " + (insertEnd - insertStart) / 1000f);
		clearTempStore();
	}
	
	private void batchApply( Categories categories ) throws RemoteException, OperationApplicationException {
		ContentValues values = new ContentValues();
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		for( Category cat: categories.getCategories()) {
			copyCategoryToValues( cat, values );
			ContentProviderOperation operation = ContentProviderOperation
					.newInsert(CategoriesContent.CONTENT_URI).withValues(values).build();
			operations.add( operation );
		}
		
		getResolver().applyBatch( Support.AUTHORITY, operations);
	}

	private void copyCategoryToValues(Category category, ContentValues values) {
		values.clear();
		values.put( CategoriesContent.CATEGORY_ID, category.getId().intValue());
		values.put( CategoriesContent.LOCALIZED_NAME, category.getLocalizedName());
		values.put( CategoriesContent.IDENTIFIER, category.getIdentifier());
	}
}
