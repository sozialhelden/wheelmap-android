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

import de.akquinet.android.androlog.Log;
import org.wheelmap.android.model.DataOperationsCategories;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Support.LastUpdateContent;
import org.wheelmap.android.model.Support.CategoriesContent;
import org.wheelmap.android.service.SyncServiceException;

import wheelmap.org.Locale;
import wheelmap.org.domain.categories.Categories;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.CategoriesRequestBuilder;
import android.content.Context;
import android.os.Bundle;

public class CategoriesExecutor extends SinglePageExecutor<Categories> implements
		IExecutor {
	private final static String TAG = CategoriesExecutor.class.getSimpleName();
	private Locale mLocale;
	private String mEtag;
	private boolean mContentIsEqual;

	public CategoriesExecutor(Context context, Bundle bundle) {
		super(context, bundle, Categories.class);
	}

	@Override
	public void prepareContent() {
		String locale = getBundle().getString(Extra.LOCALE);
		if (locale != null && !locale.equals("de")) {
			mLocale = new Locale(locale);
		}

		mEtag = LastUpdateContent.queryEtag(getResolver(), LastUpdateContent.MODULE_CATEGORIES);
	}

	@Override
	public void execute() throws SyncServiceException {
		CategoriesRequestBuilder requestBuilder = new CategoriesRequestBuilder(
				getServer(), getApiKey(), AcceptType.JSON);
		if (mLocale != null)
			requestBuilder.locale(mLocale);

		clearTempStore();
		setEtag( mEtag );
		retrieveSinglePage(requestBuilder);
		if ( mEtag != null && mEtag.equals( getEtag()) && getTempStore().isEmpty())
			mContentIsEqual = true;

		LastUpdateContent.storeEtag(getResolver(), LastUpdateContent.MODULE_CATEGORIES, getEtag());
		Log.d(TAG, "etag = " + getEtag());
	}

	@Override
	public void prepareDatabase() throws SyncServiceException {
		if ( mContentIsEqual ) {
			Log.i( TAG, "content is equal according to etag - doing nothing" );
			return;
		}

		getResolver().delete(CategoriesContent.CONTENT_URI, null, null);
		DataOperationsCategories doc = new DataOperationsCategories(
				getResolver());
		doc.insert(getTempStore());
		clearTempStore();
	}

}
