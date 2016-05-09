package org.wheelmap.android.osmdroid;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.util.constants.MapViewConstants;
import org.wheelmap.android.online.R;
import org.wheelmap.android.utils.ImageUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.util.FloatMath;
import android.util.Log;

import java.util.LinkedList;

public class MarkItemOverlay extends Overlay {

    private static final String TAG = MarkItemOverlay.class.getSimpleName();
    private static final int MAXIMUM_ZOOMLEVEL = 22;

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    protected Context mContext;

    protected final Paint mCirclePaint = new Paint();

    protected final Bitmap mPersonBitmap;
    protected final Bitmap mDirectionArrowBitmap;

    protected final MapView mMapView;

    private final LinkedList<Runnable> mRunOnFirstFix = new LinkedList<Runnable>();
    private final Point mMapCoords = new Point();

    private Location mLocation;
    protected boolean mDrawAccuracyEnabled = true;

    /** Coordinates the feet of the person are located scaled for display density. */
    protected final PointF mPersonHotspot;

    protected final double mDirectionArrowCenterX;
    protected final double mDirectionArrowCenterY;

    // to avoid allocations during onDraw
    private final float[] mMatrixValues = new float[9];
    private final Matrix mMatrix = new Matrix();
    private final Rect mMyLocationRect = new Rect();
    private final Rect mMyLocationPreviousRect = new Rect();

    // ===========================================================
    // Constructors
    // ===========================================================

    public MarkItemOverlay(MapView mapView) {
        this(mapView.getContext(), mapView);
    }

    public MarkItemOverlay(Context context,MapView mapView) {
        super(context);
        mContext = context;

        mMapView = mapView;
        mCirclePaint.setARGB(0, 100, 100, 255);
        mCirclePaint.setAntiAlias(true);

        mPersonBitmap = ImageUtils.drawableToBitmap(ContextCompat.getDrawable(mContext, R.drawable.person));
        mDirectionArrowBitmap = ImageUtils.drawableToBitmap(ContextCompat.getDrawable(mContext, R.drawable.direction_arrow));

        mDirectionArrowCenterX = mDirectionArrowBitmap.getWidth() / 2.0 - 0.5;
        mDirectionArrowCenterY = mDirectionArrowBitmap.getHeight() / 2.0 - 0.5;

        // Calculate position of person icon's feet, scaled to screen density
        mPersonHotspot = new PointF(24.0f * mScale + 0.5f, 39.0f * mScale + 0.5f);
    }

    @Override
    protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (shadow)
            return;

        Log.d(TAG,"drawSafe "+mLocation+" "+shadow);
        if (mLocation != null) {
            drawMyLocation(canvas, mapView, mLocation);
        }
    }

    @Override
    public void onDetach(MapView mapView) {
        //this.disableMyLocation();
        super.onDetach(mapView);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    protected void drawMyLocation(final Canvas canvas, final MapView mapView,
            final Location lastFix) {
        final Projection pj = mapView.getProjection();
        final int zoomDiff = MAXIMUM_ZOOMLEVEL - pj.getZoomLevel();

        float radius = 10 * mContext.getResources().getDisplayMetrics().density;

        mCirclePaint.setAlpha(100);
        mCirclePaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mMapCoords.x >> zoomDiff, mMapCoords.y >> zoomDiff, radius,
                mCirclePaint);

        mCirclePaint.setAlpha(150);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(mMapCoords.x >> zoomDiff, mMapCoords.y >> zoomDiff, radius,
        mCirclePaint);


        canvas.getMatrix(mMatrix);
        mMatrix.getValues(mMatrixValues);

    }

    protected Rect getMyLocationDrawingBounds(int zoomLevel, Location lastFix, Rect reuse) {
        if (reuse == null)
            reuse = new Rect();

        final int zoomDiff = MAXIMUM_ZOOMLEVEL - zoomLevel;
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

    public void setLocation(IGeoPoint geoPoint){
       Location location = new Location("gps");
       location.setLatitude(geoPoint.getLatitude());
       location.setLongitude(geoPoint.getLongitude());
       setLocation(location);
    }

    public void setLocation(Location location){
        mLocation = location;

        TileSystem.LatLongToPixelXY(mLocation.getLatitude(), mLocation.getLongitude(),
                MAXIMUM_ZOOMLEVEL, mMapCoords);
        final int worldSize_2 = TileSystem.MapSize(MAXIMUM_ZOOMLEVEL) / 2;
        mMapCoords.offset(-worldSize_2, -worldSize_2);


         // Get new drawing bounds
        this.getMyLocationDrawingBounds(mMapView.getZoomLevel(), mLocation, mMyLocationRect);

        // If we had a previous location, merge in those bounds too
        if (mLocation != null) {
            mMyLocationRect.union(mMyLocationPreviousRect);
        }

        final int left = mMyLocationRect.left;
        final int top = mMyLocationRect.top;
        final int right = mMyLocationRect.right;
        final int bottom = mMyLocationRect.bottom;

        // Invalidate the bounds
        mMapView.post(new Runnable() {
            @Override
            public void run() {
                mMapView.invalidateMapCoordinates(left, top, right, bottom);
            }
        });

        for (final Runnable runnable : mRunOnFirstFix) {
            new Thread(runnable).start();
        }
        mRunOnFirstFix.clear();
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
