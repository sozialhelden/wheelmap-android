package org.wheelmap.android.activity;

<<<<<<< HEAD
import org.wheelmap.android.activity.base.BaseActivity;
=======
import org.wheelmap.android.analytics.AnalyticsTrackingManager;
>>>>>>> 1d57954aef8d4309f867c6ce784e3fa52dedb76f
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Support;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.Constants;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * Created by tim on 07.02.14.
 */
public class ChooseCategoryActivity extends BaseActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    private Uri mUri = Support.CategoriesContent.CONTENT_URI;

    private ListView listView;

    CursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listView = new ListView(this);
        listView.setId(android.R.id.list);
        listView.setOnItemClickListener(this);
        setContentView(listView);

        if(getSupportActionBar() != null){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(R.string.dashboard_button_title_categories);
        }

        getSupportLoaderManager().initLoader(0,null,this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        AnalyticsTrackingManager.trackScreen(AnalyticsTrackingManager.TrackableScreensName.CATEGORYSCREEN);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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

        intent.putExtra(Extra.SELECTED_TAB, Constants.TabContent.LOCATION_BASED_LIST);
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
        adapter = new SimpleCursorAdapter(this,R.layout.simple_list_item_category,cursor,new String[]{Support.CategoryColumns.LOCALIZED_NAME},new int[]{R.id.text_category});
        listView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
