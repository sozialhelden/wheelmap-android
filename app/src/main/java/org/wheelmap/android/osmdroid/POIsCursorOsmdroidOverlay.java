package org.wheelmap.android.osmdroid;

import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.WheelchairFilterState;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Toast;

import de.akquinet.android.androlog.Log;
import roboguice.event.Observes;

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
        super(context, WheelmapApp.getSupportManager().getDefaultOverlayDrawable());
        mContext = context;
        mHandler = new Handler();
        mListener = listener;
    }

    public synchronized void setCursor(Cursor cursor) {
        if (cursor == mCursor) {
            return;
        }

        if (cursor == null) {
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
            marker = manager.lookupNodeType(nodeTypeId).getStateDrawable(state);
        }

        if (marker == null) {
            return null;
        }

        float density = mContext.getResources().getDisplayMetrics().density;

        int markerHeight = marker.getIntrinsicHeight();
        int markerWidth = marker.getIntrinsicWidth();
        float heightToWidth = (float) markerWidth / markerHeight;

        int halfDestinationDensity = (int) (16 * density);

        //show marker (centered && above) the declared position
        marker.setBounds(
                (int) (-halfDestinationDensity * heightToWidth),
                -2 * halfDestinationDensity,
                (int) (halfDestinationDensity * heightToWidth),
                0);

        GeoPoint geo = new GeoPoint(lat, lng);
        OverlayItem item = new OverlayItem(String.valueOf(id), name, name, geo);
        item.setMarker(marker);
        Log.d(TAG, item + " pos: " + geo);
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
    protected void draw(Canvas canvas, MapView mapView, boolean shadow) {

        if (shadow) {
            return;
        }

        final Projection pj = mapView.getProjection();
        final int size = size() - 1;

        Matrix canvasMatrix = canvas.getMatrix();
        /* Draw in backward cycle, so the items with the least index are on the front. */
        for (int i = size; i >= 0; i--) {
            final OverlayItem item = getItem(i);
            pj.toPixels(item.getPoint(), mCurScreenCoords);

            onDrawItem(canvas, item, mCurScreenCoords, canvasMatrix);
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
    public boolean onLongPress(final MotionEvent event, final MapView mapView) {
        int hitIndex = checkForItemHit(event, mapView);
        Log.d(TAG, "onLongPress: hitIndex = " + hitIndex);
        if (hitIndex == NO_HIT) {
            return super.onLongPress(event, mapView);
        }

        return executeLongPress(hitIndex);
    }

    @Override
    protected boolean onTap(int index) {
        return executeOnTap(index);
    }

    private int checkForItemHit(final MotionEvent event, final MapView mapView) {


        //avoid NullPointerException
        if (mCursor == null
                || mapView == null
                || event == null
                || mapView.getProjection() == null) {
            return NO_HIT;
        }

        final Projection pj = mapView.getProjection();
        final int eventX = (int) event.getX();
        final int eventY = (int) event.getY();

        Point p = new Point(eventX, eventY);
        pj.toPixelsFromProjected(p, mTouchScreenPoint);

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
