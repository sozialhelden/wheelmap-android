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
package org.wheelmap.android.net;

import org.wheelmap.android.model.Support.LocalesContent;
import org.wheelmap.android.service.SyncServiceException;

import wheelmap.org.domain.locale.Locales;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.LocalesRequestBuilder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Bundle;
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
			bulkInsert( locales );
		}
		long insertEnd = System.currentTimeMillis();
		Log.d(TAG, "insertTime = " + (insertEnd - insertStart) / 1000f);
		clearTempStore();
	}
	
	private void bulkInsert(Locales locales) {
		int size = locales.getLocales().size();
		
		ContentValues[] contentValuesArray = new ContentValues[size];
		int i = 0;
		for( String localeId: locales.getLocales().keySet()) {
			ContentValues values = new ContentValues();
			copyLocaleToValues(localeId, locales.getLocales().get( localeId ), values );
			contentValuesArray[i] = values;
			i++;
		}

		getResolver().bulkInsert(LocalesContent.CONTENT_URI,
				contentValuesArray);
	}

	private void copyLocaleToValues( String localeId, String localeName, ContentValues values) {
		values.clear();
		values.put( LocalesContent.LOCALE_ID, localeId);
		values.put( LocalesContent.LOCALIZED_NAME, localeName);
	}
}
