package org.wheelmap.android.activity;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import org.holoeverywhere.widget.EditText;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.fragment.LoginDialogFragment;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.Support;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.PressSelector;
import org.wheelmap.android.utils.UtilsMisc;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

/**
 * Created by tim on 07.02.14.
 */
public class DashboardActivity extends
        org.holoeverywhere.app.Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);

        WheelmapApp app = (WheelmapApp)this.getApplication();
        String uri = null;
        try{
            uri = app.getUriString();
        }catch (Exception ex){
            // noop
        }

        if(uri != null){
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
                //FragmentManager fm = getSupportFragmentManager();
                //LoginDialogFragment loginDialog = new LoginDialogFragment();
                //loginDialog.show(fm);
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

        search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                ((EditText)v.findViewById(R.id.dashboard_search_edit)).setHint("");
            }
        });

        checkForUpdates();

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForCrashes();
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

    public void openMithelfen(){
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
        Intent intent = new Intent(getApplicationContext(),
                MainSinglePaneActivity.class);
        //startActivity(intent);
        intent = new Intent(this,ChooseCategoryActivity.class);
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

    private void checkForCrashes() {
        String appId = getString(R.string.hockeyapp_id);
        if (TextUtils.isEmpty(appId)) {
            return;
        }
        CrashManager.register(this, appId);
    }

    private void checkForUpdates() {
        String appId = getString(R.string.hockeyapp_id);
        if (TextUtils.isEmpty(appId)) {
            return;
        }
        UpdateManager.register(this, appId);
    }

    private void account(){
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }

}
