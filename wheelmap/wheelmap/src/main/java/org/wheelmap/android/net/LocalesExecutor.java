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

import org.wheelmap.android.model.DataOperationsLocales;
import org.wheelmap.android.model.Support.LocalesContent;
import org.wheelmap.android.service.SyncServiceException;

import wheelmap.org.domain.locale.Locales;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.LocalesRequestBuilder;
import android.content.Context;
import android.os.Bundle;

public class LocalesExecutor extends SinglePageExecutor<Locales> implements
		IExecutor {

	public LocalesExecutor(Context context, Bundle bundle) {
		super(context, bundle, Locales.class);
	}

	@Override
	public void prepareContent() {
		getResolver().delete(LocalesContent.CONTENT_URI, null, null);
	}

	@Override
	public void execute() throws SyncServiceException {
		final LocalesRequestBuilder requestBuilder = new LocalesRequestBuilder(
				SERVER, getApiKey(), AcceptType.JSON);

		clearTempStore();
		retrieveSinglePage(requestBuilder);
	}

	@Override
	public void prepareDatabase() throws SyncServiceException {
		DataOperationsLocales dol = new DataOperationsLocales(getResolver());
		dol.insert(getTempStore());
		clearTempStore();
	}

}
