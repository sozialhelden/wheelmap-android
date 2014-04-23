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
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.app.ProgressDialog;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.wheelmap.android.adapter.Item;
import org.wheelmap.android.async.UploadPhotoTask;
import org.wheelmap.android.manager.MyLocationManager;
import org.wheelmap.android.mapping.node.Image;
import org.wheelmap.android.model.Request;
import org.wheelmap.android.osmdroid.MarkItemOverlay;
import org.wheelmap.android.osmdroid.MyLocationNewOverlayFixed;
import org.wheelmap.android.osmdroid.OnTapListener;
import org.wheelmap.android.osmdroid.POIsCursorOsmdroidOverlay;
import org.wheelmap.android.service.RestService;
import org.wheelmap.android.utils.DetachableResultReceiver.Receiver;
import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapController;
import org.mapsforge.android.maps.MapView;
import org.wheelmap.android.adapter.HorizontalImageAdapter;
import org.wheelmap.android.adapter.HorizontalView;
import org.wheelmap.android.app.AppCapability;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.manager.SupportManager.NodeType;
import org.wheelmap.android.manager.SupportManager.WheelchairAttributes;
import org.wheelmap.android.model.Extra;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.WheelchairState;
import org.wheelmap.android.model.Wheelmap.POIs;
import org.wheelmap.android.online.R;
import org.wheelmap.android.overlays.SingleItemOverlay;
import org.wheelmap.android.service.RestServiceHelper;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.FileUtil;
import org.wheelmap.android.utils.PressSelector;
import org.wheelmap.android.utils.SmoothInterpolator;
import org.wheelmap.android.utils.UtilsMisc;
import org.wheelmap.android.utils.ViewTool;

import android.annotation.SuppressLint;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.akquinet.android.androlog.Log;
import de.greenrobot.event.EventBus;
import roboguice.inject.ContentViewListener;

public class POIDetailFragment extends Fragment implements
        OnTapListener, LoaderCallbacks<Cursor>, Receiver, OnClickListener, MapListener {

    public final static String TAG = POIDetailFragment.class.getSimpleName();

    private final static int LOADER_CONTENT = 0;

    private static final Interpolator SMOOTH_INTERPOLATOR = new SmoothInterpolator();

    private final static long FADE_IN_ANIMATION_DURATION = 500;

    private final static int FOCUS_TO_NOTHING = 0;
    private final static int FOCUS_TO_ADRESS = 1;
    private final static int FOCUS_TO_COMMENT = 2;






    private IMapController mMapController;

    private static String baseUrl = "http://a.tiles.mapbox.com/v3/%s/";

    private static String tileUrl;

    private static final byte ZOOMLEVEL_MIN = 16;

    private OnlineTileSourceBase mMapBoxTileSource; private static final float SPAN_ENLARGEMENT_FAKTOR = 1.3f;

    private org.osmdroid.views.MapView mMapView;

    private IGeoPoint mLastRequestedPosition;

    private EventBus mBus;

    private int mVerticalDelta;

    private final static int VERTICAL_DELTA = 20;

    private double mCrrLatitude;

    private double mCrrLongitude;

    private boolean mHeightFull = true;

    private boolean isCentered;

    private ImageButton mBtnExpand;
    private ImageButton mBtnLocate;


    private RelativeLayout layoutMapDetail;

    boolean mapFocus = false;

    int mHeightLayout = -1;

    private POIsCursorOsmdroidOverlay mPoisItemizedOverlay;

    private MyLocationNewOverlay mCurrLocationOverlay;

    private MarkItemOverlay markItemOverlay;

    private MyLocationProvider mMyLocationProvider = new MyLocationProvider();

    private Location mLocation;







    ImageView img_logo;
    private Intent pictureActionIntent = null;
    Bitmap bitmap;

    long wmID;

    Cursor mCursor;


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
    public boolean onScroll(ScrollEvent event) {
        isCentered = false;
        return false;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        isCentered = false;
        return false;
    }


    public interface OnPOIDetailListener {

        void onEdit(long poiId, int focus);

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

    LinearLayout layoutComment;
    LinearLayout layoutAdress;
    LinearLayout layoutPhoto;

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

        if(!UtilsMisc.isTablet(getActivity().getApplication())){

            tileUrl = String.format( baseUrl, getString(R.string.mapbox_key));
            mMapBoxTileSource = new XYTileSource("Mapbox", null, 3, 21, 256, ".png", new String[] { tileUrl });
            mBus = EventBus.getDefault();
            mVerticalDelta = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, (float) VERTICAL_DELTA,
                    getResources().getDisplayMetrics());

            MyLocationManager.LocationEvent event = (MyLocationManager.LocationEvent) mBus
                    .getStickyEvent(MyLocationManager.LocationEvent.class);
            mLocation = event.location;
        }
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

        layoutAdress = (LinearLayout)v.findViewById(R.id.layout_detail_adress);
        layoutComment = (LinearLayout)v.findViewById(R.id.layout_detail_comment);
        layoutPhoto = (LinearLayout)v.findViewById(R.id.photo_layout);
        if(!UtilsMisc.isTablet(getActivity().getApplication())){
            layoutMapDetail = (RelativeLayout)v.findViewById(R.id.layout_map_detail);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = null;
        if(!UtilsMisc.isTablet(getActivity().getApplicationContext())){

            v = inflater.inflate(R.layout.fragment_detail, container, false);
        }

        if(UtilsMisc.isTablet(getActivity().getApplicationContext())){
            v = inflater.inflate(R.layout.fragment_detail_tablet, container, false);
        }

        initViews(v);

        ScrollView scrollView = (ScrollView)v.findViewById(R.id.scrollView);
        scrollView.requestDisallowInterceptTouchEvent(true);

        View closeButton = v.findViewById(R.id.titlebar_backbutton);

        mShowMenu = false;
        if (getArguments().containsKey(Extra.SHOW_MAP)) {
            closeButton.setVisibility(View.INVISIBLE);
            showMap(v);
        }

        if(!UtilsMisc.isTablet(getActivity().getApplicationContext())){
            closeButton.setVisibility(View.INVISIBLE);
        }

        closeButton.setOnClickListener(new OnClickListener() {
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
                    mListener.onEdit(poiId, FOCUS_TO_NOTHING);
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

        layoutComment.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onEdit(poiId, FOCUS_TO_COMMENT);
                }
            }
        });
        layoutAdress.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onEdit(poiId, FOCUS_TO_ADRESS);
                }
            }
        });

        layoutPhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialog();

            }
        });

        if(!UtilsMisc.isTablet(getActivity().getApplication())){


            mMapView = (org.osmdroid.views.MapView) v.findViewById(R.id.map_detail);
            mBtnExpand = (ImageButton) v.findViewById(R.id.map_btn_expand);
            mBtnLocate = (ImageButton) v.findViewById(R.id.map_btn_locate);

            mMapView.setTileSource(mMapBoxTileSource);
            //mMapView.setBuiltInZoomControls(true);
            mMapView.setMultiTouchControls(true);

            mMapView.setMapListener(this);

            //mMapView.setBuiltInZoomControls(true);
            mMapController = mMapView.getController();
            mMapController.setZoom(18);
            mMapController.setCenter(new org.osmdroid.mapsforge.wrapper.GeoPoint(new GeoPoint(mCrrLatitude, mCrrLongitude)));

            mPoisItemizedOverlay = new POIsCursorOsmdroidOverlay(getActivity(), this);
            mCurrLocationOverlay = new MyLocationNewOverlay(getActivity(), mMyLocationProvider,
                    mMapView);
            mCurrLocationOverlay.enableMyLocation();

            markItemOverlay = new MarkItemOverlay(getActivity(),mMapView);

            //mMapView.getOverlays().add(markItemOverlay);

            mMapView.getOverlays().add(mPoisItemizedOverlay);

            MyLocationNewOverlayFixed a = new MyLocationNewOverlayFixed(getActivity(), mMyLocationProvider,
                    mMapView);
            a.enableMyLocation();
            mMyLocationProvider.startLocationProvider(a);
            mMapView.getOverlays().add(a);



            mBtnExpand.setOnTouchListener(new PressSelector());
            mBtnExpand.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    /*centerMap(mCurrentLocationGeoPoint, true);
                    setZoomIntern(17);
                    requestUpdate();*/

                    if(mapFocus){
                        mBtnExpand.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_detail_expand));
                        mapFocus = false;

                        HeightAnimation heightAnim = new HeightAnimation(layoutMapDetail, layoutMapDetail.getHeight(), mHeightLayout);
                        heightAnim.setDuration(1000);
                        layoutMapDetail.startAnimation(heightAnim);


                    }else if(!mapFocus){
                        mBtnExpand.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_detail_collapse));
                        mapFocus = true;
                        if(mHeightLayout <= 0){
                            mHeightLayout = layoutMapDetail.getHeight();
                        }
                        HeightAnimation heightAnim = new HeightAnimation(layoutMapDetail, mHeightLayout, (mHeightLayout+800));
                        heightAnim.setDuration(1000);
                        layoutMapDetail.startAnimation(heightAnim);
                    }
                }
            });

            mBtnLocate.setOnTouchListener(new PressSelector());
            mBtnLocate.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    org.osmdroid.util.GeoPoint geoPoint = new org.osmdroid.util.GeoPoint(mCrrLatitude,
                            mCrrLongitude);

                    if (mMapView != null && !isCentered) {
                        centerMap(geoPoint, false);
                    }
                }
            });
        }

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

        listImages = null;

        try {

            WheelmapApp app = (WheelmapApp) this.getActivity().getApplication();
            listImages = app.getListImages();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(listImages != null){

            try{

                if(listImages.isEmpty()){
                    noPhotosText.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                }
                else{
                    noPhotosText.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
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
            //mapView.setBuiltInZoomControls(true);
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
    public void onTap(OverlayItem item, ContentValues values) {
        /*if (mListener != null) {
            mListener.onShowLargeMapAt(item.getPoint());
        } */
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
        mCursor= cursor;
        load(cursor);

        try{
            wmID =  Long.valueOf(POIHelper.getWMId(cursor));
            getPhotos(wmID);
        }catch(Exception e){}

        //mPoisItemizedOverlay.setCursor(mCursor);
        //mMapView.postInvalidate();

        if(!UtilsMisc.isTablet(getActivity().getApplication())){

            createPOIForDetailMap();
            mMapView.postInvalidate();

        }



    }

    public void createPOIForDetailMap(){
        ArrayList<OverlayItem> overlayItemArray = new ArrayList<OverlayItem>();

        long id = POIHelper.getId(mCursor);
        String name = POIHelper.getName(mCursor);
        SupportManager manager = WheelmapApp.getSupportManager();
        WheelchairState state = POIHelper.getWheelchair(mCursor);
        double lat = POIHelper.getLatitude(mCursor);
        double lng = POIHelper.getLongitude(mCursor);
        int nodeTypeId = POIHelper.getNodeTypeId(mCursor);
        Drawable marker = null;
        if (nodeTypeId != 0) {
            marker = manager.lookupNodeTypeList(nodeTypeId).stateDrawables.get(state);
        }
        marker = marker.getConstantState().newDrawable();
        float density = getActivity().getResources().getDisplayMetrics().density;

        int half = (int)(10*density);

        marker.setBounds(-half, -2*half, half, 0);

        //Log.d(TAG, "createItem width = " + marker.getIntrinsicWidth() + " height = " + marker.getIntrinsicHeight());

        org.osmdroid.util.GeoPoint geo = new org.osmdroid.util.GeoPoint(lat, lng);
        OverlayItem item = new OverlayItem(String.valueOf(id), name, name, geo);
        item.setMarker(marker);
        overlayItemArray.add(item);

        DefaultResourceProxyImpl defaultResourceProxyImpl = new DefaultResourceProxyImpl(this.getActivity().getApplicationContext());
        ItemizedIconOverlay<OverlayItem> myItemizedIconOverlay  = new ItemizedIconOverlay<OverlayItem>(overlayItemArray, null, defaultResourceProxyImpl);

        mMapView.getOverlays().add(myItemizedIconOverlay);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {

    }

    public void load(Cursor c) {
        mCursor = c;
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

            c.moveToFirst();
            if(!UtilsMisc.isTablet(getActivity().getApplication())){
                mCrrLongitude = POIHelper.getLongitude(c);
                mCrrLatitude = POIHelper.getLatitude(c);

                org.osmdroid.util.GeoPoint geoPoint = new org.osmdroid.util.GeoPoint(mCrrLatitude,
                        mCrrLongitude);

                if (mMapView != null && !isCentered) {
                    centerMap(geoPoint, false);
                }
            }

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

            /*
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
            }*/
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
                    .append(postCode).append("+").append(city);
            geoURI = Uri.parse("geo:" + latitude + "," + longitude + "?q="
                    + sb.toString().replace(" ", "+"));
        } else {
            //geoURI = Uri.parse("geo:" + latitude + "," + longitude);

            StringBuilder sb = new StringBuilder();
            sb.append(latitude).append("+").append(longitude);
            geoURI = Uri.parse("geo:" + latitude + "," + longitude + "?q="
                    + sb.toString().replace(" ", "+"));
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
                    startGetPhotoFromGalleryIntent();
                    /*pictureActionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    pictureActionIntent.setType("image/*");
                    pictureActionIntent.putExtra(Extra.WM_ID, poiId);
                    pictureActionIntent = getGetPhotoFromGalleryIntent();
                    startActivityForResult(pictureActionIntent,
                            Request.REQUESTCODE_PHOTO);   */
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

    public void startGetPhotoFromGalleryIntent(){
        if (Build.VERSION.SDK_INT < 19){
            Intent intent = new Intent();
            intent.setType("image/jpeg");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent,
                    Request.REQUESTCODE_PHOTO);
        } else {
            final String ACTION_OPEN_DOCUMENT = "android.intent.action.OPEN_DOCUMENT";
            Intent intent = new Intent(ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/jpeg");
            startActivityForResult(intent, Request.GALLERY_KITKAT_INTENT_CALLED);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_OK){
            return;
        }

        if(requestCode == Request.REQUESTCODE_PHOTO
                || requestCode == Request.GALLERY_KITKAT_INTENT_CALLED){
            handlePhotoIntentResult(requestCode,resultCode,data);
            return;
        }


    }

    @SuppressLint("NewApi")
    private void handlePhotoIntentResult(int requestCode, int resultCode, Intent data){
        File photoFile=null;

        if(data != null && data.getData() == null){
            if(data.getExtras().get("data")!=null){
                try{
                    Bitmap image = (Bitmap) data.getExtras().get("data");
                    photoFile = UtilsMisc.createImageFile(getActivity());

                    FileOutputStream fOut = new FileOutputStream(photoFile);
                    image.compress(Bitmap.CompressFormat.JPEG,100, fOut);
                    fOut.flush();
                    fOut.close();
                }catch(Exception e){}
            }
        }else if(data!=null && data.getData() != null ){
            Uri photo = data.getData();

            /*if(requestCode == Request.GALLERY_KITKAT_INTENT_CALLED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                // Check for the freshest data.
                //getActivity().getContentResolver().takePersistableUriPermission(photo, takeFlags);

            }*/


            String path = FileUtil.getPath(getActivity(), photo);
            if(path != null){
                photoFile = new File(path);
            }else{
                try{
                    photoFile = new File(UtilsMisc.getFilePathFromContentUri(photo,
                          getActivity().getContentResolver()));
                }catch(Exception e){}
            }
        }

        if(photoFile != null){
            uploadPhoto(photoFile);
        }else{
            //TODO but should never happen
        }
    }

    File photoFile;
    public void uploadPhoto(File photoFile){
        this.photoFile = photoFile;
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
            UploadPhotoTask upload = new UploadPhotoTask(mCursor,this,getActivity().getApplication(),progress,wmID);
            upload.execute(photoFile);
            photoFile = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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

    private void centerMap(org.osmdroid.util.GeoPoint geoPoint, boolean force) {
        Log.d(TAG, "centerMap: force = " + force + " isCentered = "
                + isCentered + " geoPoint = " + geoPoint);
        if (force || !isCentered) {
            setCenterWithOffset(geoPoint);
        }
    }

    private void setCenterWithOffset(org.osmdroid.util.GeoPoint geoPoint) {
        if (geoPoint == null) {
            return;
        }
        boolean land = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        IGeoPoint actualGeoPoint = geoPoint;




        if (mHeightFull) {
            actualGeoPoint = geoPoint;
        } else {

            if(land){
                /*Projection projection = mMapView.getProjection();
                Point point = new Point();
                point = projection.toMapPixels(geoPoint,point);
                //point = projection.toPixels(geoPoint, point);
                int horizontalOffset = mMapView.getWidth() / 4;
                point.x -= horizontalOffset;
                actualGeoPoint = projection.fromPixels(point.x, point.y);
                */

                Point point = pointFromGeoPoint(geoPoint,mMapView);
                int horizontalOffset = mMapView.getWidth() / 4;
                point.x -= horizontalOffset;
                actualGeoPoint = geoPointFromScreenCoords(point.x,point.y,mMapView);

            }else{
                /*Projection projection = mMapView.getProjection();
                Point point = new Point();
                point = projection.toPixels(geoPoint, point);
                int mVerticalOffset = mMapView.getHeight() / 4;
                point.y -= mVerticalOffset;
                actualGeoPoint = projection.fromPixels(point.x, point.y); */

                Point point = pointFromGeoPoint(geoPoint,mMapView);
                int mVerticalOffset = mMapView.getHeight() / 4;
                point.y -= mVerticalOffset;
                actualGeoPoint = geoPointFromScreenCoords(point.x,point.y,mMapView);
            }
        }

        mMapController.setCenter(actualGeoPoint);
        isCentered = true;
    }

    /**
     *
     * @param x  view coord relative to left
     * @param y  view coord relative to top
     * @param vw MapView
     * @return GeoPoint
     */
    private org.osmdroid.util.GeoPoint geoPointFromScreenCoords(int x, int y, org.osmdroid.views.MapView vw){
        // Get the top left GeoPoint
        org.osmdroid.views.MapView.Projection projection = vw.getProjection();
        org.osmdroid.util.GeoPoint geoPointTopLeft = (org.osmdroid.util.GeoPoint) projection.fromPixels(0, 0);
        Point topLeftPoint = new Point();
        // Get the top left Point (includes osmdroid offsets)
        projection.toPixels(geoPointTopLeft, topLeftPoint);
        // get the GeoPoint of any point on screen
        org.osmdroid.util.GeoPoint rtnGeoPoint = (org.osmdroid.util.GeoPoint) projection.fromPixels(x, y);
        return rtnGeoPoint;
    }

    /**
     *
     * @param gp GeoPoint
     * @param vw Mapview
     * @return a 'Point' in screen coords relative to top left
     */
    private Point pointFromGeoPoint(org.osmdroid.util.GeoPoint gp, org.osmdroid.views.MapView vw){

        Point rtnPoint = new Point();
        org.osmdroid.views.MapView.Projection projection = vw.getProjection();
        projection.toPixels(gp, rtnPoint);
        // Get the top left GeoPoint
        org.osmdroid.util.GeoPoint geoPointTopLeft = (org.osmdroid.util.GeoPoint) projection.fromPixels(0, 0);
        Point topLeftPoint = new Point();
        // Get the top left Point (includes osmdroid offsets)
        projection.toPixels(geoPointTopLeft, topLeftPoint);
        rtnPoint.x-= topLeftPoint.x; // remove offsets
        rtnPoint.y-= topLeftPoint.y;
        return rtnPoint;
    }

    private Location getLocation() {
        return mLocation;
    }

    private class MyLocationProvider implements IMyLocationProvider, SensorEventListener {

        private static final float MIN_DIRECTION_DELTA = 10;

        private float lastDirection;

        private float mDirection;

        private Location mProviderLocation;

        private IMyLocationConsumer mMyLocationConsumer;

        @Override
        public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
            mMyLocationConsumer = myLocationConsumer;
            updateLocation(getLocation());
            return true;
        }

        @Override
        public void stopLocationProvider() {
            mMyLocationConsumer = null;
        }

        @Override
        public Location getLastKnownLocation() {
            return mProviderLocation;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float direction = event.values[0];
            if (direction > 180) {
                direction -= 360;
            }

            if (isAdded()) {
                direction -= UtilsMisc.calcRotationOffset(getActivity()
                        .getWindowManager().getDefaultDisplay());
            }

            if (Math.abs(direction - lastDirection) < MIN_DIRECTION_DELTA) {
                return;
            }

            lastDirection = mDirection;
            mDirection = direction;
            Log.d(TAG, "direction: " + direction);
            updateLocation(getLocation());
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        public void updateLocation(Location location) {
            //Log.d(TAG, "updateLocation: location = " + location + " consumer = "
            //        + mMyLocationConsumer);
            if (location == null) {
                return;
            }

            mProviderLocation = location;
            mProviderLocation.setBearing(mDirection + 90);
            if (mMyLocationConsumer != null) {
                mMyLocationConsumer.onLocationChanged(mProviderLocation, this);
            }
        }
    }
}

/**
 * an animation for resizing the view.
 */

class HeightAnimation extends Animation {
    protected final int originalHeight;
    protected final View view;
    protected float perValue;

    public HeightAnimation(View view, int fromHeight, int toHeight) {
        this.view = view;
        this.originalHeight = fromHeight;
        this.perValue = (toHeight - fromHeight);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        view.getLayoutParams().height = (int) (originalHeight + perValue * interpolatedTime);
        view.requestLayout();
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }


}



