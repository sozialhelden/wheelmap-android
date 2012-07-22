package org.wheelmap.android.model;

import java.util.List;

import wheelmap.org.domain.BaseDomain;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import de.akquinet.android.androlog.Log;

public abstract class DataOperations<T extends BaseDomain, U> {
	private ContentResolver mResolver;

	protected DataOperations(ContentResolver resolver) {
		mResolver = resolver;
	}

	protected String getTag() {
		return this.getClass().getSimpleName();
	}

	protected abstract Uri getUri();

	public void insert(List<T> items) {
		long insertStart = System.currentTimeMillis();
		for (T item : items) {
			Log.d(getTag(), "inserting page " + item.getMeta().getPage());
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
