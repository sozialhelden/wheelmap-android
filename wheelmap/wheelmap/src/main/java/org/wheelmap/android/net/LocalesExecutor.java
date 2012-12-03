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

import android.content.ContentValues;
import android.database.Cursor;
import org.wheelmap.android.model.DataOperationsLocales;
import org.wheelmap.android.model.Support.LastUpdateContent;
import org.wheelmap.android.model.Support.LocalesContent;
import org.wheelmap.android.service.SyncServiceException;

import wheelmap.org.domain.locale.Locales;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.LocalesRequestBuilder;
import android.content.Context;
import android.os.Bundle;
import de.akquinet.android.androlog.Log;

public class LocalesExecutor extends SinglePageExecutor<Locales> implements
		IExecutor {

	private final static String TAG = LocalesExecutor.class.getSimpleName();
	private String mEtag;
	private boolean mContentIsEqual;

	public LocalesExecutor(Context context, Bundle bundle) {
		super(context, bundle, Locales.class);
	}

	@Override
	public void prepareContent() {
		mEtag = LastUpdateContent.queryEtag(getResolver(), LastUpdateContent.MODULE_LOCALE);
	}

	@Override
	public void execute() throws SyncServiceException {
		final LocalesRequestBuilder requestBuilder = new LocalesRequestBuilder(
				getServer(), getApiKey(), AcceptType.JSON);

		clearTempStore();
		setEtag( mEtag );
		retrieveSinglePage(requestBuilder);
		if ( mEtag != null && mEtag.equals( getEtag()) && getTempStore().isEmpty())
			mContentIsEqual = true;

		LastUpdateContent.storeEtag(getResolver(), LastUpdateContent.MODULE_LOCALE, getEtag());
		Log.d(TAG, "etag = " + getEtag());
	}

	@Override
	public void prepareDatabase() throws SyncServiceException {
		if ( mContentIsEqual ) {
		    Log.i( TAG, "content is equal according to etag - doing nothing" );
			return;
		}

		getResolver().delete(LocalesContent.CONTENT_URI, null, null);
		DataOperationsLocales dol = new DataOperationsLocales(getResolver());
		dol.insert(getTempStore());
		clearTempStore();
	}


}
