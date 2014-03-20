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
package org.wheelmap.android.model;

import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.mapping.BaseDomain;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import java.util.List;

import de.akquinet.android.androlog.Log;

public abstract class DataOperations<T extends BaseDomain, U> {

    private ContentResolver mResolver;

    protected DataOperations(ContentResolver resolver) {
        mResolver = resolver;
    }

    protected String getTag() {
        return this.getClass().getSimpleName();
    }

    public ContentResolver getResolver() {
        return mResolver;
    }

    protected abstract Uri getUri();

    public void insert(List<T> items) {
        long insertStart = System.currentTimeMillis();
        for (T item : items) {
            Log.d(getTag(), "inserting page " + item.getMeta().getPage());
            WheelmapApp.getDefaultPrefs().edit().putLong("ItemCountTotal",item.getMeta().getItemCountTotal().longValue()).commit();
            bulkInsert(item);
        }
        long insertEnd = System.currentTimeMillis();
        Log.i(getTag(), "insertTime = " + (insertEnd - insertStart) / 1000f);

    }

    protected abstract U getItem(T items, int i);

    public abstract void copyToValues(U item, ContentValues values);

    protected void bulkInsert(T item) {



        int size = item.getMeta().getItemCount().intValue();
        ContentValues[] contentValuesArray = new ContentValues[size];
        int i;
        for (i = 0; i < size; i++) {
            ContentValues values = new ContentValues();
            copyToValues(getItem(item, i), values);
            contentValuesArray[i] = values;
        }

        int count = mResolver.bulkInsert(getUri(), contentValuesArray);
        Log.d(getTag(), "Inserted records count = " + count);
    }

}
