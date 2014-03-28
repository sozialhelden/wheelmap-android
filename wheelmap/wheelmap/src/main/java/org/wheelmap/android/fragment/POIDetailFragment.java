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




import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.app.ProgressDialog;
import org.wheelmap.android.adapter.Item;
import org.wheelmap.android.async.UploadPhotoTask;
import org.wheelmap.android.model.Request;
import org.wheelmap.android.service.RestService;
import org.wheelmap.android.service.RestServiceException;
import org.wheelmap.android.utils.DetachableResultReceiver.Receiver;
import org.json.JSONObject;
import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapController;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.wheelmap.android.adapter.HorizontalImageAdapter;
import org.wheelmap.android.adapter.HorizontalView;
import org.wheelmap.android.app.AppCapability;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.manager.SupportManager.NodeType;
import org.wheelmap.android.manager.SupportManager.WheelchairAttributes;
import org.wheelmap.android.mapping.node.Photo;
import org.wheelmap.android.mapping.node.Photos;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.WheelchairState;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.online.R;
import org.wheelmap.android.overlays.OnTapListener;
import org.wheelmap.android.overlays.SingleItemOverlay;
import org.wheelmap.android.service.RestServiceHelper;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.SmoothInterpolator;
import org.wheelmap.android.utils.UtilsMisc;
import org.wheelmap.android.utils.ViewTool;

import android.annotation.SuppressLint;

import android.content.ClipData;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Interpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.akquinet.android.androlog.Log;
import roboguice.inject.ContentViewListener;

public class POIDetailFragment extends Fragment implements
        OnClickListener, OnTapListener, LoaderCallbacks<Cursor>, Receiver {

    public final static String TAG = POIDetailFragment.class.getSimpleName();

    private final static int LOADER_CONTENT = 0;

    private static final Interpolator SMOOTH_INTERPOLATOR = new SmoothInterpolator();

    private final static long FADE_IN_ANIMATION_DURATION = 500;


    ImageView img_logo;
    private Intent pictureActionIntent = null;
    Bitmap bitmap;

    long wmID;


    //@Inject
    public ContentViewListener ignored;

    //@InjectView(R.id.title_container)
    private RelativeLayout title_container;

    //@InjectView(R.id.titlebar_title)
    private TextView nameText;

    //@InjectView(R.id.titlebar_subtitle)
    private TextView categoryText;

    //@InjectView(R.id.titlebar_icon)
    private ImageView nodetypeIcon;

    //@InjectView(R.id.nodetype)
    //private TextView nodetypeText;

    //@InjectView(R.id.phone)
    private TextView phoneText;

    //@InjectView(R.id.addr)
    private TextView addressText;

    private TextView addressTitle;

    private TextView photoTitle;
    private LinearLayout photoLayout;

    //@InjectView(R.id.comment)
    private TextView commentText;

    private TextView noCommentText;
    private TextView noAdressText;
    private TextView noPhotosText;

    private TextView commentTitle;

    private TextView webText;

    private LinearLayout titlebarBackbutton;

    private ImageButton buttonPhoto;
    private ImageButton buttonEdit;
    private ImageButton buttonRoute;
    private ImageButton buttonShare;

    private TextView nothing;

    //@InjectView(R.id.website)
    //private TextView websiteText;

    //@InjectView(R.id.state_icon)
    private ImageView stateIcon;

    //@InjectView(R.id.state_text)
    private TextView stateText;

    //@InjectView(R.id.wheelchair_state_layout)
    private ViewGroup stateLayout;

    private Button mMapButton;

    private ImageView mTestImage;

    private Map<WheelchairState, WheelchairAttributes> mWSAttributes;

    private WheelchairState mWheelchairState;

    public interface OnPOIDetailListener {

        void onEdit(long poiId);

        void onEditWheelchairState(WheelchairState wState);

        void onShowLargeMapAt(GeoPoint point);

        void dismissDetailView();
    }

    private OnPOIDetailListener mListener;

    private MapView mapView;

    private MapController mapController;

    private long poiId;

    private ContentValues poiValues;

    private final static int ACTION_PROVIDER_DIRECTIONS = 0;

    private final static int ACTION_PROVIDER_SHARE = 1;

    private ShareActionProvider mShareActionProvider;

    private ShareActionProvider mDirectionsActionProvider;

    private Menu currentMenu;

    private boolean mShowMenu;

    private static final int DIALOG_ALERT = 10;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private List listImages;
    private HorizontalImageAdapter imageAdapter;
    private HorizontalView listView;

    private AlertDialog dialog;
    private ProgressDialog progress;


    @SuppressLint("UseSparseArrays")
    private final static Map<Integer, Intent> intentSaved = new HashMap<Integer, Intent>();

    public static POIDetailFragment newInstance(long id) {
        if (id == Extra.ID_UNKNOWN) {
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putLong(Extra.POI_ID, id);
        bundle.putBoolean(Extra.SHOW_MAP, true);
        POIDetailFragment f = new POIDetailFragment();
        f.setArguments(bundle);
        return f;
    }

    public static POIDetailFragment newInstance() {
        POIDetailFragment f = new POIDetailFragment();
        Bundle bundle = new Bundle();
        f.setArguments(bundle);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnPOIDetailListener) {
            mListener = (OnPOIDetailListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        mWSAttributes = SupportManager.wsAttributes;
        poiId = getArguments().getLong(Extra.POI_ID, Extra.ID_UNKNOWN);


    }

    public void initViews(View v){
        title_container = (RelativeLayout)v.findViewById(R.id.title_container);
        nameText = (TextView)v.findViewById(R.id.titlebar_title);
        categoryText = (TextView)v.findViewById(R.id.titlebar_subtitle);
        nodetypeIcon = (ImageView)v.findViewById(R.id.titlebar_icon);
        addressTitle = (TextView)v.findViewById(R.id.addr_title);
        addressText = (TextView)v.findViewById(R.id.addr);
        commentTitle = (TextView)v.findViewById(R.id.comment_title);
        commentText = (TextView)v.findViewById(R.id.comment);
        //stateIcon = (ImageView)v.findViewById(R.id.state_icon);
        stateText = (TextView)v.findViewById(R.id.state_text);
        stateLayout = (ViewGroup)v.findViewById(R.id.wheelchair_state_layout);
        webText = (TextView)v.findViewById(R.id.web);
        phoneText = (TextView)v.findViewById(R.id.phone);
        titlebarBackbutton = (LinearLayout)v.findViewById(R.id.titlebar_backbutton);
        buttonPhoto = (ImageButton)v.findViewById(R.id.detail_foto);
        buttonEdit = (ImageButton)v.findViewById(R.id.detail_edit);
        buttonRoute = (ImageButton)v.findViewById(R.id.detail_route);
        buttonShare = (ImageButton)v.findViewById(R.id.detail_share);
        nothing = (TextView)v.findViewById(R.id.nothing);

        noCommentText = (TextView)v.findViewById(R.id.nocomment);
        noAdressText = (TextView)v.findViewById(R.id.noadress);
        noPhotosText = (TextView)v.findViewById(R.id.nophotos);

        photoTitle = (TextView)v.findViewById(R.id.photo_text);
        photoLayout = (LinearLayout)v.findViewById(R.id.photo_layout);

        listView = (HorizontalView)v.findViewById(R.id.gallery);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail, container, false);

        initViews(v);

        ScrollView scrollView = (ScrollView)v.findViewById(R.id.scrollView);
        scrollView.requestDisallowInterceptTouchEvent(true);

        mShowMenu = false;
        if (getArguments().containsKey(Extra.SHOW_MAP)) {
            v.findViewById(R.id.titlebar_backbutton).setVisibility(View.GONE);
            showMap(v);
        }

        v.findViewById(R.id.titlebar_backbutton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.dismissDetailView();
            }
        });

        buttonPhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialog();
            }
        });

        buttonEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onEdit(poiId);
                }
            }
        });

        buttonRoute.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(Intent.createChooser(intentSaved.get(ACTION_PROVIDER_DIRECTIONS),
                        getString(R.string.menu_directions)));
                return;
            }
        });



        buttonShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(Intent.createChooser(intentSaved.get(ACTION_PROVIDER_SHARE),
                        getString(R.string.menu_share)));
                return;
            }
        });



        return v;
    }

    private void setupUI() {
        if(this.getActivity() != null){
            imageAdapter = new HorizontalImageAdapter(this.getActivity(), listImages);

            listView.setAdapter(imageAdapter);
            listView.setOnItemClickListener(imageAdapter);
        }
    }

    private void getImagesList() {

        Photos photos = null;

        listImages = new ArrayList();

        try {

            WheelmapApp app = (WheelmapApp) this.getActivity().getApplication();
            photos = app.getPhotos();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(photos != null){

            try{

                List<Photo> listOfPhotos = photos.getPhotos();

                if(listOfPhotos.isEmpty()){
                    noPhotosText.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                }
                else{
                    noPhotosText.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);

                    for(Photo p : listOfPhotos){

                        // always loads only the "original" photo
                        String newurl = p.getImages().get(0).getUrl();
                        String[] sList = newurl.split("\\?");
                        String url = sList[0];

                        listImages.add(url);

                        Log.d("load photo with url");

                    }
                }


            }catch(Exception ex){
                Log.d(ex.getMessage());
            }

        }
    }

    private void showMap(View v) {
        int stubId;
        if (AppCapability.degradeDetailMapAsButton()) {
            stubId = R.id.stub_button;
        } else {
            stubId = R.id.stub_map;
        }

        ViewStub stub = (ViewStub) v.findViewById(stubId);
        if(stub != null)
            stub.inflate();

        if (AppCapability.degradeDetailMapAsButton()) {
            assignButton(v);
        } else {
            assignMapView(v);
        }
    }

    private void assignMapView(View v) {
        mapView = (MapView) v.findViewById(R.id.map);
        if(mapView != null){
            mapView.setClickable(true);
            mapView.setBuiltInZoomControls(true);
            mapController = mapView.getController();
            mapController.setZoom(18);
        }
    }

    private void assignButton(View v) {
        mMapButton = (Button) v.findViewById(R.id.btn_map);
        mMapButton.setOnClickListener(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        stateLayout.setOnClickListener(this);
        if (poiId == Extra.ID_UNKNOWN) {
            stateLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_CONTENT, null, this);

    }

    @Override
    public void onStart() {
        super.onStart();
        float startValue = 0.0f;
        float endValue = 1.0f;
        ObjectAnimator anim = ObjectAnimator.ofFloat(getView(),
                "alpha", startValue, endValue);
        anim.setInterpolator(SMOOTH_INTERPOLATOR);
        anim.setDuration(FADE_IN_ANIMATION_DURATION);
        anim.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        WheelmapApp.getSupportManager().cleanReferences();
        ViewTool.nullViewDrawables(getView());
        mapView = null;
        mapController = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        //don't add options in tablet-mode
        if(!getArguments().containsKey(Extra.SHOW_MAP)){
             return;
        }
        Log.d(TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.ab_detail_fragment, menu);
        createShareActionProvider(menu);
        currentMenu = menu;
        menu.setGroupVisible(R.id.menugroup_detailview, mShowMenu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Log.d(TAG, "onPrepareOptionsMenu");

    }

    private void createShareActionProvider(Menu menu) {
        MenuItem menuItemShare = menu.findItem(R.id.menu_share);
        mShareActionProvider = (ShareActionProvider) menuItemShare
                .getActionProvider();
        mShareActionProvider
                .setShareHistoryFileName("ab_provider_share_history.xml");
        setIntentOnActionProvider(ACTION_PROVIDER_SHARE, mShareActionProvider);

        MenuItem menuItemDirection = menu.findItem(R.id.menu_directions);
        mDirectionsActionProvider = (ShareActionProvider) menuItemDirection
                .getActionProvider();
        mDirectionsActionProvider
                .setShareHistoryFileName("ab_provider_directions_history.xml");
        setIntentOnActionProvider(ACTION_PROVIDER_DIRECTIONS,
                mDirectionsActionProvider);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            /*case R.id.menu_edit:
                if (mListener != null) {
                    mListener.onEdit(poiId);
                }
                return true;
            case R.id.menu_directions:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    return false;
                } else {
                    startActivity(Intent.createChooser(intentSaved.get(ACTION_PROVIDER_DIRECTIONS),
                            getString(R.string.menu_directions)));
                    return true;
                }
            case R.id.menu_share:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    return false;
                } else {
                    startActivity(Intent.createChooser(intentSaved.get(ACTION_PROVIDER_SHARE),
                            getString(R.string.menu_share)));
                    return true;
                } */
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public long getPoiId() {
        return poiId;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.wheelchair_state_layout:
                if (mListener != null) {
                    mListener.onEditWheelchairState(mWheelchairState);
                    return;
                }
                break;

            default:
                //
        }

    }

    @Override
    public void onTap(OverlayItem item, ContentValues values) {

        if (mListener != null) {
            mListener.onShowLargeMapAt(item.getPoint());
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arguments) {
        if (poiId == Extra.ID_UNKNOWN) {
            poiId = 0;
        }

        Uri uri = ContentUris.withAppendedId(POIs.CONTENT_URI_COPY, poiId);
        return new CursorLoader(getActivity(), uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished: poiid = " + poiId);
        load(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {

    }

    private void load(Cursor c) {
        if (c == null || c.getCount() < 1 || getPoiId() == -1) {

            titlebarBackbutton.setVisibility(View.GONE);
            addressTitle.setVisibility(View.GONE);
            addressText.setVisibility(View.GONE);
            commentText.setVisibility(View.GONE);
            commentTitle.setVisibility(View.GONE);

            buttonPhoto.setVisibility(View.GONE);
            buttonEdit.setVisibility(View.GONE);
            buttonRoute.setVisibility(View.GONE);
            buttonShare.setVisibility(View.GONE);

            nameText.setVisibility(View.GONE);
            categoryText.setVisibility(View.GONE);
            stateText.setVisibility(View.GONE);
            stateLayout.setVisibility(View.GONE);
            webText.setVisibility(View.GONE);
            phoneText.setVisibility(View.GONE);

            photoTitle.setVisibility(View.GONE);
            photoLayout.setVisibility(View.GONE);

            noCommentText.setVisibility(View.GONE);
            noAdressText.setVisibility(View.GONE);
            noPhotosText.setVisibility(View.GONE);

            nothing.setVisibility(View.VISIBLE);

            return;
        }else{
            titlebarBackbutton.setVisibility(View.VISIBLE);
            addressTitle.setVisibility(View.VISIBLE);
            addressText.setVisibility(View.VISIBLE);
            commentText.setVisibility(View.VISIBLE);
            commentTitle.setVisibility(View.VISIBLE);

            buttonPhoto.setVisibility(View.VISIBLE);
            buttonEdit.setVisibility(View.VISIBLE);
            buttonRoute.setVisibility(View.VISIBLE);
            buttonShare.setVisibility(View.VISIBLE);

            nameText.setVisibility(View.VISIBLE);
            categoryText.setVisibility(View.VISIBLE);
            stateText.setVisibility(View.VISIBLE);
            stateLayout.setVisibility(View.VISIBLE);
            webText.setVisibility(View.VISIBLE);
            phoneText.setVisibility(View.VISIBLE);

            photoTitle.setVisibility(View.VISIBLE);
            photoLayout.setVisibility(View.VISIBLE);

            nothing.setVisibility(View.GONE);

            c.moveToFirst();
            poiId = POIHelper.getId(c);
            String wmIdString = POIHelper.getWMId(c);
            WheelchairState state = POIHelper.getWheelchair(c);
            String name = POIHelper.getName(c);
            String comment = POIHelper.getComment(c);


            String website = POIHelper.getWebsite(c);
            String phone = POIHelper.getPhone(c);

            String street = POIHelper.getStreet(c);
            String houseNum = POIHelper.getHouseNumber(c);
            String postCode = POIHelper.getPostcode(c);
            String city = POIHelper.getCity(c);

            try{
                wmID =  Long.valueOf(POIHelper.getWMId(c));
                getPhotos(wmID);
            }catch(Exception e){}

            String address = "";

            if(street != null){
                address += street + " ";
            }

            if(houseNum != null){
                address += houseNum + " ";
            }

            if(postCode != null){
                address += "\n";
                address += postCode + " ";
            }

            if(city != null){
                address += city + " ";
            }

            int checkIfAdress = 3;

            if(address == ""){
                addressText.setVisibility(View.GONE);
                checkIfAdress--;
            }
            else{
                addressText.setVisibility(View.VISIBLE);
                addressText.setText(address);

            }

            if(phone != null){
                phoneText.setVisibility(View.VISIBLE);
                phoneText.setText(phone);
            }else{
                phoneText.setVisibility(View.GONE);
                checkIfAdress--;
            }

            if(website != null){
                webText.setVisibility(View.VISIBLE);
                webText.setClickable(true);

                String text = "<a href=" + website + ">" + website + "</a>";
                webText.setText(Html.fromHtml(text));

                webText.setMovementMethod(LinkMovementMethod.getInstance());
            }else{
                webText.setVisibility(View.GONE);
                checkIfAdress--;
            }

            if(checkIfAdress == 0)
                noAdressText.setVisibility(View.VISIBLE);
            else
                noAdressText.setVisibility(View.GONE);

            if(comment == null){
                commentText.setVisibility(View.GONE);
                noCommentText.setVisibility(View.VISIBLE);
            }else{
                commentText.setVisibility(View.VISIBLE);
                noCommentText.setVisibility(View.GONE);
            }


            final double latitude = POIHelper.getLatitude(c);
            final double longitude = POIHelper.getLongitude(c);

            int nodeTypeId = POIHelper.getNodeTypeId(c);
            int categoryId = POIHelper.getCategoryId(c);

            SupportManager sm = WheelmapApp.getSupportManager();

            NodeType nodeType = sm.lookupNodeType(nodeTypeId);
            stateLayout.setVisibility(View.VISIBLE);
            setWheelchairState(state);
            if (name != null && name.length() > 0) {
                nameText.setText(name);
            } else {
                nameText.setText(nodeType.localizedName);
            }

            String category = sm.lookupCategory(categoryId).localizedName;
            categoryText.setText(category);
            //nodetypeText.setText(nodeType.localizedName);
            //nodetypeIcon.setImageDrawable(nodeType.iconDrawable);
            commentText.setText(comment);

            fillDirectionsActionProvider(latitude, longitude, street, houseNum,
                    postCode, city);
            fillShareActionProvider(wmIdString, name, nodeType.localizedName,
                    comment, address);

            mShowMenu = true;
            getSupportActivity().invalidateOptionsMenu();

            poiValues = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(c, poiValues);

            if (!getArguments().containsKey(Extra.SHOW_MAP)) {
                return;
            } else if (AppCapability.degradeDetailMapAsButton()) {
                mMapButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onShowLargeMapAt(new GeoPoint(latitude,
                                    longitude));
                        }

                    }
                });
            } else {
                SingleItemOverlay overlay = new SingleItemOverlay(this);
                overlay.setItem(poiValues, nodeType, state);
                overlay.enableLowDrawQuality(true);
                if(mapView != null){
                    mapView.getOverlays().clear();
                    mapView.getOverlays().add(overlay);

                    mapController.setCenter(new GeoPoint(latitude, longitude));
                }
            }
        }


    }

    private void setWheelchairState(WheelchairState newState) {
        mWheelchairState = newState;

        int stateColor = getResources().getColor(
                mWSAttributes.get(newState).colorId);

        if(mWheelchairState.getId() == WheelchairState.UNKNOWN.getId())
            stateText.setBackgroundResource(R.drawable.detail_button_grey);
        else if(mWheelchairState.getId() == WheelchairState.YES.getId())
            stateText.setBackgroundResource(R.drawable.detail_button_green);
        else if(mWheelchairState.getId() == WheelchairState.LIMITED.getId())
            stateText.setBackgroundResource(R.drawable.detail_button_orange);
        else if(mWheelchairState.getId() == WheelchairState.NO.getId())
            stateText.setBackgroundResource(R.drawable.detail_button_red);
        else if(mWheelchairState.getId() == WheelchairState.NO_PREFERENCE.getId())
            stateText.setBackgroundResource(R.drawable.detail_button_grey);
        else
            stateText.setBackgroundResource(R.drawable.detail_button_grey);




        //title_container.setBackgroundColor(stateColor);
        //stateIcon.setImageResource(mWSAttributes.get(newState).drawableId);
        //stateText.setTextColor(stateColor);

        stateText.setText(mWSAttributes.get(newState).titleStringId);

    }

    private Intent createExternIntent(String action) {
        Intent intent = new Intent(action);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        return intent;
    }

    private void fillDirectionsActionProvider(double lat, double lon,
            String street, String houseNum, String postCode, String city) {

        Uri geoURI;

        String latitude = Double.toString(lat);
        String longitude = Double.toString(lon);

        if (!TextUtils.isEmpty(street) && !TextUtils.isEmpty(houseNum)
                && !TextUtils.isEmpty(postCode) && !TextUtils.isEmpty(city)) {
            StringBuilder sb = new StringBuilder();
            sb.append(street).append("+").append(houseNum).append("+")
                    .append(postCode).append(city);
            geoURI = Uri.parse("geo:" + latitude + "," + longitude + "?q="
                    + sb.toString().replace(" ", "+"));
        } else {
            geoURI = Uri.parse("geo:" + latitude + "," + longitude + "?z=17");
        }

        Log.d(TAG, "geoURI = " + geoURI);
        Intent intent = createExternIntent(Intent.ACTION_VIEW);
        intent.setData(geoURI);

        setIntentOrStore(ACTION_PROVIDER_DIRECTIONS, intent,
                mDirectionsActionProvider);
    }

    private void fillShareActionProvider(String wmId, String name, String type,
            String comment, String address) {

        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(name)) {
            sb.append(name);
        } else {
            sb.append(type);
        }

        if (!TextUtils.isEmpty(comment)) {
            sb.append(", ");
            sb.append(comment);
        }

        if (!TextUtils.isEmpty(address)) {
            sb.append(", ");
            sb.append(address);
        }

        //if (!TextUtils.isEmpty(website)) {
        //    sb.append(", ");
        //    sb.append(website);
        //}

        if (sb.length() > 0) {
            sb.append(", ");
        }

        sb.append("http://wheelmap.org/nodes/" + wmId);
        Intent intent = createExternIntent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        setIntentOrStore(ACTION_PROVIDER_SHARE, intent, mShareActionProvider);
    }

    private void setIntentOrStore(int apKey, Intent intent,
            ShareActionProvider provider) {
        intentSaved.put(apKey, intent);
        if (provider != null) {
            provider.setShareIntent(intent);
        }
    }

    private void setIntentOnActionProvider(int apKey,
            ShareActionProvider provider) {
        if (intentSaved.containsKey(apKey)) {
            provider.setShareIntent(intentSaved.get(apKey));
        }
    }

    public void showDetail(long id) {
        Log.d(TAG,"show id: "+id);
        poiId = id;

        if(getLoaderManager() != null)
            getLoaderManager().restartLoader(LOADER_CONTENT, null, this);
    }

    public void getPhotos(long wm_id){

        if(imageAdapter != null){
            imageAdapter.clear();
        }
        DetachableResultReceiver r = new DetachableResultReceiver(new Handler());
        r.setReceiver(this);

        RestServiceHelper.retrievePhotosById(getActivity(),wm_id,r);
    }

    /**
     * {@inheritDoc}
     */
    public void onReceiveResult(int resultCode, Bundle resultData) {

        //wrong result returned?
        if(wmID != resultData.getLong(Extra.ID)){
            //return;
        }

        Log.d(TAG, "onReceiveResult resultCode = " + resultCode);
        switch (resultCode) {
            case RestService.STATUS_RUNNING: {
                break;
            }
            case RestService.STATUS_FINISHED: {
                getImagesList();
                setupUI();
                break;
            }
            case RestService.STATUS_ERROR: {
                break;
            }

        }
    }


    private void startDialog() {

        final Item[] items = {new Item(getString(R.string.photo_upload_picker_gallery),android.R.drawable.ic_menu_gallery),new Item(getString(R.string.photo_upload_picker_take_new), android.R.drawable.ic_menu_camera)};

        final ListAdapter adapter = new ArrayAdapter<Item>(this.getActivity(),android.R.layout.select_dialog_item,android.R.id.text1, items){
            public View getView(int position, View convertView, ViewGroup parent){
                View v = super.getView(position,convertView,parent);
                TextView tv = (TextView)v.findViewById(android.R.id.text1);

                tv.setCompoundDrawablesWithIntrinsicBounds(items[position].icon,0,0,0);

                int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                tv.setCompoundDrawablePadding(dp5);

                return v;
            }

        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());

        builder.setTitle(R.string.photo_upload_picker_title);
        //builder.setIcon(R.drawable.detail_ic_foto);
        builder.setCancelable(true);

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    pictureActionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    pictureActionIntent.setType("image/*");
                    pictureActionIntent.putExtra(Extra.WM_ID, poiId);
                    startActivityForResult(pictureActionIntent,
                            Request.REQUESTCODE_PHOTO);
                } else if (which == 1) {
                    pictureActionIntent = new Intent(
                            android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(pictureActionIntent,
                            Request.REQUESTCODE_PHOTO);
                }

            }
        });
        dialog = builder.create();
        dialog.show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_OK){
            return;
        }

        if(requestCode != Request.REQUESTCODE_PHOTO){
            return;
        }


        File photoFile=null;

        if(data != null && data.getData() == null){
            if(data.getExtras().get("data")!=null){
                try{
                    Bitmap image = (Bitmap) data.getExtras().get("data");
                    photoFile = UtilsMisc.createImageFile(getActivity());
                    FileOutputStream fOut = new FileOutputStream(photoFile);
                    image.compress(Bitmap.CompressFormat.PNG,100, fOut);
                    fOut.flush();
                    fOut.close();
                }catch(Exception e){}
            }
        }else if(data!=null && data.getData() != null ){
            Uri photo = data.getData();
            photoFile = new File(UtilsMisc.getFilePathFromContentUri(photo,
                    getActivity().getContentResolver()));

        }

        uploadPhoto(photoFile);
    }

    File photoFile;
    public void uploadPhoto(File photoFile){
        this.photoFile = photoFile;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(photoFile != null){
             if(progress == null){
                   progress = new ProgressDialog(getActivity());
                   progress.setMessage(getString(R.string.photo_upload_progress_title));
                   progress.show();
             }else{
                 if(progress.isShowing()){
                     return;
                 }
                 progress.show();
             }
             if(dialog !=null){
                dialog.dismiss();
                dialog = null;
             }
             Log.d(TAG,"photo to upload: "+photoFile+"");
             UploadPhotoTask upload = new UploadPhotoTask(getActivity().getApplication(),progress,wmID);
             upload.execute(photoFile);
             photoFile = null;
        }
    }

    @Override
    public void onDestroy() {
        if(progress != null){
            progress.dismiss();
            progress = null;
        }
        if(dialog != null){
            dialog.dismiss();
            dialog = null;
        }
        super.onDestroy();
    }
}
