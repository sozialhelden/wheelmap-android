package org.wheelmap.android.activity;

import com.actionbarsherlock.view.MenuItem;

import org.holoeverywhere.app.ListActivity;
import org.holoeverywhere.widget.ListView;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Support;
import org.wheelmap.android.online.R;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;

/**
 * Created by tim on 07.02.14.
 */
public class ChooseCategoryActivity extends ListActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mUri = Support.CategoriesContent.CONTENT_URI;

    CursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListView list = new ListView(this);
        list.setId(android.R.id.list);
        setContentView(list);


        setTitle(R.string.dashboard_button_title_categories);


        if(getSupportActionBar() != null){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getSupportLoaderManager().initLoader(0,null,this);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

       // SharedPreferences prefs =  WheelmapApp.getCategoryChoosedPrefs();
        Cursor c = adapter.getCursor();
        for(int i=0;i<c.getCount();i++){
            c.moveToPosition(i);
            int catId = Support.CategoriesContent.getCategoryId(c);

            ContentResolver resolver = getContentResolver();
            ContentValues values = new ContentValues();
            if (i != position) {
                values.put(Support.CategoriesContent.SELECTED,
                        Support.CategoriesContent.SELECTED_NO);
            } else {
                values.put(Support.CategoriesContent.SELECTED,
                        Support.CategoriesContent.SELECTED_YES);
            }
            String whereClause = "( " + Support.CategoriesContent.CATEGORY_ID
                    + " = ?)";
            String[] whereValues = new String[]{Integer.toString(catId)};
            resolver.update(mUri, values, whereClause, whereValues);

        }

        Intent intent = new Intent(getApplicationContext(),
                    MainSinglePaneActivity.class);

        intent.putExtra(Extra.SELECTED_TAB,0);
        intent.putExtra(Extra.QUERY_CHANGED,true);
        startActivity(intent);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new CursorLoader(this, mUri,
                Support.CategoriesContent.PROJECTION, null, null,
                Support.CategoriesContent.DEFAULT_SORT_ORDER);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursor,new String[]{Support.CategoryColumns.LOCALIZED_NAME},new int[]{android.R.id.text1});
        setListAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
