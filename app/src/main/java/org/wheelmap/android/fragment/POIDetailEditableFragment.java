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
package org.wheelmap.android.fragment;

import org.wheelmap.android.activity.LoginActivity;
import org.wheelmap.android.activity.WheelchairStateActivity;
import org.wheelmap.android.activity.WrapperActivity;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.fragment.ErrorDialogFragment.OnErrorDialogListener;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.manager.SupportManager.Category;
import org.wheelmap.android.manager.SupportManager.NodeType;
import org.wheelmap.android.manager.SupportManager.WheelchairAttributes;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.PrepareDatabaseHelper;
import org.wheelmap.android.model.Request;
import org.wheelmap.android.model.WheelchairState;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.modules.ICredentials;
import org.wheelmap.android.modules.UserCredentials;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.RestService;
import org.wheelmap.android.service.RestServiceException;
import org.wheelmap.android.service.RestServiceHelper;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.DetachableResultReceiver.Receiver;
import org.wheelmap.android.utils.UtilsMisc;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import de.akquinet.android.androlog.Log;

public class POIDetailEditableFragment extends Fragment implements
        OnErrorDialogListener, Receiver, OnClickListener, LoaderCallbacks<Cursor> {

    public static final int REQUEST_CODE_LOGIN = 1421;

    public final static String TAG = POIDetailEditableFragment.class
            .getSimpleName();

    private final static int LOADER_CONTENT = 0;

    private final static int LOADER_TMP = 1;

    private static final int DIALOG_ID_NEWPOI = 1;

    private static final int DIALOG_ID_NETWORK_ERROR = 2;

    private final static int FOCUS_TO_NOTHING = 0;
    private final static int FOCUS_TO_ADRESS = 1;
    private final static int FOCUS_TO_COMMENT = 2;

    private ICredentials mCredentials;

    private EditText nameText;

    private TextView nodetypeText;

    private EditText commentText;

    private EditText streetText;

    private EditText housenumText;

    private EditText postcodeText;

    private EditText cityText;

    private EditText websiteText;

    private EditText phoneText;

    private TextView state_text;

    private TextView geolocation_text;

    private RelativeLayout edit_state_container;

    private RelativeLayout edit_nodetype_container;

    private RelativeLayout edit_geolocation_container;

    private Long poiID = Extra.ID_UNKNOWN;

    private String wmID;

    private WheelchairState mWheelchairState;

    private double mLatitude;

    private double mLongitude;

    private int mNodeType;

    private Map<WheelchairState, WheelchairAttributes> mWSAttributes;

    private OnPOIDetailEditableListener mListener;

    private boolean mTemporaryStored;

    private DetachableResultReceiver mReceiver;

    private int focus;

    public interface OnPOIDetailEditableListener {

        public void onEditSave(boolean quit);

        public void onEditWheelchairState(WheelchairState state);

        public void onEditGeolocation(double latitude, double longitude);

        public void onEditNodetype(int nodetype);

        public void requestExternalEditedState(
                POIDetailEditableFragment fragment);

        public void onStoring(boolean isRefreshing);

    }

    public static POIDetailEditableFragment newInstance(long poiId, int focus) {
        Bundle b = new Bundle();
        b.putLong(Extra.POI_ID, poiId);
        b.putInt("Focus",focus);


        POIDetailEditableFragment fragment = new POIDetailEditableFragment();
        fragment.setArguments(b);

        return fragment;
    }

    public POIDetailEditableFragment() {
        Log.d(TAG, "constructor called ");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnPOIDetailEditableListener) {
            mListener = (OnPOIDetailEditableListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCredentials = new UserCredentials(getActivity().getApplicationContext());

        Log.d(TAG, "onCreate");
        setHasOptionsMenu(true);
        mWSAttributes = SupportManager.wsAttributes;
        poiID = getArguments().getLong(Extra.POI_ID);
        focus = getArguments().getInt("Focus");
        mReceiver = new DetachableResultReceiver(new Handler());
        mReceiver.setReceiver(this);

    }

    public void initViews(View parent){
        nameText = (EditText)parent.findViewById(R.id.name);
        nodetypeText = (TextView) parent.findViewById(R.id.nodetype);
        commentText = (EditText) parent.findViewById(R.id.comment);
        streetText = (EditText) parent.findViewById(R.id.street);
        housenumText = (EditText) parent.findViewById(R.id.housenum);
        postcodeText = (EditText) parent.findViewById(R.id.postcode);
        cityText = (EditText) parent.findViewById(R.id.city);
        websiteText = (EditText) parent.findViewById(R.id.website);
        phoneText = (EditText) parent.findViewById(R.id.phone);
        state_text = (TextView) parent.findViewById(R.id.state_text);
        geolocation_text = (TextView) parent.findViewById(R.id.geolocation);

        edit_state_container = (RelativeLayout) parent.findViewById(R.id.wheelchair_state_layout);

        edit_nodetype_container = (RelativeLayout) parent.findViewById(R.id.edit_nodetype);
        edit_geolocation_container = (RelativeLayout) parent.findViewById(R.id.edit_geolocation);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail_editable, container, false);

        v.findViewById(R.id.detail_save).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
                //quit(true);
                return;
            }
        });
        /*v.findViewById(R.id.no).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });    */
        initViews(v);

        if(focus == FOCUS_TO_NOTHING){
           //noop
        }else if(focus == FOCUS_TO_ADRESS){
            streetText.requestFocus();
        }else if(focus == FOCUS_TO_COMMENT){
            commentText.requestFocus();
        }

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edit_state_container.setOnClickListener(this);

        edit_nodetype_container.setOnClickListener(this);
        edit_geolocation_container.setOnClickListener(this);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
        retrieve(savedInstanceState);
        if (!mCredentials.isLoggedIn()) {

            Intent intent = new Intent(getActivity(),LoginActivity.class);
            startActivityForResult(intent, REQUEST_CODE_LOGIN);
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_LOGIN){
            if(resultCode != Activity.RESULT_OK){
                  getActivity().onBackPressed();
            }
        }

        if (requestCode == Request.SELECT_WHEELCHAIRSTATE) {
            if (resultCode == Activity.RESULT_OK) {
                // newly selected wheelchair state as action data
                if (data != null) {
                    WheelchairState state = WheelchairState
                            .valueOf(data.getIntExtra(Extra.WHEELCHAIR_STATE,
                                    Extra.UNKNOWN));
                    setWheelchairState(state);
                    if(mListener != null){
                        mListener.onEditWheelchairState(state);
                    }

                }
            }
        }

        if(requestCode == Request.SELECT_GEOLOCATION && resultCode == Activity.RESULT_OK && data != null){
            double latitude = data.getDoubleExtra(Extra.LATITUDE,mLatitude);
            double longitude = data.getDoubleExtra(Extra.LONGITUDE,mLongitude);
            setGeolocation(latitude,longitude);
            if(mListener != null){
               mListener.onEditGeolocation(latitude,longitude);
            }
        }

        if(requestCode == Request.SELECT_NODETYPE && resultCode == Activity.RESULT_OK && data != null){
            int nodeType = data.getIntExtra(Extra.NODETYPE,mNodeType);
            setNodetype(nodeType);
            if(mListener != null){
                mListener.onEditNodetype(nodeType);
            }
        }
    }
    private void retrieve(Bundle bundle) {
        boolean loadTempStore = mTemporaryStored
                || (bundle != null && bundle
                .containsKey(Extra.TEMPORARY_STORED));
        int loaderId;
        if (loadTempStore) {
            loaderId = LOADER_TMP;
        } else {
            loaderId = LOADER_CONTENT;
        }

        Log.d(TAG, "retrieve: init loader id = " + loaderId);
        getLoaderManager().initLoader(loaderId, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        /*try{
            storeTemporary();
        }catch(Exception e){}   */
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putBoolean(Extra.TEMPORARY_STORED, true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ab_detaileditable_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onClick(View v) {
        closeKeyboard();

        int id = v.getId();
        switch (id) {
            case R.id.wheelchair_state_layout: {
                Intent intent = new Intent(getActivity(), WheelchairStateActivity.class);
                intent.putExtra(Extra.WHEELCHAIR_STATE, mWheelchairState.getId());
                startActivityForResult(intent, Request.SELECT_WHEELCHAIRSTATE);
                break;
            }
            case R.id.edit_geolocation: {
                Intent intent = new Intent(getActivity(), WrapperActivity.class);
                intent.putExtra(WrapperActivity.EXTRA_FRAGMENT_CLASS_NAME,EditPositionFragment.class.getName());
                intent.putExtra(Extra.LATITUDE,mLatitude);
                intent.putExtra(Extra.LONGITUDE,mLongitude);
                startActivityForResult(intent, Request.SELECT_GEOLOCATION);
                break;
            }
            case R.id.edit_nodetype: {
                Intent intent = new Intent(getActivity(), WrapperActivity.class);
                intent.putExtra(WrapperActivity.EXTRA_FRAGMENT_CLASS_NAME,NodetypeSelectFragment.class.getName());
                intent.putExtra(Extra.NODETYPE, mNodeType);
                startActivityForResult(intent, Request.SELECT_NODETYPE);
                break;
            }

            default:
                // do nothing
        }
    }

    public void save() {
        ContentValues values = retrieveContentValues();
        if(values == null){
            return;
        }
        if (!values.containsKey(POIs.NODETYPE_ID)) {
            showErrorMessage(getString(R.string.error_category_missing_title),
                    getString(R.string.error_category_missing_message),
                    Extra.UNKNOWN);
            return;
        } else if (mWheelchairState == WheelchairState.UNKNOWN) {
            showErrorMessage(
                    getString(R.string.error_wheelchairstate_missing_title),
                    getString(R.string.error_wheelchairstate_missing_message),
                    Extra.UNKNOWN);
            return;
        }

        if(values.containsKey(POIs.WEBSITE)){
            String website = values.getAsString(POIs.WEBSITE);
                website = website.toLowerCase(Locale.US);
                if(!website.startsWith("http://") && !website.startsWith(
                        "https://")){
                    website = "http://"+website;
                }

            if(!android.util.Patterns.WEB_URL.matcher(website).matches()){
                showErrorMessage(null,getString(android.R.string.httpErrorBadUrl),-1);
                return;
            }

                values.put(POIs.WEBSITE,website);
        }
        values.put(POIs.DIRTY, POIs.DIRTY_ALL);
        PrepareDatabaseHelper.editCopy(getActivity().getContentResolver(),
                poiID, values);
        RestServiceHelper.executeUpdateServer(getActivity(), mReceiver);


    }

    private void quit(boolean quit) {
        if (mListener != null) {
            mListener.onEditSave(quit);
        }
    }

    @Override
    public void onErrorDialogClose(int id) {
        Log.d(TAG, "onErrorDialogClose");
        if (id == DIALOG_ID_NEWPOI) {
            quit(false);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arguments) {
        Log.d(TAG, "onCreateLoader: id = " + id);
        Uri uri = null;
        if (id == LOADER_CONTENT) {
            uri = ContentUris.withAppendedId(POIs.CONTENT_URI_COPY, poiID);
        } else {
            uri = POIs.CONTENT_URI_TMP;
        }

        Log.d(TAG, "onCreateLoader: uri = " + uri);
        return new CursorLoader(getActivity(), uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished loader id = " + loader.getId());
        load(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {

    }

    private void load(Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        UtilsMisc.dumpCursorToLog(TAG, cursor);
        cursor.moveToFirst();

        WheelchairState state = POIHelper.getWheelchair(cursor);
        String name = POIHelper.getName(cursor);
        String comment = POIHelper.getComment(cursor);
        double latitude = POIHelper.getLatitude(cursor);
        double longitude = POIHelper.getLongitude(cursor);
        int nodeType = POIHelper.getNodeTypeId(cursor);

        setGeolocation(latitude, longitude);
        setNodetype(nodeType);
        setWheelchairState(state);
        nameText.setText(name);
        commentText.setText(comment);

        streetText.setText(POIHelper.getStreet(cursor));
        housenumText.setText(POIHelper.getHouseNumber(cursor));
        postcodeText.setText(POIHelper.getPostcode(cursor));
        cityText.setText(POIHelper.getCity(cursor));

        websiteText.setText(POIHelper.getWebsite(cursor));
        phoneText.setText(POIHelper.getPhone(cursor));


        TextWatcher textWatcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Toast.makeText(yourActivity.this,"changed",0).show();
                changedEdit(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        ArrayList<EditText> firstList = new ArrayList<EditText>();
        firstList.add(nameText);
        firstList.add(commentText);
        firstList.add(streetText);
        firstList.add(housenumText);
        firstList.add(cityText);
        firstList.add(websiteText);
        firstList.add(phoneText);

        for(int i=0;i<firstList.size();i++)
        {
            firstList.get(i).addTextChangedListener(textWatcher);
        }

        wmID = POIHelper.getWMId(cursor);
        if (TextUtils.isEmpty(wmID)) {
            showGeolocationEditor(true);
        }

        retrieveExternalEditedState();
    }

    private void changedEdit(boolean changed){
        WheelmapApp app = (WheelmapApp) this.getActivity().getApplicationContext();
        app.setChangedText(changed);
    }

    private void showGeolocationEditor(boolean show) {
        if (show) {
            //edit_geolocation_container.setVisibility(View.VISIBLE);
        } else {
            //edit_geolocation_container.setVisibility(View.GONE);
        }
    }

    private void retrieveExternalEditedState() {
        Log.d(TAG, "retrieveExternalEditedState");
        if (mListener != null) {
            mListener.requestExternalEditedState(this);
        }
    }

    private ContentValues retrieveContentValues() {
        ContentValues values = new ContentValues();

        try{

            if(nameText == null){
                return null;
            }

            String name = nameText.getText().toString();
            if (!TextUtils.isEmpty(name)) {
                values.put(POIs.NAME, name);
            }

            SupportManager sm = WheelmapApp.getSupportManager();

            if (mNodeType != SupportManager.UNKNOWN_TYPE) {
                NodeType nodeType = sm.lookupNodeType(mNodeType);
                Category category = sm.lookupCategory(nodeType.categoryId);

                values.put(POIs.CATEGORY_ID, nodeType.categoryId);
                values.put(POIs.CATEGORY_IDENTIFIER, category.identifier);
                values.put(POIs.NODETYPE_IDENTIFIER, nodeType.identifier);
                values.put(POIs.NODETYPE_ID, mNodeType);

            }

            values.put(POIs.LATITUDE, mLatitude);
            values.put(POIs.LONGITUDE, mLongitude);

            values.put(POIs.WHEELCHAIR, mWheelchairState.getId());
            String description = commentText.getText().toString();
            if (!TextUtils.isEmpty(description)) {
                values.put(POIs.DESCRIPTION, description);
            }

            String street = streetText.getText().toString();
            if (!TextUtils.isEmpty(street)) {
                values.put(POIs.STREET, street);
            }
            String housenum = housenumText.getText().toString();
            if (!TextUtils.isEmpty(housenum)) {
                values.put(POIs.HOUSE_NUM, housenum);
            }
            String postcode = postcodeText.getText().toString();
            if (!TextUtils.isEmpty(postcode)) {
                values.put(POIs.POSTCODE, postcode);
            }
            String city = cityText.getText().toString();
            if (!TextUtils.isEmpty(city)) {
                values.put(POIs.CITY, city);
            }

            String website = websiteText.getText().toString();
            if (!TextUtils.isEmpty(website)) {
                values.put(POIs.WEBSITE, website);
            }
            String phone = phoneText.getText().toString();
            if (!TextUtils.isEmpty(phone)) {
                values.put(POIs.PHONE, phone);
            }

        }catch(NullPointerException npex){

            Log.d("Tag:PoiDetailEditableFragment", "NullPointException occurred");

            Toast.makeText(this.getActivity().getApplicationContext(), getResources().getString(R.string.error_internal_error), Toast.LENGTH_LONG).show();

            //this.startActivity(new Intent(this.getActivity(), DashboardActivity.class));

           return null;

        }
        return values;
    }

    public void setWheelchairState(WheelchairState newState) {
        if (newState == null) {
            return;
        }

        mWheelchairState = newState;

        int stateColor = getResources().getColor(
                mWSAttributes.get(newState).colorId);

        try{
        if(mWheelchairState.getId() == WheelchairState.UNKNOWN.getId())
            state_text.setBackgroundResource(R.drawable.detail_button_grey);
        else if(mWheelchairState.getId() == WheelchairState.YES.getId())
            state_text.setBackgroundResource(R.drawable.detail_button_green);
        else if(mWheelchairState.getId() == WheelchairState.LIMITED.getId())
            state_text.setBackgroundResource(R.drawable.detail_button_orange);
        else if(mWheelchairState.getId() == WheelchairState.NO.getId())
            state_text.setBackgroundResource(R.drawable.detail_button_red);
        else if(mWheelchairState.getId() == WheelchairState.NO_PREFERENCE.getId())
            state_text.setBackgroundResource(R.drawable.detail_button_grey);
        else
            state_text.setBackgroundResource(R.drawable.detail_button_grey);
        }catch(OutOfMemoryError e){
            System.gc();
        }



        //title_container.setBackgroundColor(stateColor);
        //stateIcon.setImageResource(mWSAttributes.get(newState).drawableId);
        //stateText.setTextColor(stateColor);

        state_text.setText(mWSAttributes.get(newState).titleStringId);
    }

    public void setGeolocation(double latitude, double longitude) {
        if (latitude == Extra.UNKNOWN || longitude == Extra.UNKNOWN) {
            return;
        }

        mLatitude = latitude;
        mLongitude = longitude;

        String positionText = String.format("%s: (%.6f:%.6f)", getResources()
                .getString(R.string.position_geopoint), mLatitude, mLongitude);
        geolocation_text.setText(positionText);
    }

    public void setNodetype(int nodetype) {
        if (nodetype == SupportManager.UNKNOWN_TYPE) {
            return;
        }

        mNodeType = nodetype;
        SupportManager manager = WheelmapApp.getSupportManager();
        NodeType nodeType = manager.lookupNodeType(nodetype);
        nodetypeText.setText(nodeType.localizedName);
    }

    public void showErrorMessage(String title, String message, int id) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance(
                title, message, id);
        if (errorDialog == null) {
            return;
        }
        errorDialog.setOnErrorDialogListener(this);
        errorDialog.show(fm, ErrorDialogFragment.TAG);
    }

    public void showNetworkErrorMessage(RestServiceException e, int id) {
        FragmentManager fm = getFragmentManager();
        ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance(e, id);
        if (errorDialog == null) {
            return;
        }
        errorDialog.setOnErrorDialogListener(this);
        errorDialog.show(fm, ErrorDialogFragment.TAG);
    }

    public void closeKeyboard() {
        View view = getView().findFocus();
        if (view == null || !(view instanceof EditText)) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }

    private void storeTemporary() {
        ContentValues values = retrieveContentValues();
        if(values == null){
           return;
        }
        if (!TextUtils.isEmpty(wmID)) {
            values.put(POIs.WM_ID, wmID);
        }

        long id = PrepareDatabaseHelper.storeTemporary(getActivity()
                .getContentResolver(), values);
        Log.d(TAG, "storeTemporary wmId = " + wmID + " id = " + id);
        getLoaderManager().destroyLoader(LOADER_CONTENT);
        mTemporaryStored = true;
    }

    private void storingStatus(boolean storing) {
        if (mListener != null) {
            mListener.onStoring(storing);
        }
    }

    private void showNewPoiOrQuit() {
        if (TextUtils.isEmpty(wmID)) {
            showErrorMessage(getString(R.string.error_newpoi_title),
                    getString(R.string.error_newpoi_message), DIALOG_ID_NEWPOI);
        } else {
            quit(false);
        }
    }

    private void showError(){
        showErrorMessage(getString(R.string.error_title_occurred),
                getString(R.string.error_network_unknown_failure), DIALOG_ID_NETWORK_ERROR);
    }

    @Override
    public void onResume(){
        super.onResume();
        mCredentials = new UserCredentials(getActivity().getApplicationContext());
    }

    /**
     * {@inheritDoc}
     */
    public void onReceiveResult(int resultCode, Bundle resultData) {
        Log.d(TAG, "onReceiveResult resultCode = " + resultCode);
        switch (resultCode) {
            case RestService.STATUS_RUNNING: {
                storingStatus(true);
                break;
            }
            case RestService.STATUS_FINISHED: {
                storingStatus(false);
                showNewPoiOrQuit();
                break;
            }
            case RestService.STATUS_ERROR: {
                storingStatus(false);
                showError();
                break;
            }
        }
    }
}
