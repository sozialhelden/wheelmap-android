package org.wheelmap.android.osmdroid;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.IOverlayMenuProvider;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.SafeDrawOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.safecanvas.ISafeCanvas;
import org.osmdroid.views.safecanvas.SafePaint;
import org.osmdroid.views.util.constants.MapViewConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.location.Location;
import android.util.FloatMath;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import java.util.LinkedList;

/**
 *
 * @author Marc Kurtz
 * @author Manuel Stahl
 *
 */
public class MarkItemOverlay extends SafeDrawOverlay{
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    protected final SafePaint mPaint = new SafePaint();
    protected final SafePaint mCirclePaint = new SafePaint();

    protected final Bitmap mPersonBitmap;
    protected final Bitmap mDirectionArrowBitmap;

    protected final MapView mMapView;

    private final IMapController mMapController;
    public IMyLocationProvider mMyLocationProvider;

    private final LinkedList<Runnable> mRunOnFirstFix = new LinkedList<Runnable>();
    private final Point mMapCoords = new Point();

    private Location mLocation;
    private final GeoPoint mGeoPoint = new GeoPoint(0, 0); // for reuse
    private boolean mIsLocationEnabled = false;
    protected boolean mIsFollowing = false; // follow location updates
    protected boolean mDrawAccuracyEnabled = true;

    /** Coordinates the feet of the person are located scaled for display density. */
    protected final PointF mPersonHotspot;

    protected final double mDirectionArrowCenterX;
    protected final double mDirectionArrowCenterY;

    public static final int MENU_MY_LOCATION = getSafeMenuId();

    private boolean mOptionsMenuEnabled = true;

    // to avoid allocations during onDraw
    private final float[] mMatrixValues = new float[9];
    private final Matrix mMatrix = new Matrix();
    private final Rect mMyLocationRect = new Rect();
    private final Rect mMyLocationPreviousRect = new Rect();

    // ===========================================================
    // Constructors
    // ===========================================================

    public MarkItemOverlay(Context context,MapView mapView) {
        this(mapView, new DefaultResourceProxyImpl(context));
    }

    public MarkItemOverlay(MapView mapView,
            ResourceProxy resourceProxy) {
        super(resourceProxy);

        mMapView = mapView;
        mMapController = mapView.getController();
        mCirclePaint.setARGB(0, 100, 100, 255);
        mCirclePaint.setAntiAlias(true);

        mPersonBitmap = mResourceProxy.getBitmap(ResourceProxy.bitmap.person);
        mDirectionArrowBitmap = mResourceProxy.getBitmap(ResourceProxy.bitmap.direction_arrow);

        mDirectionArrowCenterX = mDirectionArrowBitmap.getWidth() / 2.0 - 0.5;
        mDirectionArrowCenterY = mDirectionArrowBitmap.getHeight() / 2.0 - 0.5;

        // Calculate position of person icon's feet, scaled to screen density
        mPersonHotspot = new PointF(24.0f * mScale + 0.5f, 39.0f * mScale + 0.5f);

    }

    @Override
    public void onDetach(MapView mapView) {
        //this.disableMyLocation();
        super.onDetach(mapView);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    protected void drawMyLocation(final ISafeCanvas canvas, final MapView mapView,
            final Location lastFix) {
        final MapView.Projection pj = mapView.getProjection();
        final int zoomDiff = MapViewConstants.MAXIMUM_ZOOMLEVEL - pj.getZoomLevel();

        if (mDrawAccuracyEnabled) {
            final float radius = lastFix.getAccuracy()
                    / (float) TileSystem.GroundResolution(lastFix.getLatitude(),
                    mapView.getZoomLevel());

            mCirclePaint.setAlpha(50);
            mCirclePaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(mMapCoords.x >> zoomDiff, mMapCoords.y >> zoomDiff, radius,
                    mCirclePaint);

            mCirclePaint.setAlpha(150);
            mCirclePaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(mMapCoords.x >> zoomDiff, mMapCoords.y >> zoomDiff, radius,
                    mCirclePaint);
        }

        canvas.getMatrix(mMatrix);
        mMatrix.getValues(mMatrixValues);

        if (DEBUGMODE) {
            final float tx = (-mMatrixValues[Matrix.MTRANS_X] + 20)
                    / mMatrixValues[Matrix.MSCALE_X];
            final float ty = (-mMatrixValues[Matrix.MTRANS_Y] + 90)
                    / mMatrixValues[Matrix.MSCALE_Y];
            canvas.drawText("Lat: " + lastFix.getLatitude(), tx, ty + 5, mPaint);
            canvas.drawText("Lon: " + lastFix.getLongitude(), tx, ty + 20, mPaint);
            canvas.drawText("Alt: " + lastFix.getAltitude(), tx, ty + 35, mPaint);
            canvas.drawText("Acc: " + lastFix.getAccuracy(), tx, ty + 50, mPaint);
        }

        // Calculate real scale including accounting for rotation
        float scaleX = (float) Math.sqrt(mMatrixValues[Matrix.MSCALE_X]
                * mMatrixValues[Matrix.MSCALE_X] + mMatrixValues[Matrix.MSKEW_Y]
                * mMatrixValues[Matrix.MSKEW_Y]);
        float scaleY = (float) Math.sqrt(mMatrixValues[Matrix.MSCALE_Y]
                * mMatrixValues[Matrix.MSCALE_Y] + mMatrixValues[Matrix.MSKEW_X]
                * mMatrixValues[Matrix.MSKEW_X]);
        final double x = mMapCoords.x >> zoomDiff;
        final double y = mMapCoords.y >> zoomDiff;
        if (lastFix.hasBearing()) {
            canvas.save();
            // Rotate the icon
            canvas.rotate(lastFix.getBearing(), x, y);
            // Counteract any scaling that may be happening so the icon stays the same size
            canvas.scale(1 / scaleX, 1 / scaleY, x, y);
            // Draw the bitmap
            canvas.drawBitmap(mDirectionArrowBitmap, x - mDirectionArrowCenterX, y
                    - mDirectionArrowCenterY, mPaint);
            canvas.restore();
        } else {
            canvas.save();
            // Unrotate the icon if the maps are rotated so the little man stays upright
            canvas.rotate(-mMapView.getMapOrientation(), x, y);
            // Counteract any scaling that may be happening so the icon stays the same size
            canvas.scale(1 / scaleX, 1 / scaleY, x, y);
            // Draw the bitmap
            canvas.drawBitmap(mPersonBitmap, x - mPersonHotspot.x, y - mPersonHotspot.y, mPaint);
            canvas.restore();
        }
    }

    protected Rect getMyLocationDrawingBounds(int zoomLevel, Location lastFix, Rect reuse) {
        if (reuse == null)
            reuse = new Rect();

        final int zoomDiff = MapViewConstants.MAXIMUM_ZOOMLEVEL - zoomLevel;
        final int posX = mMapCoords.x >> zoomDiff;
        final int posY = mMapCoords.y >> zoomDiff;

        // Start with the bitmap bounds
        if (lastFix.hasBearing()) {
            // Get a square bounding box around the object, and expand by the length of the diagonal
            // so as to allow for extra space for rotating
            int widestEdge = (int) Math.ceil(Math.max(mDirectionArrowBitmap.getWidth(),
                    mDirectionArrowBitmap.getHeight()) * Math.sqrt(2));
            reuse.set(posX, posY, posX + widestEdge, posY + widestEdge);
            reuse.offset(-widestEdge / 2, -widestEdge / 2);
        } else {
            reuse.set(posX, posY, posX + mPersonBitmap.getWidth(), posY + mPersonBitmap.getHeight());
            reuse.offset((int) (-mPersonHotspot.x + 0.5f), (int) (-mPersonHotspot.y + 0.5f));
        }

        // Add in the accuracy circle if enabled
        if (mDrawAccuracyEnabled) {
            final int radius = (int) FloatMath.ceil(lastFix.getAccuracy()
                    / (float) TileSystem.GroundResolution(lastFix.getLatitude(), zoomLevel));
            reuse.union(posX - radius, posY - radius, posX + radius, posY + radius);
            final int strokeWidth = (int) FloatMath.ceil(mCirclePaint.getStrokeWidth() == 0 ? 1
                    : mCirclePaint.getStrokeWidth());
            reuse.inset(-strokeWidth, -strokeWidth);
        }

        return reuse;
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    protected void drawSafe(ISafeCanvas canvas, MapView mapView, boolean shadow) {
        if (shadow)
            return;

        if (mLocation != null) {
            drawMyLocation(canvas, mapView, mLocation);
        }
    }

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * Return a GeoPoint of the last known location, or null if not known.
     */
    public GeoPoint getLocation() {
        if (mLocation == null) {
            return null;
        } else {
            return new GeoPoint(mLocation);
        }
    }

    public Location getLastFix() {
        return mLocation;
    }


}
