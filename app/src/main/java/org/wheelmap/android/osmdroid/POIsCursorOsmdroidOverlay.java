package org.wheelmap.android.osmdroid;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.safecanvas.ISafeCanvas;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.WheelchairFilterState;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.widget.Toast;

import de.akquinet.android.androlog.Log;

public class POIsCursorOsmdroidOverlay extends ItemizedOverlay<OverlayItem> {

    private final static String TAG = POIsCursorOsmdroidOverlay.class
            .getSimpleName();

    private static final int NO_HIT = -1;

    private Context mContext;

    private Cursor mCursor;

    private Handler mHandler;

    private OnTapListener mListener;

    private final Point mCurScreenCoords = new Point();

    private final Point mTouchScreenPoint = new Point();

    private final Point mItemPoint = new Point();

    private Matrix mTempMatrix = new Matrix();

    public POIsCursorOsmdroidOverlay(Context context, OnTapListener listener) {
        super(WheelmapApp.getSupportManager().getDefaultOverlayDrawable(),
                new DefaultResourceProxyImpl(context));
        mContext = context;
        mHandler = new Handler();
        mListener = listener;
    }

    public synchronized void setCursor(Cursor cursor) {
        if (cursor == mCursor) {
            return;
        }

        if(cursor == null){
            return;
        }

        mCursor = cursor;

        if (mCursor == null) {
            return;
        }

        populate();
    }

    @Override
    public synchronized int size() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    @Override
    protected synchronized OverlayItem createItem(int i) {
        if (mCursor == null || mCursor.isClosed()) {
            return null;
        }

        int count = mCursor.getCount();
        if (count == 0 || i >= count) {
            return null;
        }

        mCursor.moveToPosition(i);
        long id = POIHelper.getId(mCursor);
        String name = POIHelper.getName(mCursor);
        SupportManager manager = WheelmapApp.getSupportManager();
        WheelchairFilterState state = POIHelper.getWheelchair(mCursor);
        double lat = POIHelper.getLatitude(mCursor);
        double lng = POIHelper.getLongitude(mCursor);
        int nodeTypeId = POIHelper.getNodeTypeId(mCursor);
        Drawable marker = null;
        if (nodeTypeId != 0) {
            //marker = manager.lookupNodeType(nodeTypeId).stateDrawables.get(state);
            marker = manager.lookupNodeType(nodeTypeId).getStateDrawable(state);
        }

        float density = mContext.getResources().getDisplayMetrics().density;

        int markerHeight = marker.getIntrinsicHeight();
        int markerWidth = marker.getIntrinsicWidth();
        float heightToWidth = (float)markerWidth / markerHeight;

        int halfDestinationDensity = (int)(16*density);

        //show marker (centered && above) the declared position
        marker.setBounds(
                (int)(-halfDestinationDensity * heightToWidth),
                -2*halfDestinationDensity,
                (int)(halfDestinationDensity * heightToWidth),
                0);

        //Log.d(TAG, "createItem width = " + marker.getIntrinsicWidth() + " height = " + marker.getIntrinsicHeight());

        GeoPoint geo = new GeoPoint(lat, lng);
        OverlayItem item = new OverlayItem(String.valueOf(id), name, name, geo);
        item.setMarker(marker);
        Log.d(TAG,item+" pos: "+geo);
        return item;
    }

    protected boolean executeOnTap(int index) {
        if (mCursor == null) {
            return false;
        }

        int count = mCursor.getCount();
        if (count == 0 || index >= count) {
            return false;
        }

        mCursor.moveToPosition(index);
        long poiId = POIHelper.getId(mCursor);
        Log.d(TAG, "onTap index = " + index + " id = " + poiId);

        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(mCursor, values);
        if (mListener != null) {
            mListener.onTap(createItem(index), values);
        }

        return true;
    }

    protected boolean executeLongPress(int index) {
        if (mCursor == null) {
            return false;
        }

        int count = mCursor.getCount();
        if (count == 0 || index >= count) {
            return false;
        }

        mCursor.moveToPosition(index);
        long poiId = POIHelper.getId(mCursor);
        String name = POIHelper.getName(mCursor);
        int nodeTypeId = POIHelper.getNodeTypeId(mCursor);
        String nodeTypeName = WheelmapApp.getSupportManager().lookupNodeType(
                nodeTypeId).localizedName;
        String address = POIHelper.getAddress(mCursor);

        StringBuilder builder = new StringBuilder();
        if (!TextUtils.isEmpty(name)) {
            builder.append(name);
        } else {
            builder.append(nodeTypeName);
        }

        if (!TextUtils.isEmpty(address)) {
            builder.append(", ");
            builder.append(address);
        }

        final String outputText = builder.toString();
        Log.d(TAG, Long.toString(poiId) + " " + outputText);
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(mContext, outputText, Toast.LENGTH_SHORT).show();
            }

        });
        return true;
    }

    @Override
    public boolean onSnapToItem(int x, int y, Point snapPoint, IMapView mapView) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void drawSafe(ISafeCanvas canvas, MapView mapView, boolean shadow) {

        if (shadow) {
            return;
        }

        final MapView.Projection pj = mapView.getProjection();
        final int size = size() - 1;

        Matrix canvasMatrix = canvas.getMatrix();
        /* Draw in backward cycle, so the items with the least index are on the front. */
        for (int i = size; i >= 0; i--) {
            final OverlayItem item = getItem(i);
            pj.toMapPixels(item.getPoint(), mCurScreenCoords);

            onDrawItem(canvas.getSafeCanvas(), item, mCurScreenCoords, canvasMatrix);
        }
        canvas.setMatrix(canvasMatrix);  //Restore old matrix
    }

    protected void onDrawItem(Canvas canvas, OverlayItem item, Point curScreenCoords,
            Matrix canvasMatrix) {

        Drawable pin = item.getDrawable();
        mTempMatrix.set(canvasMatrix);
        mTempMatrix.preTranslate(curScreenCoords.x, curScreenCoords.y);
        canvas.setMatrix(mTempMatrix);
        pin.draw(canvas);
    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView) {
        int hitIndex = checkForItemHit(event, mapView);
        Log.d(TAG, "onSingleTapConfirmed: hitIndex = " + hitIndex);
        if (hitIndex == NO_HIT) {
            return super.onSingleTapConfirmed(event, mapView);
        }

        return executeOnTap(hitIndex);
    }

    @Override
    public boolean onLongPress(final MotionEvent event, final MapView mapView) {
        int hitIndex = checkForItemHit(event, mapView);
        Log.d(TAG, "onLongPress: hitIndex = " + hitIndex);
        if (hitIndex == NO_HIT) {
            return super.onLongPress(event, mapView);
        }

        return executeLongPress(hitIndex);
    }

    private int checkForItemHit(final MotionEvent event, final MapView mapView) {


        //avoid NullPointerException
        if(mCursor == null
                || mapView == null
                || event == null
                || mapView.getProjection() == null){
            return NO_HIT;
        }

        final MapView.Projection pj = mapView.getProjection();
        final int eventX = (int) event.getX();
        final int eventY = (int) event.getY();

        pj.fromMapPixels(eventX, eventY, mTouchScreenPoint);

        mCursor.moveToFirst();
        while (!mCursor.isAfterLast()) {
            final double lat = POIHelper.getLatitude(mCursor);
            final double lon = POIHelper.getLongitude(mCursor);

            final Drawable marker = this.mDefaultMarker;

            GeoPoint geoPoint = new GeoPoint(lat, lon);
            pj.toPixels(geoPoint, mItemPoint);

            if (marker.getBounds().contains(mTouchScreenPoint.x - mItemPoint.x,
                    mTouchScreenPoint.y - mItemPoint.y)) {
                return mCursor.getPosition();
            }

            mCursor.moveToNext();
        }

        return NO_HIT;
    }
}
