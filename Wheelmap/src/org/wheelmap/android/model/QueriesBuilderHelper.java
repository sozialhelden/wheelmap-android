package org.wheelmap.android.model;

import org.wheelmap.android.model.Support.CategoriesContent;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class QueriesBuilderHelper {
	
	static public String categoriesFilter(Context context) {
		// categories id

		// Run query
		Uri uri = Support.CategoriesContent.CONTENT_URI;
		Cursor cursor = context.getContentResolver().query(uri, null, null, null,null);

		StringBuilder categories = new StringBuilder("");

		int selectedCount = 0;
		if (cursor.moveToFirst()) {
			do { 
				int id = CategoriesContent.getCategoryId(cursor);				
				if (CategoriesContent.getSelected(cursor)) {
					selectedCount++;
					if (categories.length() > 0)
						categories.append(" OR category_id=");
					else
						categories.append(" category_id=");
					categories.append(new Integer(id).toString());

				}


			} while(cursor.moveToNext());
		}
		if (selectedCount == 0) {
			if (cursor.moveToFirst()) {
				do { 
					int id = CategoriesContent.getCategoryId(cursor);				
					if (categories.length() > 0)
						categories.append(" AND NOT category_id=");
					else
						categories.append(" NOT category_id=");

					categories.append(new Integer(id).toString());

				} while(cursor.moveToNext());
			}

		}

		// wheelchair state filter
	
	
		Log.d("QueriesBuilderHelper", categories.toString());

		// 

		return categories.toString();

	}

}
