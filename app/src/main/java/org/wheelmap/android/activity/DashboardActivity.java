package org.wheelmap.android.activity;

import org.wheelmap.android.activity.profile.ProfileActivity;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Request;
import org.wheelmap.android.model.Support;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.modules.UserCredentials;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.PressSelector;
import org.wheelmap.android.utils.UtilsMisc;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by tim on 07.02.14.
 */
public class DashboardActivity extends AppCompatActivity {

    private UserCredentials mCredentials;
    private String address = null;
    WheelmapApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCredentials = new UserCredentials(getApplicationContext());

        setContentView(R.layout.activity_dashboard);

        app = (WheelmapApp)this.getApplication();
        String uri = null;

        double lat = 0;
        double lon = 0;

        try{
            uri = app.getUriString();
        }catch (Exception ex){
            // noop
        }

        try{
            address = app.getAddressString();
        }catch(Exception ex){
            // noop
        }

        try{
            lon = app.getGeoLon();
            lat = app.getGeoLat();

        }catch(Exception ex){
            // noop
        }

        if(uri != null){
            openMap();

        }

        if(lat != 0){
            openMap();
        }

        if(lon != 0){
            openMap();
        }

        View btn_in_der_naehe = findViewById(R.id.dashboard_btn_in_der_naehe);
        btn_in_der_naehe.setOnTouchListener(new PressSelector());
        btn_in_der_naehe.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                openInDerNaehe();
            }
        });

        View btn_karte = findViewById(R.id.dashboard_btn_karte);
        btn_karte.setOnTouchListener(new PressSelector());
        btn_karte.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                openMap();
            }
        });
        View btn_kategorien = findViewById(R.id.dashboard_btn_kategorien);
        btn_kategorien.setOnTouchListener(new PressSelector());
        btn_kategorien.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                openKategorien();
            }
        });

        View btn_mithelfen = findViewById(R.id.dashboard_btn_mithelfen);
        btn_mithelfen.setOnTouchListener(new PressSelector());
        btn_mithelfen.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                openMithelfen();
            }
        });

        View news = findViewById(R.id.dashboard_btn_news);
        news.setOnTouchListener(new PressSelector());
        news.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                 openWebViewNews();
            }
        });

        findViewById(R.id.dashboard_info).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this,InfoActivity.class);
                startActivity(intent);
            }
        });

        TextView txt_orte = (TextView) findViewById(R.id.dashboard_text_orte);
        long count = WheelmapApp.getDefaultPrefs().getLong("ItemCountTotal",-1);

        if(count <= 0){
            txt_orte.setText("... " + getString(R.string.dashboard_locations));
        }else{
            txt_orte.setText(count + " "+ getString(R.string.dashboard_locations));
        }


        findViewById(R.id.dashboard_login).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                account();
            }
        });

        findViewById(R.id.dashboard_search).setOnTouchListener(new PressSelector());

        findViewById(R.id.dashboard_search).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });

        EditText search = (EditText) findViewById(R.id.dashboard_search_edit);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener(){

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){

                    performSearch();
                    return true;
                }
                return false;
            }
        });

        if(address != null){
            //((EditText)findViewById(R.id.dashboard_search_edit)).requestFocus();
            ((EditText)findViewById(R.id.dashboard_search_edit)).setText(address);
            app.setAddressString(null);
        }

        search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(address != null){
                    ((EditText)v.findViewById(R.id.dashboard_search_edit)).setText(address);
                    app.setAddressString(null);
                }else
                ((EditText)v.findViewById(R.id.dashboard_search_edit)).setHint("");
            }
        });

        WheelmapApp.checkForUpdates(this);

        boolean loggedIn = mCredentials.isLoggedIn();
        onActivityResult(Request.REQUEST_CODE_LOGIN,loggedIn?RESULT_OK:RESULT_CANCELED,null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WheelmapApp.checkForCrashes(this);
    }

    private int getPoiCount(){
        //doesnt work correctly
        Uri uri = Wheelmap.POIs.CONTENT_URI_ALL;
        Cursor c = getContentResolver().query(uri,null,null,null,null);
        int count = c.getCount();
        c.close();
        return count;
    }

    private void performSearch(){
        EditText search = (EditText) findViewById(R.id.dashboard_search_edit);

        Intent intent;

        WheelmapApp app = (WheelmapApp) this.getApplicationContext();
        app.setSaved(true);

        if (UtilsMisc.isTablet(getApplicationContext())) {
            intent = new Intent(getApplicationContext(),
                    MainMultiPaneActivity.class);
        } else {
            intent = new Intent(getApplicationContext(),
                    MainSinglePaneActivity.class);
        }
        intent.putExtra(Extra.SELECTED_TAB,0);
        intent.putExtra(SearchManager.QUERY,search.getText().toString());
        startActivity(intent);
    }

    private void openInDerNaehe(){
        Intent intent;

        if (UtilsMisc.isTablet(getApplicationContext())) {
            intent = new Intent(getApplicationContext(),
                    MainMultiPaneActivity.class);
        } else {
            intent = new Intent(getApplicationContext(),
                    MainSinglePaneActivity.class);
        }
        intent.putExtra(Extra.SELECTED_TAB,0);
        startActivity(intent);
        resetKategorieFilter();
    }

    private void openMap(){
        Intent intent;

        if (UtilsMisc.isTablet(getApplicationContext())) {
            intent = new Intent(getApplicationContext(),
                    MainMultiPaneActivity.class);
        } else {
            intent = new Intent(getApplicationContext(),
                    MainSinglePaneActivity.class);
        }
        intent.putExtra(Extra.SELECTED_TAB,1);
        startActivity(intent);
        resetKategorieFilter();
    }

    public void openWebViewNews(){
        startActivity(new Intent(this.getApplicationContext(),WebViewNewsActivity.class));
    }

    public void openMithelfen(){
        resetKategorieFilter();
        Intent intent;

        if (UtilsMisc.isTablet(getApplicationContext())) {
            intent = new Intent(getApplicationContext(),
                    MainMultiPaneActivity.class);
        } else {
            intent = new Intent(getApplicationContext(),
                    MainSinglePaneActivity.class);
        }
        intent.putExtra(Extra.SELECTED_TAB,0);
        intent.putExtra(Extra.MAP_MODE_ENGAGE, true);
        startActivity(intent);
    }

    public void openKategorien(){
        Intent intent = new Intent(this,ChooseCategoryActivity.class);
        intent.putExtra(Extra.SELECTED_TAB,2);
        startActivity(intent);
    }

    public void resetKategorieFilter(){
        Uri mUri = Support.CategoriesContent.CONTENT_URI;
        Cursor c = getContentResolver().query(mUri,
                Support.CategoriesContent.PROJECTION, null, null,
                Support.CategoriesContent.DEFAULT_SORT_ORDER);

        for(int i=0;i<c.getCount();i++){
            c.moveToPosition(i);
            int catId = Support.CategoriesContent.getCategoryId(c);

            ContentResolver resolver = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(Support.CategoriesContent.SELECTED,
                    Support.CategoriesContent.SELECTED_YES);

            String whereClause = "( " + Support.CategoriesContent.CATEGORY_ID
                    + " = ?)";
            String[] whereValues = new String[]{Integer.toString(catId)};
            resolver.update(mUri, values, whereClause, whereValues);
        }
        c.close();
    }

    private void account(){
        Intent intent = new Intent(this,ProfileActivity.class);
        startActivityForResult(intent, Request.REQUEST_CODE_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if(requestCode == Request.REQUEST_CODE_LOGIN){
             if(resultCode == Activity.RESULT_OK){
                 ImageView image = (ImageView) findViewById(R.id.dashboard_login);
                 image.setImageResource(R.drawable.start_icon_logged_in);
             }else{
                 ImageView image = (ImageView) findViewById(R.id.dashboard_login);
                 image.setImageResource(R.drawable.start_icon_login);
             }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
