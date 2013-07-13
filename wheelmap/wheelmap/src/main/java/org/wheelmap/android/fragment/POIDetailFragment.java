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

import com.google.inject.Inject;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.nineoldandroids.animation.ObjectAnimator;

import org.holoeverywhere.HoloEverywhere;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Activity.Addons;
import org.holoeverywhere.app.Fragment;
import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapController;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.OverlayItem;
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
import org.wheelmap.android.overlays.OnTapListener;
import org.wheelmap.android.overlays.SingleItemOverlay;
import org.wheelmap.android.utils.SmoothInterpolator;
import org.wheelmap.android.utils.ViewTool;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import de.akquinet.android.androlog.Log;
import roboguice.inject.ContentViewListener;
import roboguice.inject.InjectView;

public class POIDetailFragment extends Fragment implements
        OnClickListener, OnTapListener, LoaderCallbacks<Cursor> {

    public final static String TAG = POIDetailFragment.class.getSimpleName();

    private final static int LOADER_CONTENT = 0;

    private static final Interpolator SMOOTH_INTERPOLATOR = new SmoothInterpolator();

    private final static long FADE_IN_ANIMATION_DURATION = 500;

    @Inject
    public ContentViewListener ignored;

    @InjectView(R.id.title_container)
    private RelativeLayout title_container;

    @InjectView(R.id.titlebar_title)
    private TextView nameText;

    @InjectView(R.id.titlebar_subtitle)
    private TextView categoryText;

    @InjectView(R.id.titlebar_icon)
    private ImageView nodetypeIcon;

    @InjectView(R.id.nodetype)
    private TextView nodetypeText;

    @InjectView(R.id.phone)
    private TextView phoneText;

    @InjectView(R.id.addr)
    private TextView addressText;

    @InjectView(R.id.comment)
    private TextView commentText;

    @InjectView(R.id.website)
    private TextView websiteText;

    @InjectView(R.id.state_icon)
    private ImageView stateIcon;

    @InjectView(R.id.state_text)
    private TextView stateText;

    @InjectView(R.id.wheelchair_state_layout)
    private ViewGroup stateLayout;

    private Button mMapButton;

    private Map<WheelchairState, WheelchairAttributes> mWSAttributes;

    private WheelchairState mWheelchairState;

    public interface OnPOIDetailListener {

        void onEdit(long poiId);

        void onEditWheelchairState(WheelchairState wState);

        void onShowLargeMapAt(GeoPoint point);
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
        f.setArguments(new Bundle());
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_detail, container, false);
        mShowMenu = false;
        if (getArguments().containsKey(Extra.SHOW_MAP)) {
            showMap(v);
        }
        return v;
    }

    private void showMap(View v) {
        int stubId;
        if (AppCapability.degradeDetailMapAsButton()) {
            stubId = R.id.stub_button;
        } else {
            stubId = R.id.stub_map;
        }

        ViewStub stub = (ViewStub) v.findViewById(stubId);
        stub.inflate();

        if (AppCapability.degradeDetailMapAsButton()) {
            assignButton(v);
        } else {
            assignMapView(v);
        }
    }

    private void assignMapView(View v) {
        mapView = (MapView) v.findViewById(R.id.map);

        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapController = mapView.getController();
        mapController.setZoom(18);
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
            case R.id.menu_edit:
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
                }
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
        if (c == null || c.getCount() < 1) {
            return;
        }

        c.moveToFirst();
        poiId = POIHelper.getId(c);
        String wmIdString = POIHelper.getWMId(c);
        WheelchairState state = POIHelper.getWheelchair(c);
        String name = POIHelper.getName(c);
        String comment = POIHelper.getComment(c);
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
        nodetypeText.setText(nodeType.localizedName);
        nodetypeIcon.setImageDrawable(nodeType.iconDrawable);
        commentText.setText(comment);

        String address = POIHelper.getAddress(c);
        addressText.setText(address);

        String website = POIHelper.getWebsite(c);
        websiteText.setText(website);
        phoneText.setText(POIHelper.getPhone(c));

        String street = POIHelper.getStreet(c);
        String houseNum = POIHelper.getHouseNumber(c);
        String postCode = POIHelper.getPostcode(c);
        String city = POIHelper.getCity(c);

        fillDirectionsActionProvider(latitude, longitude, street, houseNum,
                postCode, city);
        fillShareActionProvider(wmIdString, name, nodeType.localizedName,
                comment, address, website);

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
            mapView.getOverlays().clear();
            mapView.getOverlays().add(overlay);
            mapController.setCenter(new GeoPoint(latitude, longitude));
        }
    }

    private void setWheelchairState(WheelchairState newState) {
        mWheelchairState = newState;

        int stateColor = getResources().getColor(
                mWSAttributes.get(newState).colorId);

        title_container.setBackgroundColor(stateColor);
        stateIcon.setImageResource(mWSAttributes.get(newState).drawableId);
        stateText.setTextColor(stateColor);
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

        Log.d(TAG, "geoURI = " + geoURI.toString());
        Intent intent = createExternIntent(Intent.ACTION_VIEW);
        intent.setData(geoURI);

        setIntentOrStore(ACTION_PROVIDER_DIRECTIONS, intent,
                mDirectionsActionProvider);
    }

    private void fillShareActionProvider(String wmId, String name, String type,
            String comment, String address, String website) {

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

        if (!TextUtils.isEmpty(website)) {
            sb.append(", ");
            sb.append(website);
        }

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
        poiId = id;
        getLoaderManager().restartLoader(LOADER_CONTENT, null, this);
    }

}
