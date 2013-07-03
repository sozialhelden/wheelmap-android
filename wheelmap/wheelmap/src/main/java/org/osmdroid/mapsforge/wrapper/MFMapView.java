package org.osmdroid.mapsforge.wrapper;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.MapViewMode;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.api.IProjection;

import android.content.Context;
import android.util.AttributeSet;

/**
 * A wrapper for the Google {@link org.mapsforge.android.maps.MapView} class. This implements {@link
 * IMapView}, which is also implemented by the osmdroid {@link org.osmdroid.views.MapView}.
 *
 * @author Neil Boyd
 */
public class MFMapView implements IMapView {

    private final org.mapsforge.android.maps.MapView mMapView;

    public MFMapView(final org.mapsforge.android.maps.MapView pMapView) {
        mMapView = pMapView;
    }

    public MFMapView(final Context pContext) {
        this(new org.mapsforge.android.maps.MapView(pContext));
    }

    public MFMapView(final Context pContext, final AttributeSet pAttrs) {
        this(new org.mapsforge.android.maps.MapView(pContext, pAttrs));
    }

    @Override
    public IMapController getController() {
        return new MapController(mMapView.getController());
    }

    @Override
    public IProjection getProjection() {
        return new Projection(mMapView.getProjection());
    }

    @Override
    public int getZoomLevel() {
        return mMapView.getZoomLevel();
    }

    @Override
    public int getLatitudeSpan() {
        return 0;
        // TODO return mMapView.getLatitudeSpan();
    }

    @Override
    public int getLongitudeSpan() {
        return 0;
        // TODO return mMapView.getLongitudeSpan();
    }

    @Override
    public IGeoPoint getMapCenter() {
        return new GeoPoint(mMapView.getMapCenter());
    }

    @Override
    public int getMaxZoomLevel() {
        return mMapView.getMaxZoomLevel();
    }

    @Override
    public void setBackgroundColor(final int pColor) {
        mMapView.setBackgroundColor(pColor);
    }

    public void onResume() {
        mMapView.onResume();
    }

    public void onPause() {
        mMapView.onPause();
    }

    public void destroy() {
        mMapView.destroy();
    }

    public MapViewMode getMapViewMode() {
        return mMapView.getMapViewMode();
    }


    public void setMapFileFromPreferences(String fileName) {
        mMapView.setMapFileFromPreferences(fileName);
    }

    public MapView getWrappedMapView() {
        return mMapView;
    }

    public boolean hasValidMapFile() {
        return mMapView.hasValidMapFile();
    }

    public String getMapFile() {
        return mMapView.getMapFile();
    }

    public boolean hasValidCenter() {
        return mMapView.hasValidCenter();
    }
}
