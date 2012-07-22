package org.wheelmap.android.model;

import java.util.Map;
import java.util.Set;

import org.wheelmap.android.model.Support.LocalesContent;

import wheelmap.org.domain.locale.Locales;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

class Locale {
	String id;
	String name;
}

public class DataOperationsLocales extends DataOperations<Locales, Locale> {
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

}
