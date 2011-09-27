package org.wheelmap.android.net;

import org.wheelmap.android.model.Support.CategoriesContent;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;

import wheelmap.org.Locale;
import wheelmap.org.domain.categories.Categories;
import wheelmap.org.domain.categories.Category;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.CategoriesRequestBuilder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Bundle;
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
		if ( locale != null && !locale.equals( "de" )) {
			mLocale = new Locale( locale );
		}
		
		getResolver().delete( CategoriesContent.CONTENT_URI, null, null );
	}
	
	@Override
	public void execute() throws SyncServiceException {
		final long startRemote = System.currentTimeMillis();
		CategoriesRequestBuilder requestBuilder = new CategoriesRequestBuilder( SERVER, getApiKey(), AcceptType.JSON );
//		requestBuilder.paging( new Paging( DEFAULT_TEST_PAGE_SIZE ));
		if ( mLocale != null ) 
			requestBuilder.locale( mLocale );

		clearTempStore();
		retrieveSinglePage(requestBuilder);
		
		Log.d(TAG, "remote sync took "
				+ (System.currentTimeMillis() - startRemote) + "ms");
	}

	@Override
	public void prepareDatabase() throws SyncServiceException {
		long insertStart = System.currentTimeMillis();
		for( Categories categories: getTempStore() ) {
			bulkInsert( categories );
		}
		long insertEnd = System.currentTimeMillis();
		Log.d(TAG, "insertTime = " + (insertEnd - insertStart) / 1000f);
		clearTempStore();
	}
	
	private void bulkInsert(Categories categories) {
		int size = categories.getCategories().size();
		ContentValues[] contentValuesArray = new ContentValues[size];
		int i;
		for (i = 0; i < size; i++) {
			ContentValues values = new ContentValues();
			copyCategoryToValues(categories.getCategories().get(i),
					values );
			contentValuesArray[i] = values;
		}

		getResolver().bulkInsert(CategoriesContent.CONTENT_URI,
				contentValuesArray);
	}

	private void copyCategoryToValues(Category category, ContentValues values) {
		values.clear();
		values.put( CategoriesContent.CATEGORY_ID, category.getId().intValue());
		values.put( CategoriesContent.LOCALIZED_NAME, category.getLocalizedName());
		values.put( CategoriesContent.IDENTIFIER, category.getIdentifier());
		values.put( CategoriesContent.SELECTED, CategoriesContent.SELECTED_YES );
	}
}
