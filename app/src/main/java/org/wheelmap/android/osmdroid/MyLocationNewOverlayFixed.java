package org.wheelmap.android.osmdroid;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.IOverlayMenuProvider;
import org.osmdroid.views.overlay.Overlay.Snappable;
import org.osmdroid.views.overlay.SafeDrawOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.safecanvas.ISafeCanvas;
import org.osmdroid.views.safecanvas.SafePaint;
import org.osmdroid.views.util.constants.MapViewConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wheelmap.android.fragment.POIsOsmdroidFragment;
import org.wheelmap.android.utils.MyLocationProvider;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import de.akquinet.android.androlog.Log;

/**
 *
 * @author Marc Kurtz
 * @author Manuel Stahl
 *
 */
public class MyLocationNewOverlayFixed extends SafeDrawOverlay implements IMyLocationConsumer,
        IOverlayMenuProvider, Snappable {
    private static final Logger logger = LoggerFactory.getLogger(MyLocationNewOverlayFixed.class);
    private static final String TAG = MyLocationNewOverlayFixed.class.getSimpleName();

    // ===========================================================
    // Constants
    // ===========================================================

    private final int DEFAULT_ZOOMLEVEL_LOCATED = 18;
    private final int DEFAULT_ZOOMLEVEL_UNKNOWN = 6;

    // ===========================================================
    // Fields
    // ===========================================================

    protected final SafePaint mPaint = new SafePaint();
    protected final Bitmap locationMarkerBitmap;

    protected final MapView mMapView;

    private final IMapController mMapController;
    public IMyLocationProvider mMyLocationProvider;

    private final LinkedList<Runnable> mRunOnFirstFix = new LinkedList<Runnable>();
    private final Point mMapCoords = new Point();

    private Location mLocation;
    private final GeoPoint mGeoPoint = new GeoPoint(0, 0); // for reuse
    private boolean mIsLocationEnabled = false;

    protected boolean mIsFollowing = true; // follow location updates

    protected final double locationMarkerCenterX;
    protected final double locationMarkerCenterY;

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

    Context mContext;

    public MyLocationNewOverlayFixed(Context context, MapView mapView) {
        this(context, new GpsMyLocationProvider(context), mapView);
    }

    public MyLocationNewOverlayFixed(Context context, IMyLocationProvider myLocationProvider,
            MapView mapView) {
        this(myLocationProvider, mapView, new DefaultResourceProxyImpl(context),context);
    }

    public MyLocationNewOverlayFixed(IMyLocationProvider myLocationProvider, MapView mapView,
            ResourceProxy resourceProxy,Context context) {
        super(resourceProxy);
        mContext = context;

        mMapView = mapView;
        mMapController = mapView.getController();

        locationMarkerBitmap = getBitmapDirectionArrow();
        locationMarkerCenterX = locationMarkerBitmap.getWidth() / 2.0;
        locationMarkerCenterY = locationMarkerBitmap.getHeight() / 2.0;

        setMyLocationProvider(myLocationProvider);
    }

    public Bitmap getBitmapDirectionArrow(){
        InputStream is = null;
        try {
            final String resName = "location_poi.png";
            is = mContext.getAssets().open(resName);
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resName);
            }
            DisplayMetrics mDisplayMetrics =  mContext.getResources().getDisplayMetrics();

            BitmapFactory.Options options = new BitmapFactory.Options();
            DisplayMetrics metrics = mContext.getApplicationContext().getResources().getDisplayMetrics();
            options.inScreenDensity = metrics.densityDpi;
            options.inTargetDensity =  metrics.densityDpi;
            options.inDensity = DisplayMetrics.DENSITY_DEFAULT*2;

            return BitmapFactory.decodeStream(is, null, options);
        } catch (final Exception e) {
            System.gc();
            // there's not much we can do here
            // - when we load a bitmap from resources we expect it to be found
            e.printStackTrace();
            return null;
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (final IOException ignore) {
                }
            }
        }
    }

    @Override
    public void onDetach(MapView mapView) {
        this.disableMyLocation();
        super.onDetach(mapView);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public IMyLocationProvider getMyLocationProvider() {
        return mMyLocationProvider;
    }

    protected void setMyLocationProvider(IMyLocationProvider myLocationProvider) {
        if (myLocationProvider == null)
            throw new RuntimeException(
                    "You must pass an IMyLocationProvider to setMyLocationProvider()");

        if (mMyLocationProvider != null)
            mMyLocationProvider.stopLocationProvider();

        mMyLocationProvider = myLocationProvider;
    }

    protected void drawMyLocation(final ISafeCanvas canvas, final MapView mapView,
            final Location lastFix) {
        final Projection pj = mapView.getProjection();
        final int zoomDiff = MapViewConstants.MAXIMUM_ZOOMLEVEL - pj.getZoomLevel();

        canvas.getMatrix(mMatrix);
        mMatrix.getValues(mMatrixValues);

        // Calculate real scale including accounting for rotation
        float scaleX = (float) Math.sqrt(mMatrixValues[Matrix.MSCALE_X]
                * mMatrixValues[Matrix.MSCALE_X] + mMatrixValues[Matrix.MSKEW_Y]
                * mMatrixValues[Matrix.MSKEW_Y]);
        float scaleY = (float) Math.sqrt(mMatrixValues[Matrix.MSCALE_Y]
                * mMatrixValues[Matrix.MSCALE_Y] + mMatrixValues[Matrix.MSKEW_X]
                * mMatrixValues[Matrix.MSKEW_X]);
        final double x = mMapCoords.x >> zoomDiff;
        final double y = mMapCoords.y >> zoomDiff;

        canvas.save();
        canvas.scale(1 / scaleX, 1 / scaleY, x, y);

        float density = mContext.getResources().getDisplayMetrics().density;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(locationMarkerBitmap, (int)(20 * density), (int)(20 * density), false);

        canvas.drawBitmap(scaledBitmap, x - scaledBitmap.getWidth() / 2, y - scaledBitmap.getHeight() / 2, null);
        canvas.restore();
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
            int widestEdge = (int) Math.ceil(Math.max(locationMarkerBitmap.getWidth(),
                    locationMarkerBitmap.getHeight()) * Math.sqrt(2));
            reuse.set(posX, posY, posX + widestEdge, posY + widestEdge);
            reuse.offset(-widestEdge / 2, -widestEdge / 2);
        } else {
            reuse.set(posX, posY, posX + locationMarkerBitmap.getWidth(), posY + locationMarkerBitmap.getHeight());
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

        if (mLocation != null && isMyLocationEnabled()) {
            drawMyLocation(canvas, mapView, mLocation);
        }
    }

    @Override
    public boolean onSnapToItem(final int x, final int y, final Point snapPoint,
            final IMapView mapView) {
        if (this.mLocation != null) {
            snapPoint.x = mMapCoords.x;
            snapPoint.y = mMapCoords.y;
            final double xDiff = x - mMapCoords.x;
            final double yDiff = y - mMapCoords.y;
            final boolean snap = xDiff * xDiff + yDiff * yDiff < 64;
            if (DEBUGMODE) {
                logger.debug("snap=" + snap);
            }
            return snap;
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            this.disableFollowLocation();
        }

        return super.onTouchEvent(event, mapView);
    }

    // ===========================================================
    // Menu handling methods
    // ===========================================================

    @Override
    public void setOptionsMenuEnabled(final boolean pOptionsMenuEnabled) {
        this.mOptionsMenuEnabled = pOptionsMenuEnabled;
    }

    @Override
    public boolean isOptionsMenuEnabled() {
        return this.mOptionsMenuEnabled;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
            final MapView pMapView) {
        pMenu.add(0, MENU_MY_LOCATION + pMenuIdOffset, Menu.NONE,
                mResourceProxy.getString(ResourceProxy.string.my_location))
                .setIcon(mResourceProxy.getDrawable(ResourceProxy.bitmap.ic_menu_mylocation))
                .setCheckable(true);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
            final MapView pMapView) {
        pMenu.findItem(MENU_MY_LOCATION + pMenuIdOffset).setChecked(this.isMyLocationEnabled());
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem pItem, final int pMenuIdOffset,
            final MapView pMapView) {
        final int menuId = pItem.getItemId() - pMenuIdOffset;
        if (menuId == MENU_MY_LOCATION) {
            if (this.isMyLocationEnabled()) {
                this.disableFollowLocation();
                this.disableMyLocation();
            } else {
                this.enableFollowLocation();
                this.enableMyLocation();
            }
            return true;
        } else {
            return false;
        }
    }

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * Return a GeoPoint of the last known location, or null if not known.
     */
    public GeoPoint getMyLocation() {
        if (mLocation == null) {
            return null;
        } else {
            return new GeoPoint(mLocation);
        }
    }

    public Location getLastFix() {
        return mLocation;
    }

    /**
     * Enables "follow" functionality. The map will center on your current location and
     * automatically scroll as you move. Scrolling the map in the UI will disable.
     */
    public void enableFollowLocation() {
        mIsFollowing = true;

        // set initial location when enabled
        if (isMyLocationEnabled()) {
            mLocation = mMyLocationProvider.getLastKnownLocation();
            if (mLocation != null) {
                TileSystem.LatLongToPixelXY(mLocation.getLatitude(), mLocation.getLongitude(),
                        MapViewConstants.MAXIMUM_ZOOMLEVEL, mMapCoords);
                final int worldSize_2 = TileSystem.MapSize(MapViewConstants.MAXIMUM_ZOOMLEVEL) / 2;
                mMapCoords.offset(-worldSize_2, -worldSize_2);
                mMapController.animateTo(new GeoPoint(mLocation));
            }
        }

        // Update the screen to see changes take effect
        if (mMapView != null) {
            mMapView.postInvalidate();
        }
    }

    /**
     * Disables "follow" functionality.
     */
    public void disableFollowLocation() {
        mIsFollowing = false;
    }

    /**
     * If enabled, the map will center on your current location and automatically scroll as you
     * move. Scrolling the map in the UI will disable.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isFollowLocationEnabled() {
        return mIsFollowing;
    }

    @Override
    public void onLocationChanged(Location location, IMyLocationProvider source) {
        // If we had a previous location, let's get those bounds
        Location oldLocation = mLocation;
        if (oldLocation != null) {
            this.getMyLocationDrawingBounds(mMapView.getZoomLevel(), oldLocation,
                    mMyLocationPreviousRect);
        }

        mLocation = location;

        if (mLocation != null) {
            TileSystem.LatLongToPixelXY(mLocation.getLatitude(), mLocation.getLongitude(),
                    MapViewConstants.MAXIMUM_ZOOMLEVEL, mMapCoords);
            final int worldSize_2 = TileSystem.MapSize(MapViewConstants.MAXIMUM_ZOOMLEVEL) / 2;
            mMapCoords.offset(-worldSize_2, -worldSize_2);

            if (mIsFollowing && source instanceof MyLocationProvider) {
                mGeoPoint.setLatitudeE6((int) (mLocation.getLatitude() * 1E6));
                mGeoPoint.setLongitudeE6((int) (mLocation.getLongitude() * 1E6));
                mMapController.setZoom(DEFAULT_ZOOMLEVEL_LOCATED);
                mMapController.animateTo(mGeoPoint);
            } else {
                // Get new drawing bounds
                this.getMyLocationDrawingBounds(mMapView.getZoomLevel(), mLocation, mMyLocationRect);

                // If we had a previous location, merge in those bounds too
                if (oldLocation != null) {
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
            }
        }

        for (final Runnable runnable : mRunOnFirstFix) {
            new Thread(runnable).start();
        }
        mRunOnFirstFix.clear();
    }

    public boolean enableMyLocation(IMyLocationProvider myLocationProvider) {
        this.setMyLocationProvider(myLocationProvider);
        mIsLocationEnabled = false;
        return enableMyLocation();
    }

    /**
     * Enable receiving location updates from the provided IMyLocationProvider and show your
     * location on the maps. You will likely want to call enableMyLocation() from your Activity's
     * Activity.onResume() method, to enable the features of this overlay. Remember to call the
     * corresponding disableMyLocation() in your Activity's Activity.onPause() method to turn off
     * updates when in the background.
     */
    public boolean enableMyLocation() {
        if (mIsLocationEnabled)
            mMyLocationProvider.stopLocationProvider();

        boolean result = mMyLocationProvider.startLocationProvider(this);
        mIsLocationEnabled = result;

        // set initial location when enabled
        if (result && isFollowLocationEnabled()) {
            mLocation = mMyLocationProvider.getLastKnownLocation();
            if (mLocation != null) {
                TileSystem.LatLongToPixelXY(mLocation.getLatitude(), mLocation.getLongitude(),
                        MapViewConstants.MAXIMUM_ZOOMLEVEL, mMapCoords);
                final int worldSize_2 = TileSystem.MapSize(MapViewConstants.MAXIMUM_ZOOMLEVEL) / 2;
                mMapCoords.offset(-worldSize_2, -worldSize_2);
                mMapController.animateTo(new GeoPoint(mLocation));
            }
        }

        // Update the screen to see changes take effect
        if (mMapView != null) {
            mMapView.postInvalidate();
        }

        return result;
    }

    /**
     * Disable location updates
     */
    public void disableMyLocation() {
        mIsLocationEnabled = false;

        if (mMyLocationProvider != null) {
            mMyLocationProvider.stopLocationProvider();
        }

        // Update the screen to see changes take effect
        if (mMapView != null) {
            mMapView.postInvalidate();
        }
    }

    /**
     * If enabled, the map is receiving location updates and drawing your location on the map.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isMyLocationEnabled() {
        return mIsLocationEnabled;
    }

    public boolean runOnFirstFix(final Runnable runnable) {
        if (mMyLocationProvider != null && mLocation != null) {
            new Thread(runnable).start();
            return true;
        } else {
            mRunOnFirstFix.addLast(runnable);
            return false;
        }
    }
}
