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

import org.wheelmap.android.mapping.locale.Locales;
import org.wheelmap.android.model.Support.LocalesContent;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import java.util.Map;
import java.util.Set;

public class DataOperationsLocales extends DataOperations<Locales, DataOperationsLocales.Locale> {

    private String[] mKeys;

    private Locale locale = new Locale();

    public DataOperationsLocales(ContentResolver resolver) {
        super(resolver);
    }

    @Override
    protected Locale getItem(Locales item, int i) {
        locale.id = mKeys[i];
        locale.name = item.getLocales().get(mKeys[i]);
        return locale;
    }

    @Override
    protected void bulkInsert(Locales item) {
        mKeys = getKeys(item.getLocales());
        super.bulkInsert(item);
    }

    private String[] getKeys(Map<String, String> map) {
        Set<String> keys = map.keySet();
        String[] val = new String[keys.size()];
        keys.toArray(val);
        return val;
    }

    @Override
    public void copyToValues(Locale item, ContentValues values) {
        values.clear();
        values.put(LocalesContent.LOCALE_ID, item.id);
        values.put(LocalesContent.LOCALIZED_NAME, item.name);
    }

    @Override
    public Uri getUri() {
        return LocalesContent.CONTENT_URI;
    }

    static class Locale {

        String id;

        String name;
    }
}
